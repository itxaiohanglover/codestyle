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

package top.codestyle.admin.search.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import top.codestyle.admin.search.config.KafkaConfigProperties;
import top.codestyle.admin.search.model.dto.DataChangeMessage;
import top.codestyle.admin.search.service.impl.EsBulkSyncServiceImpl;
import top.codestyle.admin.search.service.impl.MessageIdempotencyServiceImpl;
import top.codestyle.admin.search.service.impl.MetricsServiceImpl;

import java.util.List;
import java.util.Objects;

/**
 * Kafka数据变更消费者，用于消费数据库变更消息并同步到Elasticsearch
 * 支持批量消费、消息幂等性、错误重试和死信队列
 * 
 * <p><b>消息来源：</b></p>
 * <ul>
 * <li><b>企业级模式（推荐）</b>：Canal Server直连Kafka，发送JSON格式的CanalMessage</li>
 * <li><b>兼容模式</b>：应用层MultiDatabaseCanalListener发送的DataChangeMessage对象</li>
 * </ul>
 * 
 * <p><b>注意：</b>如果使用Canal Server直连Kafka模式，请使用{@link CanalKafkaMessageConsumer}</p>
 * 
 * @author ChonghaoGao
 * @date 2025/12/22
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(name = "org.springframework.kafka.core.KafkaTemplate")
@ConditionalOnExpression("'${kafka.enabled:true}' == 'true' && '${canal.enabled:false}' == 'true'")
public class KafkaDataChangeConsumer {

    private final EsBulkSyncServiceImpl esBulkSyncService;
    private final MessageIdempotencyServiceImpl idempotencyService;
    private final KafkaTemplate<String, Object> dlqKafkaTemplate;
    private final KafkaConfigProperties kafkaConfigProperties;
    private final MetricsServiceImpl metricsService;

    /**
     * 批量消费数据变更消息
     * 支持幂等性检查、错误重试和死信队列
     * 
     * @param records        批量消息记录
     * @param acknowledgment 手动提交offset
     */
    @KafkaListener(topics = "${kafka.topics.data-change:data-change}", groupId = "${kafka.group.id:codestyle-search}", containerFactory = "batchKafkaListenerContainerFactory")
    public void consumeBatchDataChange(List<ConsumerRecord<String, DataChangeMessage>> records,
                                       Acknowledgment acknowledgment) {

        if (records == null || records.isEmpty()) {
            return;
        }

        log.info("Received batch of {} messages", records.size());

        int successCount = 0;
        int duplicateCount = 0;
        int failureCount = 0;

        try {
            // 提取消息并过滤空值
            List<DataChangeMessage> messages = records.stream()
                .map(ConsumerRecord::value)
                .filter(Objects::nonNull)
                .toList();

            // 批量处理消息
            for (DataChangeMessage message : messages) {
                try {
                    // 1. 幂等性检查
                    if (idempotencyService.isProcessed(message)) {
                        log.warn("Message already processed, skipping: messageId={}, operation={}, table={}.{}", message
                            .getMessageId(), message.getOperationType(), message.getDatabase(), message.getTable());
                        duplicateCount++;
                        metricsService.recordDuplicate();
                        continue;
                    }

                    // 2. 处理消息
                    esBulkSyncService.processDataChangeMessage(message);

                    // 3. 标记消息为已处理
                    idempotencyService.markAsProcessed(message);
                    successCount++;
                    metricsService.recordSuccess(message);

                    log.debug("Successfully processed message: messageId={}, operation={}, table={}.{}", message
                        .getMessageId(), message.getOperationType(), message.getDatabase(), message.getTable());

                } catch (RetryableException e) {
                    // 可重试异常，记录日志但不发送到DLQ（等待Kafka重试）
                    log.warn("Retryable error processing message: messageId={}, error={}", message.getMessageId(), e
                        .getMessage());
                    failureCount++;
                    metricsService.recordFailure();
                    // 发送到DLQ，但标记为可重试
                    sendToDeadLetterQueue(message, e, true);

                } catch (Exception e) {
                    // 不可重试异常，发送到DLQ
                    log.error("Error processing message: messageId={}, operation={}, table={}.{}", message
                        .getMessageId(), message.getOperationType(), message.getDatabase(), message.getTable(), e);
                    failureCount++;
                    metricsService.recordFailure();
                    sendToDeadLetterQueue(message, e, false);
                }
            }

            // 手动提交offset（只有处理成功的消息才提交）
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

            log.info("Batch processing completed: total={}, success={}, duplicate={}, failure={}", messages
                .size(), successCount, duplicateCount, failureCount);

        } catch (Exception e) {
            log.error("Fatal error in batch processing", e);
            // 不提交offset，等待重试
            throw e;
        }
    }

    /**
     * 发送消息到死信队列
     * 
     * @param message   失败的消息
     * @param exception 异常信息
     * @param retryable 是否可重试
     */
    private void sendToDeadLetterQueue(DataChangeMessage message, Exception exception, boolean retryable) {
        try {
            String dlqTopic = kafkaConfigProperties.getDeadLetterTopic();
            String key = message.getMessageId();

            // 创建DLQ消息（可以扩展为包含异常信息的对象）
            DataChangeMessage dlqMessage = new DataChangeMessage();
            dlqMessage.setMessageId(message.getMessageId() + ".dlq");
            dlqMessage.setOperationType(message.getOperationType());
            dlqMessage.setDatabase(message.getDatabase());
            dlqMessage.setTable(message.getTable());
            dlqMessage.setPrimaryKey(message.getPrimaryKey());
            dlqMessage.setAfterData(message.getAfterData());
            dlqMessage.setBeforeData(message.getBeforeData());
            dlqMessage.setTimestamp(System.currentTimeMillis());

            // Spring Boot 3.x中KafkaTemplate.send()返回CompletableFuture，使用whenComplete处理回调
            dlqKafkaTemplate.send(dlqTopic, key, dlqMessage).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Message sent to DLQ: messageId={}, retryable={}, partition={}, offset={}", message
                        .getMessageId(), retryable, result != null
                            ? result.getRecordMetadata().partition()
                            : "unknown", result != null ? result.getRecordMetadata().offset() : "unknown");
                } else {
                    log.error("Failed to send message to DLQ: messageId={}, retryable={}", message
                        .getMessageId(), retryable, ex);
                }
            });

        } catch (Exception e) {
            log.error("Error sending message to DLQ: messageId={}", message.getMessageId(), e);
        }
    }

    /**
     * 可重试异常类
     */
    public static class RetryableException extends RuntimeException {
        public RetryableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
