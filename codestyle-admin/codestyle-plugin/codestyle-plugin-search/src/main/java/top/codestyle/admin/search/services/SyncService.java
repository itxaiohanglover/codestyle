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

package top.codestyle.admin.search.services;

import java.time.LocalDateTime;

/**
 * ES同步服务接口
 * 定义MySQL与ES之间的数据同步方法
 *
 * @author ChonghaoGao
 * @date 2025/12/4
 */
public interface SyncService {

    /**
     * 全量同步：将MySQL中所有数据同步到ES
     * 
     * @return 同步成功的数量
     */
    int fullSync();

    /**
     * 增量同步：将指定时间后更新的数据同步到ES
     * 
     * @param lastSyncTime 上次同步时间
     * @return 同步成功的数量
     */
    int incrementalSync(LocalDateTime lastSyncTime);

    /**
     * 单条数据同步：根据ID将MySQL中的数据同步到ES
     * 
     * @param id MySQL数据ID
     * @return 是否同步成功
     */
    boolean syncById(Long id);

    /**
     * 逻辑删除的数据同步：将MySQL中的数据同步到ES
     *
     *
     * @return 是否同步成功
     */
    int syncDeletedData();
}