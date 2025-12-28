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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Kafka配置属性类，用于绑定Kafka相关配置
 */
@Component
@Data
@ConfigurationProperties(prefix = "kafka")
public class KafkaConfigProperties {

    /**
     * 是否启用Kafka
     */
    private boolean enabled = true;

    /**
     * Kafka服务器地址
     * 配置路径：kafka.bootstrap-servers
     * 注意：宿主机应用使用9094端口（EXTERNAL listener），Docker内部使用9092
     */
    private String bootstrapServers = "localhost:9094";

    /**
     * Acknowledgment模式
     */
    private String acks = "1";

    /**
     * 重试次数
     */
    private int retries = 3;

    /**
     * 批量大小
     */
    private int batchSize = 16384;

    /**
     * 延迟时间（毫秒）
     */
    private int lingerMs = 1;

    /**
     * 缓冲区大小
     */
    private int bufferMemory = 33554432;

    /**
     * 数据变更主题
     * 配置路径：kafka.data-change-topic
     */
    private String dataChangeTopic = "data-change";

    /**
     * 消费者组ID
     */
    private String groupId = "codestyle-search";

    /**
     * 死信队列主题
     */
    private String deadLetterTopic = "search.data.change.dlq";

    /**
     * 批量消费大小
     */
    private int maxPollRecords = 500;

    /**
     * 消费者并发数
     */
    private int concurrency = 3;

}
