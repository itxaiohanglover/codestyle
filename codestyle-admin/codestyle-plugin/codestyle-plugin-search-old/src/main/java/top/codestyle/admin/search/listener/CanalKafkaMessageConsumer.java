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

package top.codestyle.admin.search.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import top.codestyle.admin.search.config.KafkaConfigProperties;
import top.codestyle.admin.search.model.dto.CanalMessage;
import top.codestyle.admin.search.model.dto.DataChangeMessage;
import top.codestyle.admin.search.util.CanalMessageConverter;
import top.codestyle.admin.search.service.impl.EsBulkSyncServiceImpl;
import top.codestyle.admin.search.service.impl.MessageIdempotencyServiceImpl;
import top.codestyle.admin.search.service.impl.MetricsServiceImpl;

import java.util.List;
import java.util.Objects;

/**
 * 
 * @author chonghaoGao
 * @date 2025/12/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(name = "org.springframework.kafka.core.ConsumerFactory")
@ConditionalOnExpression("!${canal.enabled:false} && ${kafka.enabled:true}")
public class CanalKafkaMessageConsumer {

    private final EsBulkSyncServiceImpl esBulkSyncService;
    private final MessageIdempotencyServiceImpl idempotencyService;
    private final CanalMessageConverter canalMessageConverter;
    private final KafkaTemplate<String, Object> dlqKafkaTemplate;
    private final KafkaConfigProperties kafkaConfigProperties;
    private final MetricsServiceImpl metricsService;
    private final ObjectMapper objectMapper;
    private final Environment environment;

    /**
     * 初始化方法，验证依赖注入和配置
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("========================================");
        log.info("CanalKafkaMessageConsumer 初始化完成");
        log.info("========================================");

        // 输出配置诊断信息
        String canalEnabled = environment.getProperty("canal.enabled", "false");
        String kafkaEnabled = environment.getProperty("kafka.enabled", "true");
        log.info("配置诊断:");
        log.info("  canal.enabled = {}", canalEnabled);
        log.info("  kafka.enabled = {}", kafkaEnabled);
        log.info("  条件评估: canal.enabled=false && kafka.enabled=true");

        // 输出依赖注入状态
        log.info("依赖注入状态:");
        log.info("  EsBulkSyncService: {}", esBulkSyncService != null ? "已注入" : "未注入");
        log.info("  MessageIdempotencyService: {}", idempotencyService != null ? "已注入" : "未注入");
        log.info("  CanalMessageConverter: {}", canalMessageConverter != null ? "已注入" : "未注入");
        log.info("  KafkaConfigProperties: {}", kafkaConfigProperties != null ? "已注入" : "未注入");

        // 输出Kafka配置
        if (kafkaConfigProperties != null) {
            log.info("Kafka配置:");
            log.info("  Topic: {}", kafkaConfigProperties.getDataChangeTopic());
            log.info("  Group ID: {}", kafkaConfigProperties.getGroupId());
            log.info("  Bootstrap Servers: {}", kafkaConfigProperties.getBootstrapServers());
        }

        // 输出监听配置
        String topic = environment.getProperty("kafka.topics.data-change", "data-change");
        String groupId = environment.getProperty("kafka.group.id", "codestyle-search");
        log.info("监听配置:");
        log.info("  Topic: {}", topic);
        log.info("  Group ID: {}", groupId);

        log.info("========================================");
        log.info("CanalKafkaMessageConsumer 已就绪，等待接收Kafka消息");
        log.info("========================================");
    }

    /**
     * 批量消费Canal Server发送的Kafka消息
     * 
     * <p>Canal Server发送的消息格式是JSON字符串，需要解析为CanalMessage，然后转换为DataChangeMessage</p>
     * 
     * @param records        批量消息记录（JSON字符串格式）
     * @param acknowledgment 手动提交offset
     */
    @KafkaListener(topics = "${kafka.topics.data-change:data-change}", groupId = "${kafka.group.id:codestyle-search}", containerFactory = "canalKafkaListenerContainerFactory")
    public void consumeBatchCanalMessages(List<ConsumerRecord<String, String>> records, Acknowledgment acknowledgment) {
        // 记录每次调用，即使没有消息也要记录（用于诊断消费者是否在运行）
        log.info("========== Kafka监听器被触发 ========== records数量: {}", records != null ? records.size() : 0);
        // #region agent log
        try {
            java.util.Map<String, Object> logData = new java.util.HashMap<>();
            logData.put("recordCount", records != null ? records.size() : 0);
            logData.put("timestamp", System.currentTimeMillis());
            logData.put("sessionId", "debug-session");
            logData.put("runId", "run1");
            logData.put("hypothesisId", "A");
            logData.put("location", "CanalKafkaMessageConsumer.java:115");
            logData.put("message", "收到Kafka消息");
            java.nio.file.Files.write(java.nio.file.Paths
                .get("d:\\java_projects\\code_style_template\\codestyle\\codestyle-admin\\.cursor\\debug.log"), (new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(logData) + "\n")
                    .getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception e) {
        }
        // #endregion
        log.info("================== Kafka消息接收 ==================");
        log.info("Kafka监听器被调用，收到 {} 条消息", records != null ? records.size() : 0);

        if (records == null || records.isEmpty()) {
            log.info("消息列表为空，跳过处理");
            return;
        }

        log.info("收到Kafka消息批次，数量: {}", records.size());
        log.info("消息来源: Canal Server (Kafka模式)");

        // 输出每条消息的元数据（用于诊断）
        for (int i = 0; i < records.size(); i++) {
            ConsumerRecord<String, String> record = records.get(i);
            log.info("消息[{}] 元数据:", i);
            log.info("  Topic: {}", record.topic());
            log.info("  Partition: {}", record.partition());
            log.info("  Offset: {}", record.offset());
            log.info("  Key: {}", record.key());
            log.info("  Value长度: {} 字符", record.value() != null ? record.value().length() : 0);
            if (record.value() != null) {
                String preview = record.value().length() > 500
                    ? record.value().substring(0, 500) + "..."
                    : record.value();
                log.info("  Value: {}", preview);
            }
        }

        int successCount = 0;
        int duplicateCount = 0;
        int failureCount = 0;

        try {
            // 解析Canal消息并转换为DataChangeMessage
            List<DataChangeMessage> messages = records.stream()
                .map(ConsumerRecord::value)
                .filter(Objects::nonNull)
                .flatMap(jsonMessage -> {
                    try {
                        log.info("开始解析Canal消息，长度: {} 字符", jsonMessage.length());
                        log.info("消息内容预览: {}", jsonMessage.length() > 300
                            ? jsonMessage.substring(0, 300) + "..."
                            : jsonMessage);

                        // 解析JSON为CanalMessage
                        CanalMessage canalMessage = objectMapper.readValue(jsonMessage, CanalMessage.class);
                        log.info("Canal消息解析成功:");
                        log.info("  type: {}", canalMessage.getType());
                        log.info("  database: {}", canalMessage.getDatabase());
                        log.info("  table: {}", canalMessage.getTable());
                        log.info("  isDdl: {}", canalMessage.getIsDdl());
                        log.info("  dataSize: {}", canalMessage.getData() != null ? canalMessage.getData().size() : 0);

                        // 跳过DDL语句
                        if (Boolean.TRUE.equals(canalMessage.getIsDdl())) {
                            log.info("跳过DDL语句: {}", canalMessage.getSql());
                            return java.util.stream.Stream.<DataChangeMessage>empty();
                        }

                        // 转换为DataChangeMessage（可能一条Canal消息包含多条数据变更）
                        List<DataChangeMessage> dataChangeMessages = canalMessageConverter.convert(canalMessage);
                        log.info("转换为 {} 条DataChangeMessage", dataChangeMessages.size());

                        for (DataChangeMessage msg : dataChangeMessages) {
                            log.info("  DataChangeMessage: operation={}, table={}.{}, primaryKey={}", msg
                                .getOperationType(), msg.getDatabase(), msg.getTable(), msg.getPrimaryKey());
                        }

                        return dataChangeMessages.stream();
                    } catch (Exception e) {
                        log.error("解析Canal消息失败: {}", e.getMessage(), e);
                        log.error("消息内容: {}", jsonMessage.length() < 1000
                            ? jsonMessage
                            : jsonMessage.substring(0, 1000) + "...");
                        return java.util.stream.Stream.<DataChangeMessage>empty();
                    }
                })
                .filter(Objects::nonNull)
                .toList();

            log.info("================== 消息解析完成 ==================");
            log.info("原始Canal消息数: {}, 转换后DataChangeMessage数: {}", records.size(), messages.size());

            // #region agent log
            try {
                java.util.Map<String, Object> logData = new java.util.HashMap<>();
                logData.put("sessionId", "debug-session");
                logData.put("runId", "run1");
                logData.put("hypothesisId", "E");
                logData.put("location", "CanalKafkaMessageConsumer:AFTER_CONVERT");
                logData.put("message", "消息转换完成");
                logData.put("data", java.util.Map.of("recordsSize", records.size(), "messagesSize", messages.size()));
                logData.put("timestamp", System.currentTimeMillis());
                java.nio.file.Files.write(java.nio.file.Paths
                    .get("d:\\java_projects\\code_style_template\\codestyle\\codestyle-admin\\.cursor\\debug.log"), (new com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(logData) + "\n")
                        .getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
            } catch (Exception e) {
            }
            // #endregion

            // 批量处理消息
            for (DataChangeMessage message : messages) {
                // #region agent log
                try {
                    java.util.Map<String, Object> logData = new java.util.HashMap<>();
                    logData.put("sessionId", "debug-session");
                    logData.put("runId", "run1");
                    logData.put("hypothesisId", "E");
                    logData.put("location", "CanalKafkaMessageConsumer:BEFORE_PROCESS");
                    logData.put("message", "准备处理消息");
                    logData.put("data", java.util.Map.of("messageId", message.getMessageId() != null
                        ? message.getMessageId()
                        : "NULL", "operation", message.getOperationType() != null
                            ? message.getOperationType()
                            : "NULL", "table", message.getTable() != null ? message.getTable() : "NULL"));
                    logData.put("timestamp", System.currentTimeMillis());
                    java.nio.file.Files.write(java.nio.file.Paths
                        .get("d:\\java_projects\\code_style_template\\codestyle\\codestyle-admin\\.cursor\\debug.log"), (new com.fasterxml.jackson.databind.ObjectMapper()
                            .writeValueAsString(logData) + "\n")
                            .getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
                } catch (Exception e) {
                }
                // #endregion
                try {
                    // 1. 幂等性检查
                    boolean isProcessed = idempotencyService.isProcessed(message);
                    log.info("幂等性检查: messageId={}, isProcessed={}, operation={}, table={}.{}", message
                        .getMessageId(), isProcessed, message.getOperationType(), message.getDatabase(), message
                            .getTable());
                    // #region agent log
                    try {
                        java.util.Map<String, Object> logData = new java.util.HashMap<>();
                        logData.put("sessionId", "debug-session");
                        logData.put("runId", "run1");
                        logData.put("hypothesisId", "E");
                        logData.put("location", "CanalKafkaMessageConsumer:IDEMPOTENCY_CHECK");
                        logData.put("message", "幂等性检查结果");
                        logData.put("data", java.util.Map.of("messageId", message.getMessageId() != null
                            ? message.getMessageId()
                            : "NULL", "isProcessed", isProcessed));
                        logData.put("timestamp", System.currentTimeMillis());
                        java.nio.file.Files.write(java.nio.file.Paths
                            .get("d:\\java_projects\\code_style_template\\codestyle\\codestyle-admin\\.cursor\\debug.log"), (new com.fasterxml.jackson.databind.ObjectMapper()
                                .writeValueAsString(logData) + "\n")
                                .getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
                    } catch (Exception e) {
                    }
                    // #endregion
                    if (isProcessed) {
                        log.warn("Message already processed, skipping: messageId={}, operation={}, table={}.{}", message
                            .getMessageId(), message.getOperationType(), message.getDatabase(), message.getTable());
                        duplicateCount++;
                        metricsService.recordDuplicate();
                        // #region agent log
                        try {
                            java.util.Map<String, Object> logData = new java.util.HashMap<>();
                            logData.put("sessionId", "debug-session");
                            logData.put("runId", "run1");
                            logData.put("hypothesisId", "E");
                            logData.put("location", "CanalKafkaMessageConsumer:SKIPPED_DUPLICATE");
                            logData.put("message", "消息被跳过（已处理）");
                            logData.put("data", java.util.Map.of("messageId", message.getMessageId() != null
                                ? message.getMessageId()
                                : "NULL"));
                            logData.put("timestamp", System.currentTimeMillis());
                            java.nio.file.Files.write(java.nio.file.Paths
                                .get("d:\\java_projects\\code_style_template\\codestyle\\codestyle-admin\\.cursor\\debug.log"), (new com.fasterxml.jackson.databind.ObjectMapper()
                                    .writeValueAsString(logData) + "\n")
                                    .getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
                        } catch (Exception e) {
                        }
                        // #endregion
                        continue;
                    }

                    // 2. 处理消息（同步到ES）
                    log.info("开始处理消息，同步到ES: messageId={}, operation={}, table={}.{}, primaryKey={}", message
                        .getMessageId(), message.getOperationType(), message.getDatabase(), message.getTable(), message
                            .getPrimaryKey());
                    // #region agent log
                    try {
                        java.util.Map<String, Object> logData = new java.util.HashMap<>();
                        logData.put("sessionId", "debug-session");
                        logData.put("runId", "run1");
                        logData.put("hypothesisId", "E");
                        logData.put("location", "CanalKafkaMessageConsumer:BEFORE_ES_SYNC");
                        logData.put("message", "准备同步到ES");
                        logData.put("data", java.util.Map.of("messageId", message.getMessageId() != null
                            ? message.getMessageId()
                            : "NULL"));
                        logData.put("timestamp", System.currentTimeMillis());
                        java.nio.file.Files.write(java.nio.file.Paths
                            .get("d:\\java_projects\\code_style_template\\codestyle\\codestyle-admin\\.cursor\\debug.log"), (new com.fasterxml.jackson.databind.ObjectMapper()
                                .writeValueAsString(logData) + "\n")
                                .getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
                    } catch (Exception e) {
                    }
                    // #endregion
                    esBulkSyncService.processDataChangeMessage(message);
                    // #region agent log
                    try {
                        java.util.Map<String, Object> logData = new java.util.HashMap<>();
                        logData.put("sessionId", "debug-session");
                        logData.put("runId", "run1");
                        logData.put("hypothesisId", "E");
                        logData.put("location", "CanalKafkaMessageConsumer:AFTER_ES_SYNC");
                        logData.put("message", "ES同步完成");
                        logData.put("data", java.util.Map.of("messageId", message.getMessageId() != null
                            ? message.getMessageId()
                            : "NULL"));
                        logData.put("timestamp", System.currentTimeMillis());
                        java.nio.file.Files.write(java.nio.file.Paths
                            .get("d:\\java_projects\\code_style_template\\codestyle\\codestyle-admin\\.cursor\\debug.log"), (new com.fasterxml.jackson.databind.ObjectMapper()
                                .writeValueAsString(logData) + "\n")
                                .getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
                    } catch (Exception e) {
                    }
                    // #endregion
                    log.info("消息处理完成，已同步到ES: messageId={}", message.getMessageId());

                    // 3. 标记消息为已处理
                    idempotencyService.markAsProcessed(message);
                    // #region agent log
                    try {
                        java.util.Map<String, Object> logData = new java.util.HashMap<>();
                        logData.put("sessionId", "debug-session");
                        logData.put("runId", "run1");
                        logData.put("hypothesisId", "E");
                        logData.put("location", "CanalKafkaMessageConsumer:MARK_PROCESSED");
                        logData.put("message", "标记消息为已处理");
                        logData.put("data", java.util.Map.of("messageId", message.getMessageId() != null
                            ? message.getMessageId()
                            : "NULL"));
                        logData.put("timestamp", System.currentTimeMillis());
                        java.nio.file.Files.write(java.nio.file.Paths
                            .get("d:\\java_projects\\code_style_template\\codestyle\\codestyle-admin\\.cursor\\debug.log"), (new com.fasterxml.jackson.databind.ObjectMapper()
                                .writeValueAsString(logData) + "\n")
                                .getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
                    } catch (Exception e) {
                    }
                    // #endregion
                    successCount++;
                    metricsService.recordSuccess(message);

                    log.debug("Successfully processed message: messageId={}, operation={}, table={}.{}", message
                        .getMessageId(), message.getOperationType(), message.getDatabase(), message.getTable());

                } catch (RetryableException e) {
                    // 可重试异常
                    log.warn("Retryable error processing message: messageId={}, error={}", message.getMessageId(), e
                        .getMessage());
                    failureCount++;
                    metricsService.recordFailure();
                    sendToDeadLetterQueue(message, e, true);

                } catch (Exception e) {
                    // 不可重试异常
                    log.error("Error processing message: messageId={}, operation={}, table={}.{}", message
                        .getMessageId(), message.getOperationType(), message.getDatabase(), message.getTable(), e);
                    failureCount++;
                    metricsService.recordFailure();
                    sendToDeadLetterQueue(message, e, false);
                }
            }

            // 手动提交offset
            if (acknowledgment != null) {
                log.info("准备提交Kafka offset: total={}, success={}, duplicate={}, failure={}", messages
                    .size(), successCount, duplicateCount, failureCount);
                acknowledgment.acknowledge();
                log.info("Kafka offset提交成功");
            } else {
                log.warn("Acknowledgment为null，无法提交offset！");
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
     */
    private void sendToDeadLetterQueue(DataChangeMessage message, Exception exception, boolean retryable) {
        try {
            String dlqTopic = kafkaConfigProperties.getDeadLetterTopic();
            String key = message.getMessageId();

            DataChangeMessage dlqMessage = new DataChangeMessage();
            dlqMessage.setMessageId(message.getMessageId() + ".dlq");
            dlqMessage.setOperationType(message.getOperationType());
            dlqMessage.setDatabase(message.getDatabase());
            dlqMessage.setTable(message.getTable());
            dlqMessage.setPrimaryKey(message.getPrimaryKey());
            dlqMessage.setAfterData(message.getAfterData());
            dlqMessage.setBeforeData(message.getBeforeData());
            dlqMessage.setTimestamp(System.currentTimeMillis());

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
