package top.codestyle;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.es.template.model.CodeStyleTemplate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Elasticsearch集成测试类，测试索引创建、数据插入和查询功能
 */
@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ElasticsearchIntegrationTest {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private static final String INDEX_NAME = "template_index";
    private static final int TEST_DATA_COUNT = 50; // 测试数据条数

    @BeforeAll
    public static void setUpTestEnvironment() throws IOException {
        log.info("开始设置测试环境...");
        
        // 创建索引
        ElasticsearchIndexTest.createIndex();
        
        // 插入测试数据
        ElasticsearchIndexTest.insertTestData(TEST_DATA_COUNT);
        
        log.info("测试环境设置完成");
    }

    @AfterAll
    public static void cleanUpTestEnvironment() throws IOException {
        // 注意：根据需要选择清理方式
        // 方式1：删除所有测试数据，但保留索引结构
        // ElasticsearchTestUtils.deleteAllTestData();
        
        // 方式2：删除整个索引（默认使用这种方式）
        ElasticsearchIndexTest.deleteIndex();
        
        log.info("测试环境清理完成");
    }

    @Test
    @Order(1)
    public void testIndexExists() throws IOException {
        log.info("测试索引是否存在");
        boolean exists = elasticsearchClient.indices().exists(b -> b.index(INDEX_NAME)).value();
        assertTrue(exists, "索引应该存在");
    }

    @Test
    @Order(2)
    public void testDataCount() throws IOException {
        log.info("测试数据条数");
        long count = elasticsearchClient.count(b -> b.index(INDEX_NAME)).count();
        assertTrue(count >= TEST_DATA_COUNT, "数据条数应该至少为" + TEST_DATA_COUNT);
        log.info("实际数据条数: {}", count);
    }

    @Test
    @Order(3)
    public void testSearchFunctionality() throws IOException {
        log.info("测试搜索功能");
        // 执行搜索
        SearchResponse<CodeStyleTemplate> response = elasticsearchClient.search(b -> b
                .index(INDEX_NAME)
                .query(q -> q
                        .match(m -> m
                                .field("projectDescription")
                                .query("微服务")
                        )
                )
                .size(10),
                CodeStyleTemplate.class
        );

        // 验证搜索结果
        List<Hit<CodeStyleTemplate>> hits = response.hits().hits();
        log.info("搜索到 {} 条结果", hits.size());
        
        // 至少应该有一些结果
        assertFalse(hits.isEmpty(), "搜索应该返回至少一条结果");
        
        // 打印部分搜索结果
        hits.forEach(hit -> {
            log.info("搜索结果: fileId={}, projectDescription={}", 
                    hit.source().getFileId(), 
                    hit.source().getProjectDescription());
        });
    }

    @Test
    @Order(4)
    public void testKeywordSearch() throws IOException {
        log.info("测试精确关键词搜索");
        // 执行精确关键词搜索
        SearchResponse<CodeStyleTemplate> response = elasticsearchClient.search(b -> b
                .index(INDEX_NAME)
                .query(q -> q
                        .term(t -> t
                                .field("groupId")
                                .value("com.example")
                        )
                )
                .size(20),
                CodeStyleTemplate.class
        );

        List<Hit<CodeStyleTemplate>> hits = response.hits().hits();
        log.info("关键词搜索到 {} 条结果", hits.size());
        
        // 验证所有结果的groupId都为com.example
        hits.forEach(hit -> {
            assertEquals("com.example", hit.source().getGroupId(), "groupId应该为com.example");
        });
    }

    @Test
    @Order(5)
    public void testComplexSearch() throws IOException {
        log.info("测试复杂查询");
        // 复合查询：同时满足多个条件
        SearchResponse<CodeStyleTemplate> response = elasticsearchClient.search(b -> b
                .index(INDEX_NAME)
                .query(q -> q
                        .bool(bq -> bq
                                .must(m1 -> m1
                                        .term(t -> t
                                                .field("artifactId")
                                                .value("core")
                                        )
                                )
                                .should(s -> s
                                        .match(m -> m
                                                .field("description")
                                                .query("服务")
                                        )
                                )
                        )
                )
                .size(15),
                CodeStyleTemplate.class
        );

        List<Hit<CodeStyleTemplate>> hits = response.hits().hits();
        log.info("复杂查询搜索到 {} 条结果", hits.size());
        
        // 验证所有结果的artifactId都为core
        hits.forEach(hit -> {
            assertEquals("core", hit.source().getArtifactId(), "artifactId应该为core");
        });
    }

    /**
     * 动态配置属性源，用于测试环境
     */
    @DynamicPropertySource
    static void elasticsearchProperties(DynamicPropertyRegistry registry) {
        // 这里可以覆盖默认配置，如果需要使用不同的Elasticsearch实例
        // registry.add("spring.elasticsearch.uris", () -> "http://localhost:9200");
    }
}
