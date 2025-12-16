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

package top.codestyle.admin.search.services.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.codestyle.admin.search.services.CanalSyncService;
import top.codestyle.admin.search.strategy.SyncStrategyFactory;

import java.time.LocalDateTime;

/**
 * ES同步服务实现类
 * 实现MySQL与ES之间的数据同步逻辑
 * 使用策略模式优化同步策略
 *
 * @author ChonghaoGao
 * @date 2025/12/4
 */
@Slf4j
@Service
@AllArgsConstructor
public class SyncServiceImpl implements CanalSyncService {

    private final SyncStrategyFactory syncStrategyFactory;

    /**
     * 全量同步：将MySQL中所有数据同步到ES
     * 注意：此方法会先删除ES中的所有数据，然后重新载入，不应频繁调用
     * 
     * @return 同步成功的数量
     */
    @Override
    public int fullSync() {
        return (int)syncStrategyFactory.getSyncStrategy("fullSync").executeSync();
    }

    /**
     * 通过Canal实现全量更新
     * 实际上是调用fullSync方法，确保只有一个全量更新入口
     *
     * @return 同步成功的数量
     */
    @Override
    public int fullSyncByCanal() {
        log.info("通过Canal实现全量更新，调用统一的fullSync方法");
        return fullSync();
    }

    /**
     * 增量同步：将指定时间后更新的数据同步到ES
     * 支持处理逻辑删除和物理删除
     * 
     * @param lastSyncTime 上次同步时间
     * @return 同步成功的数量
     */
    @Override
    public int incrementalSync(LocalDateTime lastSyncTime) {

        return (int)syncStrategyFactory.getSyncStrategy("incrementalSync").executeSync(lastSyncTime);
    }

    /**
     * 同步删除操作：处理已标记为删除的数据
     * 注意：此方法查询所有已标记为删除的数据，并从ES中删除它们
     * 同时确保恢复删除状态的数据能被重新同步到ES
     * 
     * @return 处理成功的数量
     */
    @Override
    public int syncDeletedData() {
        return (int)syncStrategyFactory.getSyncStrategy("deletedSync").executeSync();
    }

    /**
     * 单条数据同步：根据ID将MySQL中的数据同步到ES
     * 支持软删除和物理删除：
     * - 如果数据在MySQL中存在且未删除：同步到ES
     * - 如果数据在MySQL中存在且已删除：从ES中删除
     * - 如果数据在MySQL中不存在（物理删除）：从ES中删除
     * 
     * @param id MySQL数据ID
     * @return 是否同步成功
     */
    @Override
    public boolean syncById(Long id) {
        return (boolean)syncStrategyFactory.getSyncStrategy("singleSync").executeSync(id);
    }
}
