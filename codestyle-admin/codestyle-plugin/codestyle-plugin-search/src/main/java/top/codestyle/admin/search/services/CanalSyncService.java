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

/**
 * Canal同步服务接口
 * 扩展SyncService，增加Canal相关的同步方法
 *
 * @author ChonghaoGao
 * @date 2025/12/13
 */
public interface CanalSyncService extends SyncService {

    /**
     * 全量同步：通过Canal实现全量更新
     * 将MySQL中所有数据同步到ES
     *
     * @return 同步成功的数量
     */
    int fullSyncByCanal();
}
