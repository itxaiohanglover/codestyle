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
 * 检索模块配置
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "search")
public class SearchProperties {

    /**
     * 是否启用检索模块
     */
    private Boolean enabled = true;

    /**
     * Elasticsearch 配置
     */
    private ElasticsearchProperties elasticsearch = new ElasticsearchProperties();

    /**
     * Milvus 配置
     */
    private MilvusProperties milvus = new MilvusProperties();

    /**
     * 混合检索配置
     */
    private HybridProperties hybrid = new HybridProperties();

    /**
     * 重排配置
     */
    private RerankProperties rerank = new RerankProperties();

    /**
     * 缓存配置
     */
    private CacheProperties cache = new CacheProperties();

    @Data
    public static class ElasticsearchProperties {
        private Boolean enabled = true;
        private String hosts = "localhost:9200";
        private String username;
        private String password;
        private String index = "codestyle_templates";
    }

    @Data
    public static class MilvusProperties {
        private Boolean enabled = false;
        private String host = "localhost";
        private Integer port = 19530;
        private String collection = "codestyle_templates";
        private Integer dimension = 1024;
    }

    @Data
    public static class HybridProperties {
        private Boolean enabled = true;
        private String fusionStrategy = "RRF";
    }

    @Data
    public static class RerankProperties {
        private Boolean enabled = false;
        private String provider = "BGE";
        private String apiUrl = "http://localhost:8001/rerank";
        private String model = "BAAI/bge-reranker-v2-m3";
        private Integer topK = 10;
    }

    @Data
    public static class CacheProperties {
        private Boolean enabled = true;
        private LocalProperties local = new LocalProperties();
        private RedisProperties redis = new RedisProperties();

        @Data
        public static class LocalProperties {
            private Integer maxSize = 1000;
            private Long ttl = 300L; // 5 分钟
        }

        @Data
        public static class RedisProperties {
            private Long ttl = 3600L; // 1 小时
        }
    }
}
