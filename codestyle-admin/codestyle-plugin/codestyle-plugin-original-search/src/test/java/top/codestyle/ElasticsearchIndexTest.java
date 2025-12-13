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

package top.codestyle;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import top.codestyle.generator.MockDataGenerator;
import top.codestyle.pojo.entity.CodeStyleTemplateDO;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Elasticsearch测试工具类（更新版）
 */
@Slf4j
public class ElasticsearchIndexTest {

    private static final String INDEX_NAME = "template_index";
    private static ElasticsearchClient client;
    private static MockDataGenerator mockDataGenerator;

    @BeforeAll
    public static void setUp() {
        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();

        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        client = new ElasticsearchClient(transport);
        mockDataGenerator = new MockDataGenerator();
    }

    @AfterAll
    public static void tearDown() {
        if (client != null) {
            try {
                client._transport().close();
            } catch (IOException e) {
                log.error("关闭Elasticsearch客户端失败", e);
            }
        }
    }

    /**
     * 创建索引
     */
    public static void createIndex() throws IOException {
        boolean exists = client.indices().exists(new ExistsRequest.Builder().index(INDEX_NAME).build()).value();

        if (!exists) {
            client.indices()
                .create(new CreateIndexRequest.Builder().index(INDEX_NAME)
                    .withJson(new FileInputStream("src/main/resources/elasticsearch/template-settings.json"))
                    .build());
            log.info("索引 {} 创建成功", INDEX_NAME);
        } else {
            log.info("索引 {} 已存在", INDEX_NAME);
        }
    }

    /**
     * 单线程批量插入
     */
    public static void insertTestData(int count) throws IOException {
        if (count <= 0)
            return;

        List<CodeStyleTemplateDO> templates = mockDataGenerator.generateBatchTemplates(count);
        BulkRequest.Builder bulk = new BulkRequest.Builder();

        for (CodeStyleTemplateDO template : templates) {
            bulk.operations(op -> op.index(idx -> idx.index(INDEX_NAME).id(template.getId()).document(template)));
        }

        BulkResponse result = client.bulk(bulk.build());

        if (result.errors()) {
            log.error("批量插入数据失败");
            result.items().forEach(item -> {
                if (item.error() != null) {
                    log.error("插入失败: {} - {}", item.id(), item.error().reason());
                }
            });
        } else {
            log.info("成功插入 {} 条测试数据", templates.size());
        }
    }

    /**
     * 并行批量插入（修复 bulk 未执行的问题）
     */
    public static void insertTestDataParallel(int count, int threadCount) throws Exception {
        if (count <= 0)
            return;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        int batchSize = count / threadCount;
        int remainder = count % threadCount;

        for (int i = 0; i < threadCount; i++) {
            final int currentBatchSize = (i < remainder) ? batchSize + 1 : batchSize;
            final int threadIndex = i;

            executorService.submit(() -> {
                try {
                    List<CodeStyleTemplateDO> templates = mockDataGenerator.generateBatchTemplates(currentBatchSize);

                    BulkRequest.Builder bulk = new BulkRequest.Builder();
                    for (CodeStyleTemplateDO template : templates) {
                        bulk.operations(op -> op.index(idx -> idx.index(INDEX_NAME)
                            .id(template.getId())
                            .document(template)));
                    }

                    // ***修复：真正执行 bulk 插入***
                    BulkResponse result = client.bulk(bulk.build());

                    if (result.errors()) {
                        errorCount.addAndGet(currentBatchSize);
                        result.items().forEach(item -> {
                            if (item.error() != null) {
                                log.error("[线程 {}] 插入失败 {} - {}", threadIndex, item.id(), item.error().reason());
                            }
                        });
                    } else {
                        successCount.addAndGet(currentBatchSize);
                        log.info("[线程 {}] 成功插入 {}", threadIndex, currentBatchSize);
                    }

                } catch (Exception e) {
                    errorCount.addAndGet(currentBatchSize);
                    log.error("线程 {} 执行失败", threadIndex, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
        log.info("并行插入完成，总成功: {}, 总失败: {}", successCount.get(), errorCount.get());
    }

    /**
     * 删除所有数据
     */
    public static void deleteAllTestData() throws IOException {
        client.deleteByQuery(d -> d.index(INDEX_NAME).query(q -> q.matchAll(m -> m)));
        log.info("已删除索引 {} 中的所有数据", INDEX_NAME);
    }

    /**
     * 删除索引
     */
    public static void deleteIndex() throws IOException {
        boolean exists = client.indices().exists(new ExistsRequest.Builder().index(INDEX_NAME).build()).value();

        if (exists) {
            client.indices().delete(new DeleteIndexRequest.Builder().index(INDEX_NAME).build());
            log.info("已删除索引 {}", INDEX_NAME);
        } else {
            log.info("索引 {} 不存在", INDEX_NAME);
        }
    }

    /**
     * 主测试方法
     */
    @Test
    public void testCreateInsertAndDeleteData() throws Exception {
        createTestIndex();
        //        clearCurrentIndex();
    }

    private static void createTestIndex() throws Exception {
        createIndex();
        insertTestDataParallel(1000, 4);
    }

    private static void clearCurrentIndex() throws IOException {
        deleteAllTestData();
        deleteIndex();
    }
}
