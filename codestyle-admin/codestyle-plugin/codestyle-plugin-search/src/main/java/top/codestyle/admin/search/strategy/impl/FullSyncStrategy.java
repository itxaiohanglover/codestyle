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
import top.codestyle.admin.search.model.es.entity.RemoteMetaDoc;
import top.codestyle.admin.search.model.mysql.entity.RemoteMetaDO;
import top.codestyle.admin.search.respository.es.RemoteSearchESRepository;
import top.codestyle.admin.search.strategy.SyncStrategy;

import java.util.List;

/**
 * 全量同步策略
 * 实现将MySQL中所有数据同步到ES的逻辑
 *
 * @author ChonghaoGao
 * @date 2025/12/13
 */
@Slf4j
@Component("fullSyncStrategy")
@AllArgsConstructor
public class FullSyncStrategy implements SyncStrategy {

    private final RemoteMetaInfoMapper remoteMetaInfoMapper;
    private final RemoteSearchESRepository remoteSearchESRepository;

    /**
     * 执行全量同步
     *
     * @param params 同步参数，此处不需要参数
     * @return 同步成功的数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object executeSync(Object... params) {
        log.info("开始全量同步MySQL数据到ES");

        try {
            // 1. 先删除ES中的所有数据
            remoteSearchESRepository.deleteAllMetaDocs();

            // 2. 查询MySQL中所有数据（未被逻辑删除的）
            List<RemoteMetaDO> allMetaInfos = remoteMetaInfoMapper.selectList(null);
            log.info("从MySQL查询到 {} 条数据", allMetaInfos.size());

            if (allMetaInfos.isEmpty()) {
                log.info("MySQL中没有数据，无需同步");
                return 0;
            }

            // 3. 直接批量处理数据，减少日志打印
            int totalCount = allMetaInfos.size();
            List<RemoteMetaDoc> allMetaDocs = allMetaInfos.parallelStream()
                    .map(RemoteMetaDO::toRemoteMetaDoc)
                    .toList();
            remoteSearchESRepository.saveMetaDocsByList(allMetaDocs);

            log.info("全量同步完成，成功同步 {} 条数据", totalCount);
            return totalCount;

        } catch (Exception e) {
            log.error("全量同步失败: {}", e.getMessage());
            throw new RuntimeException("全量同步失败", e);
        }
    }
}
