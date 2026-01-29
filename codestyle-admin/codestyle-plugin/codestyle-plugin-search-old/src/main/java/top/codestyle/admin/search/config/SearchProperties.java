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

package top.codestyle.admin.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 搜索配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "codestyle.search")
public class SearchProperties {

    /**
     * 是否启用搜索功能
     */
    private Boolean enabled = true;

    /**
     * 缓存过期时间（秒）
     */
    private Integer cacheExpire = 300;

    /**
     * 热门键缓存过期时间（秒）
     */
    private Integer hotKeyCacheExpire = 14400; // 默认4小时

    /**
     * 最大缓存大小
     */
    private Integer maxCacheSize = 1000;

    /**
     * 热点数据识别配置
     */
    private HotKeyConfig hotKey = new HotKeyConfig();

    /**
     * 缓存预热配置
     */
    private WarmupConfig warmup = new WarmupConfig();

    /**
     * 批处理大小
     */
    private Integer batchSize = 100;

    /**
     * 搜索查询配置
     */
    private QueryConfig query = new QueryConfig();

    /**
     * 聚合配置
     */
    private AggregationConfig aggregation = new AggregationConfig();

    /**
     * 批量同步线程池配置
     */
    private ThreadPoolConfig threadPool = new ThreadPoolConfig();

    /**
     * 查询配置
     */
    @Data
    public static class QueryConfig {
        /**
         * 最大返回结果数
         */
        private Integer maxResults = 1;

        /**
         * multi_match查询类型
         * 可选值：best_fields, most_fields, cross_fields, phrase, phrase_prefix
         */
        private String multiMatchType = "cross_fields";

        /**
         * 字段权重配置
         */
        private FieldWeights fieldWeights = new FieldWeights();
    }

    /**
     * 字段权重配置
     */
    @Data
    public static class FieldWeights {
        /**
         * groupId字段权重（对应配置中的group-id）
         */
        private Float groupId = 3.0f;

        /**
         * artifactId字段权重（对应配置中的artifact-id）
         */
        private Float artifactId = 2.0f;

        /**
         * description字段权重
         */
        private Float description = 1.0f;
    }

    /**
     * 聚合配置
     */
    @Data
    public static class AggregationConfig {
        /**
         * groupId聚合配置（对应配置中的group-ids）
         */
        private TermsAggregationConfig groupIds = new TermsAggregationConfig();

        /**
         * artifactId聚合配置（对应配置中的artifact-ids）
         */
        private TermsAggregationConfig artifactIds = new TermsAggregationConfig();
    }

    /**
     * Terms聚合配置
     */
    @Data
    public static class TermsAggregationConfig {
        /**
         * 聚合桶数量
         */
        private Integer size = 1000;

        /**
         * 聚合字段名（包含.keyword后缀）
         */
        private String field;
    }

    /**
     * 热点数据识别配置
     */
    @Data
    public static class HotKeyConfig {
        /**
         * 热点判定阈值（1小时内访问次数）
         */
        private Long threshold = 100L;

        /**
         * 统计时间窗口（秒）
         */
        private Long windowSeconds = 3600L; // 1小时
    }

    /**
     * 缓存预热配置
     */
    @Data
    public static class WarmupConfig {
        /**
         * 是否启用缓存预热
         */
        private Boolean enabled = true;

        /**
         * 预热的热点关键词数量
         */
        private Integer count = 100;

        /**
         * 定时预热间隔（秒）
         */
        private Long intervalSeconds = 1800L; // 30分钟
    }

    /**
     * 批量同步线程池配置
     */
    @Data
    public static class ThreadPoolConfig {
        /**
         * 核心线程数
         */
        private Integer corePoolSize = 2;

        /**
         * 最大线程数
         */
        private Integer maximumPoolSize = 4;

        /**
         * 队列容量（有界队列，避免内存溢出）
         */
        private Integer queueCapacity = 100;

        /**
         * 线程存活时间（秒）
         */
        private Long keepAliveSeconds = 60L;

        /**
         * 线程名前缀
         */
        private String threadNamePrefix = "search-sync-";

        /**
         * 等待任务完成超时时间（分钟）
         */
        private Long awaitTerminationMinutes = 5L;
    }
}