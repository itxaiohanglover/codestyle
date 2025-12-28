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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;

import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ES批量处理器配置类
 * 使用Spring Data Elasticsearch实现批量操作，替代已废弃的RestHighLevelClient
 */
@Slf4j
@Configuration
@ConditionalOnClass(name = "org.springframework.data.elasticsearch.core.ElasticsearchOperations")
public class BulkProcessorConfig {

    private CustomBulkProcessor customBulkProcessor;

    /**
     * 创建自定义批量处理器包装类
     * 通过方法参数注入ElasticsearchOperations，避免循环依赖问题
     * 
     * @param elasticsearchOperations Elasticsearch操作接口，由Spring Data Elasticsearch自动配置提供
     * @param bulkProcessorProperties 批量处理器配置属性
     * @return 自定义批量处理器实例
     */
    @Bean
    public CustomBulkProcessor customBulkProcessor(ElasticsearchOperations elasticsearchOperations,
                                                   BulkProcessorProperties bulkProcessorProperties) {
        this.customBulkProcessor = new CustomBulkProcessor(elasticsearchOperations, bulkProcessorProperties);
        return this.customBulkProcessor;
    }

    /**
     * 应用关闭时关闭批量处理器
     */
    @PreDestroy
    public void closeBulkProcessor() {
        if (this.customBulkProcessor != null) {
            try {
                this.customBulkProcessor.flush();
                log.info("ES批量处理器关闭成功");
            } catch (Exception e) {
                log.error("关闭ES批量处理器时出错: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 自定义批量处理器包装类
     * 提供更友好的API和线程安全保证
     * 使用Spring Data Elasticsearch的批量操作API
     */
    @Data
    public static class CustomBulkProcessor {
        private final ElasticsearchOperations elasticsearchOperations;
        private final BulkProcessorProperties properties;
        private final ReentrantLock lock = new ReentrantLock();

        /**
         * 带索引名的IndexQuery包装类
         * 用于在队列中存储索引名和IndexQuery的关联关系
         */
        @Getter
        private static class IndexQueryWithIndex {
            private final IndexQuery indexQuery;
            private final String indexName;

            public IndexQueryWithIndex(IndexQuery indexQuery, String indexName) {
                this.indexQuery = indexQuery;
                this.indexName = indexName;
            }

        }

        // 批量操作队列，存储带索引名的IndexQuery
        private final BlockingQueue<IndexQueryWithIndex> indexQueue = new LinkedBlockingQueue<>();

        // 批量计数器
        private final AtomicInteger batchCounter = new AtomicInteger(0);

        // 最后刷新时间
        private volatile long lastFlushTime = System.currentTimeMillis();

        public CustomBulkProcessor(ElasticsearchOperations elasticsearchOperations,
                                   BulkProcessorProperties properties) {
            this.elasticsearchOperations = elasticsearchOperations;
            this.properties = properties;
        }

        /**
         * 添加单个文档到批量处理器
         * 
         * @param indexQuery 索引查询对象
         * @param indexName  索引名
         */
        public void add(IndexQuery indexQuery, String indexName) {
            if (indexQuery == null || indexName == null || indexName.isEmpty()) {
                log.warn("索引查询对象或索引名不能为空，忽略添加");
                return;
            }

            try {
                lock.lock();
                indexQueue.offer(new IndexQueryWithIndex(indexQuery, indexName));

                // 检查是否需要刷新
                int count = batchCounter.incrementAndGet();
                long currentTime = System.currentTimeMillis();
                boolean shouldFlush = count >= properties
                    .getBulkActions() || (currentTime - lastFlushTime) >= properties.getFlushIntervalMs();

                if (shouldFlush) {
                    flushInternal();
                }
            } catch (Exception e) {
                log.error("添加文档到批量处理器失败: {}", e.getMessage(), e);
                throw new RuntimeException("添加文档到批量处理器失败", e);
            } finally {
                lock.unlock();
            }
        }

        /**
         * 添加单个文档到批量处理器（使用Map数据）
         * 
         * @param data       文档数据
         * @param indexName  索引名
         * @param documentId 文档ID
         */
        public void add(Map<String, Object> data, String indexName, String documentId) {
            if (data == null || indexName == null || indexName.isEmpty()) {
                log.warn("文档数据或索引名不能为空，忽略添加");
                return;
            }

            // 创建IndexQuery对象
            // 注意：withSource接受String类型，需要使用JSON序列化
            IndexQuery indexQuery = new IndexQuery();
            indexQuery.setId(documentId);
            indexQuery.setObject(data);  // 使用setObject方法设置Map数据

            // 添加到队列
            add(indexQuery, indexName);
        }

        /**
         * 主动刷新批量处理器中的所有操作
         */
        public void flush() {
            try {
                lock.lock();
                flushInternal();
            } catch (Exception e) {
                log.error("刷新批量处理器失败: {}", e.getMessage(), e);
                throw new RuntimeException("刷新批量处理器失败", e);
            } finally {
                lock.unlock();
            }
        }

        /**
         * 内部刷新方法（需要在锁内调用）
         */
        private void flushInternal() {
            if (indexQueue.isEmpty()) {
                return;
            }

            List<IndexQueryWithIndex> queriesWithIndex = new ArrayList<>();
            int drained = indexQueue.drainTo(queriesWithIndex, properties.getBulkActions());

            if (drained == 0) {
                return;
            }

            try {
                // 按索引名分组处理
                Map<String, List<IndexQuery>> queriesByIndex = new java.util.HashMap<>();
                for (IndexQueryWithIndex queryWithIndex : queriesWithIndex) {
                    String indexName = queryWithIndex.getIndexName();
                    IndexQuery indexQuery = queryWithIndex.getIndexQuery();
                    queriesByIndex.computeIfAbsent(indexName, k -> new ArrayList<>()).add(indexQuery);
                }

                // 对每个索引执行批量操作
                for (Map.Entry<String, List<IndexQuery>> entry : queriesByIndex.entrySet()) {
                    String indexName = entry.getKey();
                    List<IndexQuery> indexQueries = entry.getValue();

                    log.debug("执行ES批量操作，索引: {}, 文档数量: {}", indexName, indexQueries.size());

                    elasticsearchOperations.bulkIndex(indexQueries, IndexCoordinates.of(indexName));

                    log.debug("ES批量操作成功，索引: {}, 文档数量: {}", indexName, indexQueries.size());
                }

                batchCounter.set(0);
                lastFlushTime = System.currentTimeMillis();

            } catch (Exception e) {
                log.error("ES批量操作失败: {}", e.getMessage(), e);
                // 将失败的查询重新放回队列（可选，根据业务需求决定）
                // indexQueue.addAll(queriesWithIndex);
                throw new RuntimeException("ES批量操作失败", e);
            }
        }
    }
}