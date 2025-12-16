/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this fileInfo except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.codestyle.admin.search.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.codestyle.admin.search.mapper.RemoteMetaInfoMapper;
import top.codestyle.admin.search.model.mysql.entity.RemoteMetaDO;
import top.codestyle.admin.search.respository.es.RemoteSearchESRepository;
import top.codestyle.admin.search.strategy.SyncStrategy;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 增量同步策略
 * 实现将指定时间后更新的数据同步到ES的逻辑
 *
 * @author ChonghaoGao
 * @date 2025/12/13
 */
@Slf4j
@Component("incrementalSyncStrategy")
@AllArgsConstructor
public class IncrementalSyncStrategy implements SyncStrategy {

    private final RemoteMetaInfoMapper remoteMetaInfoMapper;
    private final RemoteSearchESRepository remoteSearchESRepository;

    /**
     * 执行增量同步
     *
     * @param params 同步参数，第一个参数为上次同步时间
     * @return 同步成功的数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object executeSync(Object... params) {
        if (params == null || params.length == 0 || !(params[0] instanceof LocalDateTime lastSyncTime)) {
            throw new IllegalArgumentException("增量同步需要提供LocalDateTime类型的上次同步时间");
        }

        log.info("开始增量同步MySQL数据到ES，上次同步时间: {}", lastSyncTime);

        try {
            // 1. 查询指定时间后更新的数据
            // 转换LocalDateTime为Date
            Date lastSyncDate = Date.from(lastSyncTime.atZone(ZoneId.systemDefault()).toInstant());

            // 2. 使用自定义方法查询更新的数据，包括已删除的
            List<RemoteMetaDO> updatedMetaInfos = remoteMetaInfoMapper.selectUpdatedDataAfterTime(lastSyncDate);

            log.info("从MySQL查询到 {} 条增量数据", updatedMetaInfos.size());

            // 3. 逐条同步到ES
            int successCount = 0;
            for (RemoteMetaDO metaInfo : updatedMetaInfos) {
                try {
                    if (metaInfo.getDeleted() == 1) {
                        // 如果数据已被标记为删除，则从ES中删除该文档
                        remoteSearchESRepository.deleteMetaDoc(metaInfo.getId());
                        log.info("从ES中删除已标记为删除的数据，ID: {}", metaInfo.getId());
                    } else {
                        // 更新或插入ES文档
                        remoteSearchESRepository.saveMetaDoc(metaInfo.toRemoteMetaDoc());
                    }
                    successCount++;
                } catch (Exception e) {
                    log.error("同步ID为 {} 的数据失败: {}", metaInfo.getId(), e.getMessage());
                }
            }

            // 4. 处理物理删除：对比MySQL和ES数据，找出物理删除的数据并从ES中删除
            successCount += handlePhysicalDeletions();

            log.info("增量同步完成，成功同步 {} 条数据", successCount);
            return successCount;

        } catch (Exception e) {
            log.error("增量同步失败: {}", e.getMessage());
            throw new RuntimeException("增量同步失败", e);
        }
    }

    /**
     * 处理物理删除：对比MySQL和ES数据，找出物理删除的数据并从ES中删除
     * 
     * @return 处理成功的数量
     */
    private int handlePhysicalDeletions() {
        log.info("开始处理物理删除");

        try {
            // 1. 获取MySQL中所有存在的数据ID（包括逻辑删除的）
            List<RemoteMetaDO> allMysqlData = remoteMetaInfoMapper.selectList(null);
            // 提取MySQL中所有数据的ID
            Set<Long> mysqlIds = allMysqlData.stream().map(RemoteMetaDO::getId).collect(Collectors.toSet());

            // 2. 获取ES中所有文档的ID
            Iterable<Long> esIdsIterable = remoteSearchESRepository.getAllMetaDocIds();
            Set<Long> esIds = new HashSet<>();
            esIdsIterable.forEach(esIds::add);

            log.info("MySQL中存在 {} 条数据，ES中存在 {} 条文档", mysqlIds.size(), esIds.size());

            // 3. 找出ES中存在但MySQL中不存在的ID（物理删除的数据）
            Set<Long> physicalDeletedIds = new HashSet<>(esIds);
            physicalDeletedIds.removeAll(mysqlIds);

            log.info("发现 {} 条物理删除的数据", physicalDeletedIds.size());

            // 4. 从ES中删除这些物理删除的数据
            int successCount = 0;
            for (Long id : physicalDeletedIds) {
                try {
                    remoteSearchESRepository.deleteMetaDoc(id);
                    log.info("从ES中删除物理删除的数据，ID: {}", id);
                    successCount++;
                } catch (Exception e) {
                    log.error("删除物理删除的数据失败，ID: {}, 错误: {}", id, e.getMessage());
                }
            }

            log.info("物理删除处理完成，成功处理 {} 条数据", successCount);
            return successCount;

        } catch (Exception e) {
            log.error("处理物理删除失败: {}", e.getMessage());
            // 不抛出异常，允许继续执行后续同步步骤
            return 0;
        }
    }
}
