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

/**
 * 单条数据同步策略
 * 实现根据ID将MySQL中的数据同步到ES的逻辑
 *
 * @author ChonghaoGao
 * @date 2025/12/13
 */
@Slf4j
@Component("singleSyncStrategy")
@AllArgsConstructor
public class SingleSyncStrategy implements SyncStrategy {

    private final RemoteMetaInfoMapper remoteMetaInfoMapper;
    private final RemoteSearchESRepository remoteSearchESRepository;

    /**
     * 执行单条数据同步
     *
     * @param params 同步参数，第一个参数为数据ID
     * @return 是否同步成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object executeSync(Object... params) {
        if (params == null || params.length == 0 || !(params[0] instanceof Long)) {
            throw new IllegalArgumentException("单条数据同步需要提供Long类型的ID参数");
        }

        Long id = (Long)params[0];
        log.info("开始同步ID为 {} 的数据到ES", id);

        try {
            // 1. 根据ID查询MySQL数据，包括已删除的
            RemoteMetaDO metaInfo = remoteMetaInfoMapper.selectByIdIncludeDeleted(id);

            if (metaInfo == null) {
                // 2. 数据在MySQL中不存在（物理删除），从ES中删除
                log.warn("ID为 {} 的数据在MySQL中不存在，执行物理删除同步", id);
                remoteSearchESRepository.deleteMetaDoc(id);
                log.info("从ES中删除物理删除的数据，ID: {}", id);
            } else if (metaInfo.getDeleted() == 1) {
                // 3. 数据已被标记为逻辑删除，从ES中删除
                remoteSearchESRepository.deleteMetaDoc(metaInfo.getId());
                log.info("从ES中删除已标记为删除的数据，ID: {}", metaInfo.getId());
            } else {
                // 4. 数据正常，同步到ES
                remoteSearchESRepository.saveMetaDoc(metaInfo.toRemoteMetaDoc());
                log.info("同步ID为 {} 的数据成功", id);
            }
            return true;

        } catch (Exception e) {
            log.error("同步ID为 {} 的数据失败: {}", id, e.getMessage());
            return false;
        }
    }
}
