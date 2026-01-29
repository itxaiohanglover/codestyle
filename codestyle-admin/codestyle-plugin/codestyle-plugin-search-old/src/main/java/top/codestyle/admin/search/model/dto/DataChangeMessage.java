/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package top.codestyle.admin.search.model.dto;

import lombok.Data;
import java.util.Map;

/**
 * 
 * 数据变更消息模型，用于在Kafka中传输数据库变更信息
 * 
 * @author ChonghaoGao
 * @date 2025/12/22
 */
@Data
public class DataChangeMessage {

    /**
     * 消息唯一ID（用于幂等性保证）
     * 格式：{database}.{table}.{primaryKey}.{timestamp}
     */
    private String messageId;

    /**
     * 操作类型：INSERT/UPDATE/DELETE
     */
    private String operationType;

    /**
     * 数据库名称
     */
    private String database;

    /**
     * 表名
     */
    private String table;

    /**
     * 主键值（用于删除和更新操作）
     */
    private Object primaryKey;

    /**
     * 变更后的数据（用于插入和更新操作）
     */
    private Map<String, Object> afterData;

    /**
     * 变更前的数据（用于更新和删除操作）
     */
    private Map<String, Object> beforeData;

    /**
     * 变更时间戳
     */
    private long timestamp;

    /**
     * 生成消息ID
     * 如果messageId为空，则自动生成
     */
    public String getMessageId() {
        if (messageId == null || messageId.isEmpty()) {
            // 生成唯一消息ID：database.table.primaryKey.timestamp
            messageId = String.format("%s.%s.%s.%d", database != null ? database : "unknown", table != null
                ? table
                : "unknown", primaryKey != null ? primaryKey : "unknown", timestamp > 0
                    ? timestamp
                    : System.currentTimeMillis());
        }
        return messageId;
    }
}
