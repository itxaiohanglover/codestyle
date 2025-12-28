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

package top.codestyle.admin.search.service;

/**
 * 
 * ES同步服务接口
 * 
 * <p><b>注意：</b>增量同步通过Canal+Kafka实现，不需要在此接口中定义。</p>
 * <p>架构流程：MySQL Binlog → Canal Server → Kafka → Kafka消费者 → ES</p>
 * 
 * @author ChonghaoGao
 * @date 2025/12/22
 */
public interface SyncService {

    /**
     * 全量同步：将MySQL中所有数据同步到ES
     * 
     * <p>注意：此方法会先删除ES中的所有数据，然后重新载入，不应频繁调用。</p>
     * <p>通常在应用启动时执行一次，后续增量同步通过Canal+Kafka实现。</p>
     * 
     * @return 同步成功的数量
     */
    int fullSync();
}
