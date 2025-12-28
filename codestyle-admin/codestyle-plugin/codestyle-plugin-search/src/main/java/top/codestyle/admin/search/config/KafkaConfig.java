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

package top.codestyle.admin.search.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import top.codestyle.admin.search.model.dto.DataChangeMessage;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Kafka配置类，配置生产者和消费者
 * 兼容Kafka 4.0.1版本
 * 
 * @author ChonghaoGao
 * @date 2025/12/22
 */
@Slf4j
@Configuration
@AllArgsConstructor
@EnableConfigurationProperties(KafkaConfigProperties.class)
@ConditionalOnClass(name = "org.springframework.kafka.core.ConsumerFactory")
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {

    private final KafkaConfigProperties kafkaConfigProperties;

    @jakarta.annotation.PostConstruct
    public void init() {
        // #region agent log
        try {
            java.util.Map<String, Object> logData = new java.util.HashMap<>();
            logData.put("sessionId", "debug-session");
            logData.put("runId", "run1");
            logData.put("hypothesisId", "B");
            logData.put("location", "KafkaConfig.init");
            logData.put("message", "KafkaConfig初始化");
            logData.put("data", java.util.Map.of("bootstrapServers", kafkaConfigProperties.getBootstrapServers() != null
                ? kafkaConfigProperties.getBootstrapServers()
                : "NULL", "groupId", kafkaConfigProperties.getGroupId() != null
                    ? kafkaConfigProperties.getGroupId()
                    : "NULL", "enabled", kafkaConfigProperties.isEnabled()));
            logData.put("timestamp", System.currentTimeMillis());
            java.nio.file.Files.write(java.nio.file.Paths
                .get("d:\\java_projects\\code_style_template\\codestyle\\codestyle-admin\\.cursor\\debug.log"), (new ObjectMapper()
                    .writeValueAsString(logData) + "\n")
                    .getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception e) {
        }
        // #endregion
        log.info("========================================");
        log.info("KafkaConfig 初始化完成");
        log.info("Bootstrap Servers: {}", kafkaConfigProperties.getBootstrapServers());
        log.info("Group ID: {}", kafkaConfigProperties.getGroupId());
        log.info("Data Change Topic: {}", kafkaConfigProperties.getDataChangeTopic());
        log.info("Max Poll Records: {}", kafkaConfigProperties.getMaxPollRecords());
        log.info("Concurrency: {}", kafkaConfigProperties.getConcurrency());
        log.info("========================================");
    }

    /**
     * 配置Kafka生产者工厂
     * 兼容Kafka 4.0.1版本
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        String bootstrapServers = kafkaConfigProperties.getBootstrapServers();
        // #region agent log
        try {
            java.util.Map<String, Object> logData = new java.util.HashMap<>();
            logData.put("sessionId", "debug-session");
            logData.put("runId", "run1");
            logData.put("hypothesisId", "B");
            logData.put("location", "KafkaConfig.producerFactory:ENTRY");
            logData.put("message", "开始创建ProducerFactory");
            logData.put("data", java.util.Map.of("bootstrapServers", bootstrapServers != null
                ? bootstrapServers
                : "NULL"));
            logData.put("timestamp", System.currentTimeMillis());
            java.nio.file.Files.write(java.nio.file.Paths
                .get("d:\\java_projects\\code_style_template\\codestyle\\codestyle-admin\\.cursor\\debug.log"), (new ObjectMapper()
                    .writeValueAsString(logData) + "\n")
                    .getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception e) {
        }
        // #endregion

        // #region agent log - 测试 Kafka 连接性
        if (bootstrapServers != null) {
            try {
                String[] servers = bootstrapServers.split(",");
                for (String server : servers) {
                    String[] parts = server.trim().split(":");
                    String host = parts[0];
                    int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9092;
                    boolean reachable = false;
                    try (Socket socket = new Socket()) {
                        socket.connect(new InetSocketAddress(host, port), 3000);
                        reachable = true;
                    } catch (Exception e) {
                        reachable = false;
                    }
                    java.util.Map<String, Object> logData = new java.util.HashMap<>();
                    logData.put("sessionId", "debug-session");
                    logData.put("runId", "run1");
                    logData.put("hypothesisId", "A");
                    logData.put("location", "KafkaConfig.producerFactory:CONNECTIVITY_TEST");
                    logData.put("message", "测试Kafka连接性");
                    logData.put("data", java.util.Map.of("host", host, "port", port, "reachable", reachable));
                    logData.put("timestamp", System.currentTimeMillis());
                    java.nio.file.Files.write(java.nio.file.Paths
                        .get("d:\\java_projects\\code_style_template\\codestyle\\codestyle-admin\\.cursor\\debug.log"), (new ObjectMapper()
                            .writeValueAsString(logData) + "\n")
                            .getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
                    log.info("Kafka连接测试: host={}, port={}, reachable={}", host, port, reachable);
                }
            } catch (Exception e) {
                log.warn("Kafka连接测试失败: {}", e.getMessage());
            }
        }
        // #endregion

        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, kafkaConfigProperties.getAcks());
        configProps.put(ProducerConfig.RETRIES_CONFIG, kafkaConfigProperties.getRetries());
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, kafkaConfigProperties.getBatchSize());
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, kafkaConfigProperties.getLingerMs());
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, kafkaConfigProperties.getBufferMemory());

        // Kafka 4.0.1版本兼容性配置
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000); // 2分钟超时
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);   // 30秒请求超时

        // #region agent log
        try {
            java.util.Map<String, Object> logData = new java.util.HashMap<>();
            logData.put("sessionId", "debug-session");
            logData.put("runId", "run1");
            logData.put("hypothesisId", "D");
            logData.put("location", "KafkaConfig.producerFactory:BEFORE_CREATE");
            logData.put("message", "准备创建DefaultKafkaProducerFactory");
            logData.put("data", java.util.Map.of("configSize", configProps.size()));
            logData.put("timestamp", System.currentTimeMillis());
            java.nio.file.Files.write(java.nio.file.Paths
                .get("d:\\java_projects\\code_style_template\\codestyle\\codestyle-admin\\.cursor\\debug.log"), (new ObjectMapper()
                    .writeValueAsString(logData) + "\n")
                    .getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception e) {
        }
        // #endregion

        DefaultKafkaProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(configProps);

        // #region agent log
        try {
            java.util.Map<String, Object> logData = new java.util.HashMap<>();
            logData.put("sessionId", "debug-session");
            logData.put("runId", "run1");
            logData.put("hypothesisId", "D");
            logData.put("location", "KafkaConfig.producerFactory:AFTER_CREATE");
            logData.put("message", "DefaultKafkaProducerFactory创建成功");
            logData.put("timestamp", System.currentTimeMillis());
            java.nio.file.Files.write(java.nio.file.Paths
                .get("d:\\java_projects\\code_style_template\\codestyle\\codestyle-admin\\.cursor\\debug.log"), (new ObjectMapper()
                    .writeValueAsString(logData) + "\n")
                    .getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception e) {
        }
        // #endregion

        return factory;
    }

    /**
     * 配置Kafka模板
     * 兼容Kafka 4.0.1版本
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * 配置Kafka消费者工厂
     * 支持批量消费和手动提交offset
     */
    @Bean
    public ConsumerFactory<String, DataChangeMessage> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigProperties.getBootstrapServers());
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfigProperties.getGroupId());
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // 手动提交
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaConfigProperties.getMaxPollRecords()); // 批量消费大小

        // JsonDeserializer配置
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, DataChangeMessage.class);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * 配置批量消费监听器容器工厂（用于DataChangeMessage对象）
     * 支持批量消费和手动提交offset
     * 用于：应用层发送的DataChangeMessage对象
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DataChangeMessage> batchKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DataChangeMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(true); // 启用批量消费
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE); // 手动提交
        factory.setConcurrency(kafkaConfigProperties.getConcurrency()); // 并发消费者数量
        return factory;
    }

    /**
     * 配置Canal Server直连Kafka模式的消费者工厂
     * Canal Server发送的是JSON字符串，需要String反序列化
     */
    @Bean
    public ConsumerFactory<String, String> canalConsumerFactory() {
        // #region agent log
        try {
            java.nio.file.Files.write(java.nio.file.Paths
                .get("d:\\java_projects\\code_style_template\\codestyle\\codestyle-admin\\.cursor\\debug.log"), (java.time.Instant
                    .now()
                    .toEpochMilli() + "|HYPOTHESIS_D|KafkaConfig.canalConsumerFactory|创建Canal消费者工厂|" + java.util.Map
                        .of("bootstrapServers", kafkaConfigProperties
                            .getBootstrapServers(), "groupId", kafkaConfigProperties
                                .getGroupId(), "maxPollRecords", kafkaConfigProperties
                                    .getMaxPollRecords(), "timestamp", System.currentTimeMillis()) + "\n")
                    .getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception e) {
        }
        // #endregion
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigProperties.getBootstrapServers());
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfigProperties.getGroupId());
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // 设置为earliest：如果没有offset，从最早的消息开始消费（确保不丢失消息）
        // 如果有offset，从上次提交的位置继续消费（这是Kafka的默认行为）
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // 手动提交
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaConfigProperties.getMaxPollRecords());
        // 确保消费者保持活跃，不会因为长时间没有消息而停止
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000); // 30秒会话超时
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000); // 10秒心跳间隔
        configProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5分钟最大轮询间隔

        log.info("Canal消费者工厂配置: bootstrapServers={}, groupId={}, maxPollRecords={}", kafkaConfigProperties
            .getBootstrapServers(), kafkaConfigProperties.getGroupId(), kafkaConfigProperties.getMaxPollRecords());
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * 配置Canal Server直连Kafka模式的监听器容器工厂
     * 用于：Canal Server发送的JSON字符串消息
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> canalKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(canalConsumerFactory());
        factory.setBatchListener(true); // 启用批量消费
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE); // 手动提交
        factory.setConcurrency(kafkaConfigProperties.getConcurrency()); // 并发消费者数量
        // 确保即使没有消息也会持续轮询
        ContainerProperties containerProps = factory.getContainerProperties();
        containerProps.setIdleEventInterval(5000L); // 5秒空闲事件间隔，用于诊断
        log.info("Canal Kafka监听器容器工厂配置完成: batchListener=true, ackMode=MANUAL_IMMEDIATE, concurrency={}", kafkaConfigProperties
            .getConcurrency());
        return factory;
    }

    /**
     * 配置死信队列主题的KafkaTemplate
     */
    @Bean("dlqKafkaTemplate")
    public KafkaTemplate<String, Object> dlqKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
