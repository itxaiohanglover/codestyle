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

package top.codestyle.admin.search.service;

import top.codestyle.admin.search.model.dto.DataChangeMessage;

import java.util.Map;

/**
 * 
 * Kafka生产者服务接口，用于将Canal消息发送到Kafka
 * 注意：此服务只负责生产者角色，消费者逻辑由KafkaDataChangeConsumer处理
 * 
 * @author ChonghaoGao
 * @date 2025/12/22
 */
public interface DataSyncService {

    /**
     * 发送数据变更消息到Kafka（使用DataChangeMessage对象）
     * 
     * @param message 数据变更消息对象
     */
    void sendDataChangeMessage(DataChangeMessage message);

    /**
     * 发送数据变更消息到Kafka（使用多个参数）
     * 
     * @param operationType 操作类型：INSERT/UPDATE/DELETE
     * @param database      数据库名称
     * @param table         表名
     * @param afterData     变更后的数据
     * @param beforeData    变更前的数据
     */
    void sendDataChangeMessage(String operationType,
                               String database,
                               String table,
                               Map<String, Object> afterData,
                               Map<String, Object> beforeData);
}
