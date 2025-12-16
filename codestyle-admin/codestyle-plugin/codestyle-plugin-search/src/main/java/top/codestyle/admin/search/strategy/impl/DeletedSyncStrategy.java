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
 * 删除数据同步策略
 * 实现处理已标记为删除的数据的同步逻辑
 *
 * @author ChonghaoGao
 * @date 2025/12/13
 */
@Slf4j
@Component("deletedSyncStrategy")
@AllArgsConstructor
public class DeletedSyncStrategy implements SyncStrategy {

    private final RemoteMetaInfoMapper remoteMetaInfoMapper;
    private final RemoteSearchESRepository remoteSearchESRepository;

    /**
     * 执行删除数据同步
     *
     * @param params 同步参数，此处不需要参数
     * @return 处理成功的数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object executeSync(Object... params) {
        log.info("开始同步删除操作");

        try {
            // 1. 使用自定义SQL查询所有已标记为删除的数据，绕过MyBatis-Plus的逻辑删除自动过滤
            List<RemoteMetaDO> deletedMetaInfos = remoteMetaInfoMapper.selectAllDeletedData();

            log.info("从MySQL查询到 {} 条已删除数据", deletedMetaInfos.size());

            // 2. 逐条同步到ES（从ES中删除）
            remoteSearchESRepository.deleteAllMetaDocs();

            // 3. 额外处理：查询所有已恢复的数据（deleted从1变为0），并同步到ES
            List<RemoteMetaDO> restoredMetaInfos = remoteMetaInfoMapper.selectAllRestoredData();

            log.info("从MySQL查询到 {} 条已恢复数据", restoredMetaInfos.size());
            List<RemoteMetaDoc> remoteMetaDocs = restoredMetaInfos.stream()
                .parallel()
                .map(RemoteMetaDO::toRemoteMetaDoc)
                .toList();
            remoteSearchESRepository.saveMetaDocsByList(remoteMetaDocs);

            log.info("同步删除操作完成，成功处理 {} 条数据", remoteMetaDocs.size());
            return remoteMetaDocs.size();

        } catch (Exception e) {
            log.error("同步删除操作失败: {}", e.getMessage());
            throw new RuntimeException("同步删除操作失败", e);
        }
    }
}
