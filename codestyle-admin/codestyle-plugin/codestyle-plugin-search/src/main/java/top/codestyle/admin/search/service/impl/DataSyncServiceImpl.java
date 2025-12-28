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

package top.codestyle.admin.search.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import top.codestyle.admin.search.config.KafkaConfigProperties;
import top.codestyle.admin.search.model.dto.DataChangeMessage;
import top.codestyle.admin.search.service.DataSyncService;

import java.util.Map;

/**
 * 
 * Kafka生产者服务实现类，用于将Canal消息发送到Kafka
 * 注意：此服务只负责生产者角色，消费者逻辑由KafkaDataChangeConsumer处理
 * 
 * @author ChonghaoGao
 * @date 2025/12/22
 */
@Slf4j
@Service("dataSyncService")
@RequiredArgsConstructor
@ConditionalOnClass(name = "org.springframework.kafka.core.KafkaTemplate")
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class DataSyncServiceImpl implements DataSyncService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaConfigProperties kafkaConfigProperties;

    /**
     * 发送数据变更消息到Kafka（使用DataChangeMessage对象）
     * 
     * @param message 数据变更消息对象
     */
    @Override
    public void sendDataChangeMessage(DataChangeMessage message) {
        if (message == null) {
            log.warn("数据变更消息为空，忽略发送");
            return;
        }
        log.warn("进来了");
        try {
            // 确保消息ID已生成
            if (message.getMessageId() == null || message.getMessageId().isEmpty()) {
                message.setMessageId(message.getMessageId()); // 触发自动生成
            }

            // 发送到Kafka
            String topic = kafkaConfigProperties.getDataChangeTopic();
            String key = message.getMessageId(); // 使用messageId作为key，便于分区和去重

            // Spring Boot 3.x中KafkaTemplate.send()返回CompletableFuture，使用whenComplete处理回调
            kafkaTemplate.send(topic, key, message).whenComplete((result, exception) -> {
                if (exception == null) {
                    log.debug("Successfully sent message to Kafka: messageId={}, operation={}, partition={}, offset={}", message
                        .getMessageId(), message.getOperationType(), result != null
                            ? result.getRecordMetadata().partition()
                            : "unknown", result != null ? result.getRecordMetadata().offset() : "unknown");
                } else {
                    log.error("Failed to send message to Kafka: messageId={}, operation={}", message
                        .getMessageId(), message.getOperationType(), exception);
                }
            });

        } catch (Exception e) {
            log.error("Error sending data change message to Kafka: messageId={}", message.getMessageId(), e);
        }
    }

    /**
     * 发送数据变更消息到Kafka（使用多个参数）
     * 
     * @param operationType 操作类型：INSERT/UPDATE/DELETE
     * @param database      数据库名称
     * @param table         表名
     * @param afterData     变更后的数据
     * @param beforeData    变更前的数据
     */
    @Override
    public void sendDataChangeMessage(String operationType,
                                      String database,
                                      String table,
                                      Map<String, Object> afterData,
                                      Map<String, Object> beforeData) {
        try {
            // 创建数据变更消息
            DataChangeMessage message = new DataChangeMessage();
            message.setOperationType(operationType);
            message.setDatabase(database);
            message.setTable(table);
            message.setPrimaryKey(buildPrimaryKeyValue(afterData != null ? afterData : beforeData));
            message.setAfterData(afterData);
            message.setBeforeData(beforeData);
            message.setTimestamp(System.currentTimeMillis());

            // 生成消息ID（格式：database.table.primaryKey.timestamp）
            String messageId = String.format("%s.%s.%s.%d", database != null ? database : "unknown", table != null
                ? table
                : "unknown", message.getPrimaryKey() != null ? message.getPrimaryKey() : "unknown", message
                    .getTimestamp());
            message.setMessageId(messageId);

            // 调用重载方法
            sendDataChangeMessage(message);

        } catch (Exception e) {
            log.error("Error sending data change message to Kafka", e);
        }
    }

    /**
     * 构建主键值
     *
     * @param data 数据
     * @return 主键值
     */
    private Object buildPrimaryKeyValue(Map<String, Object> data) {
        // 尝试从常见的主键字段中获取值
        Object id = data.get("id");
        if (id != null) {
            return id;
        }
        Object uid = data.get("uid");
        if (uid != null) {
            return uid;
        }
        // 如果没有找到常见主键，返回第一个值
        return data.values().stream().findFirst().orElse(null);
    }
}
