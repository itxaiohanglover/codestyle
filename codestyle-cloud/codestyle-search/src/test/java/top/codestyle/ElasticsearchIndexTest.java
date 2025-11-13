package top.codestyle;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
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
import top.codestyle.entity.es.CodeStyleTemplate;
import top.codestyle.generator.MockDataGenerator;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Elasticsearch测试工具类，用于创建索引、插入测试数据和清理数据
 */
@Slf4j
public class ElasticsearchIndexTest {

    private static final String INDEX_NAME = "template_index";
    private static ElasticsearchClient client;
    private static MockDataGenerator mockDataGenerator;

    @BeforeAll
    public static void setUp() {
        // 创建Elasticsearch客户端
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http")
        ).build();

        RestClientTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );

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
        // 检查索引是否已存在
        boolean exists = client.indices().exists(new ExistsRequest.Builder()
                .index(INDEX_NAME)
                .build()).value();

        if (!exists) {
            // 创建索引
            client.indices().create(new CreateIndexRequest.Builder()
                    .index(INDEX_NAME)
                    .settings(builder -> builder.numberOfShards(String.valueOf(10)).numberOfReplicas(String.valueOf(1))
                            .refreshInterval(refresh -> refresh.time("1s"))
                    )
                    .mappings(m -> m
                            .properties("groupId", p -> p.keyword(k -> k.store(true)))
                            .properties("artifactId", p -> p.keyword(k -> k.store(true)))
                            .properties("filePath", p -> p.keyword(k -> k))
                            .properties("version", p -> p.keyword(k -> k))
                            .properties("fileName", p -> p.text(t -> t.analyzer("ik_max_word")))
                            .properties("projectDescription", p -> p.text(t -> t.analyzer("ik_max_word")))
                            .properties("description", p -> p.text(t -> t.analyzer("ik_max_word")))
                            .properties("fileId", p -> p.keyword(k -> k))
                    )
                    .build());
            log.info("索引 {} 创建成功", INDEX_NAME);
        } else {
            log.info("索引 {} 已存在", INDEX_NAME);
        }
    }

    /**
     * 插入测试数据
     * @param count 要插入的数据条数
     */
    public static void insertTestData(int count) throws IOException {
        if (count <= 0) {
            log.warn("数据条数必须大于0");
            return;
        }

        log.info("开始生成 {} 条测试数据", count);
        List<CodeStyleTemplate> templates = mockDataGenerator.generateBatchTemplates(count);
        log.info("测试数据生成完成");

        // 使用bulk API批量插入数据
        BulkRequest.Builder bulk = new BulkRequest.Builder();

        for (CodeStyleTemplate template : templates) {
            bulk.operations(op -> op
                    .index(idx -> idx
                            .index(INDEX_NAME)
                            .id(template.getFileId())
                            .document(template)
                    )
            );
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
     * 并行插入大量测试数据
     * @param count 要插入的数据条数
     * @param threadCount 并行线程数
     */
    public static void insertTestDataParallel(int count, int threadCount) throws Exception {
        if (count <= 0) {
            log.warn("数据条数必须大于0");
            return;
        }

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
                    log.info("线程 {} 开始生成 {} 条数据", threadIndex, currentBatchSize);
                    List<CodeStyleTemplate> templates = mockDataGenerator.generateBatchTemplates(currentBatchSize);
                    
                    BulkRequest.Builder bulk = new BulkRequest.Builder();
                    for (CodeStyleTemplate template : templates) {
                        bulk.operations(op -> op
                                .index(idx -> idx
                                        .index(INDEX_NAME)
                                        .id(template.getFileId())
                                        .document(template)
                                )
                        );
                    }

                    BulkResponse result = client.bulk(bulk.build());
                    successCount.addAndGet(currentBatchSize);
                    log.info("线程 {} 完成，成功插入 {} 条数据", threadIndex, currentBatchSize);
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
        
        log.info("并行插入完成，成功: {}, 失败: {}", successCount.get(), errorCount.get());
    }

    /**
     * 删除所有测试数据
     */
    public static void deleteAllTestData() throws IOException {
        // 使用delete_by_query删除所有数据
        client.deleteByQuery(new DeleteByQueryRequest.Builder()
                .index(INDEX_NAME)
                .query(q -> q.matchAll(m -> m))
                .build());
        log.info("已删除索引 {} 中的所有数据", INDEX_NAME);
    }

    /**
     * 删除整个索引
     */
    public static void deleteIndex() throws IOException {
        // 检查索引是否存在
        boolean exists = client.indices().exists(new ExistsRequest.Builder()
                .index(INDEX_NAME)
                .build()).value();

        if (exists) {
            // 删除索引
            client.indices().delete(new DeleteIndexRequest.Builder()
                    .index(INDEX_NAME)
                    .build());
            log.info("已删除索引 {}", INDEX_NAME);
        } else {
            log.info("索引 {} 不存在", INDEX_NAME);
        }
    }

    /**
     * 测试方法 - 创建索引并插入指定数量的测试数据
     */
    @Test
    public void testCreateIndexAndInsertData() throws IOException {
        // 创建索引
        createIndex();
        
        // 插入10条测试数据
        insertTestData(1000);
        
        // 注意：取消注释下面的代码可以在测试完成后清理数据
        // deleteAllTestData();
        // deleteIndex();
    }


}
