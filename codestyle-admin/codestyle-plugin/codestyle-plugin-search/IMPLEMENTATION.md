# CodeStyle 检索模块实现规划

> 详细的编码实现计划和代码示例
> 
> **版本**: 1.0.0  
> **日期**: 2026-01-29

---

## 📋 实施步骤

### 阶段一：项目初始化

#### 1. 创建模块结构

```bash
cd codestyle-admin/codestyle-plugin
mkdir -p codestyle-plugin-search/src/main/java/top/codestyle/admin/search/{spi,provider,rerank,model,service,controller,config,util}
mkdir -p codestyle-plugin-search/src/main/resources
mkdir -p codestyle-plugin-search/src/test/java/top/codestyle/admin/search
```

#### 2. pom.xml 配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>top.codestyle.admin</groupId>
        <artifactId>codestyle-plugin</artifactId>
        <version>${revision}</version>
    </parent>

    <artifactId>codestyle-plugin-search</artifactId>
    <name>${project.artifactId}</name>
    <description>检索插件（支持 ES、Milvus 多源混合检索）</description>

    <dependencies>
        <!-- 公共模块 -->
        <dependency>
            <groupId>top.codestyle.admin</groupId>
            <artifactId>codestyle-common</artifactId>
        </dependency>

        <!-- Elasticsearch -->
        <dependency>
            <groupId>co.elastic.clients</groupId>
            <artifactId>elasticsearch-java</artifactId>
            <version>8.13.0</version>
        </dependency>

        <!-- Milvus -->
        <dependency>
            <groupId>io.milvus</groupId>
            <artifactId>milvus-sdk-java</artifactId>
            <version>2.3.4</version>
        </dependency>

        <!-- Spring Boot Starter Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
    </dependencies>
</project>
```

---

### 阶段二：核心接口定义

#### 1. SearchSourceType 枚举

```java
package top.codestyle.admin.search.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 检索数据源类型
 */
@Getter
@RequiredArgsConstructor
public enum SearchSourceType {
    
    ELASTICSEARCH("Elasticsearch", "全文检索"),
    MILVUS("Milvus", "向量检索"),
    HYBRID("Hybrid", "混合检索"),
    CUSTOM("Custom", "自定义数据源");
    
    private final String code;
    private final String description;
}
```

#### 2. SearchRequest 模型

```java
package top.codestyle.admin.search.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 检索请求
 */
@Data
@Schema(description = "检索请求")
public class SearchRequest {
    
    @Schema(description = "查询文本", example = "如何配置 MySQL 连接池")
    @NotBlank(message = "查询文本不能为空")
    private String query;
    
    @Schema(description = "数据源类型")
    private SearchSourceType sourceType = SearchSourceType.HYBRID;
    
    @Schema(description = "返回结果数量", example = "10")
    private Integer topK = 10;
    
    @Schema(description = "是否启用重排", example = "true")
    private Boolean enableRerank = false;
    
    @Schema(description = "过滤条件")
    private Map<String, Object> filters;
    
    @Schema(description = "超时时间（毫秒）", example = "5000")
    private Long timeout = 5000L;
}
```

#### 3. SearchResult 模型

```java
package top.codestyle.admin.search.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * 检索结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "检索结果")
public class SearchResult {
    
    @Schema(description = "文档 ID")
    private String id;
    
    @Schema(description = "数据源类型")
    private SearchSourceType sourceType;
    
    @Schema(description = "标题")
    private String title;
    
    @Schema(description = "内容摘要")
    private String snippet;
    
    @Schema(description = "完整内容")
    private String content;
    
    @Schema(description = "相关性分数")
    private Double score;
    
    @Schema(description = "排名")
    private Integer rank;
    
    @Schema(description = "元数据")
    private Map<String, Object> metadata;
    
    @Schema(description = "高亮片段")
    private String highlight;
}
```

#### 4. SearchProvider 接口

```java
package top.codestyle.admin.search.spi;

import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;
import java.util.List;

/**
 * 检索提供者 SPI 接口
 * 参考 AssistantAgent 的 SearchProvider 设计
 */
public interface SearchProvider {
    
    /**
     * 判断是否支持指定的数据源类型
     */
    boolean supports(SearchSourceType type);
    
    /**
     * 执行检索
     */
    List<SearchResult> search(SearchRequest request);
    
    /**
     * 获取 Provider 名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * 获取优先级（数值越小优先级越高）
     */
    default int getPriority() {
        return 100;
    }
}
```

#### 5. RerankProvider 接口

```java
package top.codestyle.admin.search.spi;

import top.codestyle.admin.search.model.SearchResult;
import java.util.List;

/**
 * 重排提供者接口
 */
public interface RerankProvider {
    
    /**
     * 对检索结果进行重排序
     * 
     * @param query 查询文本
     * @param results 原始检索结果
     * @return 重排后的结果
     */
    List<SearchResult> rerank(String query, List<SearchResult> results);
    
    /**
     * 获取 Provider 名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
```

---

### 阶段三：配置类实现

#### 1. SearchProperties 配置

```java
package top.codestyle.admin.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 检索模块配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "search")
public class SearchProperties {
    
    /** 是否启用检索模块 */
    private Boolean enabled = true;
    
    /** Elasticsearch 配置 */
    private ElasticsearchProperties elasticsearch = new ElasticsearchProperties();
    
    /** Milvus 配置 */
    private MilvusProperties milvus = new MilvusProperties();
    
    /** 混合检索配置 */
    private HybridProperties hybrid = new HybridProperties();
    
    /** 重排配置 */
    private RerankProperties rerank = new RerankProperties();
    
    /** 缓存配置 */
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
        private Boolean enabled = true;
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
        private Boolean enabled = true;
        private String provider = "BGE";
        private String apiUrl = "http://localhost:8001/rerank";
        private String model = "BAAI/bge-reranker-v2-m3";
        private Integer topK = 10;
    }
    
    @Data
    public static class CacheProperties {
        private Boolean enabled = true;
        private Long ttl = 3600L;
    }
}
```

#### 2. ElasticsearchConfig 配置

```java
package top.codestyle.admin.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 配置
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true")
public class ElasticsearchConfig {
    
    private final SearchProperties searchProperties;
    
    @Bean
    public ElasticsearchClient elasticsearchClient() {
        SearchProperties.ElasticsearchProperties es = searchProperties.getElasticsearch();
        
        // 创建 RestClient
        RestClient restClient = RestClient.builder(
            HttpHost.create(es.getHosts())
        ).setHttpClientConfigCallback(httpClientBuilder -> {
            if (es.getUsername() != null && es.getPassword() != null) {
                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(es.getUsername(), es.getPassword())
                );
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
            return httpClientBuilder;
        }).build();
        
        // 创建 Transport
        RestClientTransport transport = new RestClientTransport(
            restClient,
            new JacksonJsonpMapper()
        );
        
        return new ElasticsearchClient(transport);
    }
}
```

---

### 阶段四：Provider 实现

#### 1. ElasticsearchSearchProvider

```java
package top.codestyle.admin.search.provider;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import top.codestyle.admin.search.config.SearchProperties;
import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;
import top.codestyle.admin.search.spi.SearchProvider;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch 检索提供者
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(ElasticsearchClient.class)
public class ElasticsearchSearchProvider implements SearchProvider {
    
    private final ElasticsearchClient esClient;
    private final SearchProperties searchProperties;
    
    @Override
    public boolean supports(SearchSourceType type) {
        return SearchSourceType.ELASTICSEARCH == type;
    }
    
    @Override
    public List<SearchResult> search(SearchRequest request) {
        try {
            String index = searchProperties.getElasticsearch().getIndex();
            
            // 构建 ES 查询
            SearchResponse<Document> response = esClient.search(s -> s
                .index(index)
                .query(q -> q
                    .multiMatch(m -> m
                        .query(request.getQuery())
                        .fields("title^3", "content^2", "tags")
                    )
                )
                .size(request.getTopK())
                .highlight(h -> h
                    .fields("content", f -> f
                        .preTags("<em>")
                        .postTags("</em>")
                    )
                ),
                Document.class
            );
            
            // 转换结果
            return convertToSearchResults(response);
            
        } catch (Exception e) {
            log.error("Elasticsearch 检索失败", e);
            throw new RuntimeException("Elasticsearch 检索失败", e);
        }
    }
    
    private List<SearchResult> convertToSearchResults(SearchResponse<Document> response) {
        return response.hits().hits().stream()
            .map(this::convertHit)
            .collect(Collectors.toList());
    }
    
    private SearchResult convertHit(Hit<Document> hit) {
        Document doc = hit.source();
        return SearchResult.builder()
            .id(hit.id())
            .sourceType(SearchSourceType.ELASTICSEARCH)
            .title(doc.getTitle())
            .content(doc.getContent())
            .snippet(extractSnippet(doc.getContent()))
            .score(hit.score())
            .rank(0)
            .highlight(extractHighlight(hit))
            .build();
    }
    
    private String extractSnippet(String content) {
        return content.length() > 200 ? content.substring(0, 200) + "..." : content;
    }
    
    private String extractHighlight(Hit<Document> hit) {
        if (hit.highlight() != null && hit.highlight().containsKey("content")) {
            return String.join(" ... ", hit.highlight().get("content"));
        }
        return null;
    }
    
    @lombok.Data
    public static class Document {
        private String title;
        private String content;
        private List<String> tags;
    }
}
```

---

### 阶段五：Service 实现

#### SearchService 接口

```java
package top.codestyle.admin.search.service;

import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;
import java.util.List;

/**
 * 检索服务接口
 */
public interface SearchService {
    
    /**
     * 单源检索
     */
    List<SearchResult> search(SearchSourceType type, SearchRequest request);
    
    /**
     * 混合检索
     */
    List<SearchResult> hybridSearch(SearchRequest request);
    
    /**
     * 检索并重排
     */
    List<SearchResult> searchWithRerank(SearchRequest request);
}
```

---

### 阶段六：Controller 实现

```java
package top.codestyle.admin.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.service.SearchService;
import top.continew.starter.web.model.R;

import java.util.List;

/**
 * 检索 API
 */
@Tag(name = "检索 API")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    
    private final SearchService searchService;
    
    @Operation(summary = "单源检索", description = "从指定数据源检索")
    @PostMapping("/single")
    public R<List<SearchResult>> singleSearch(@Valid @RequestBody SearchRequest request) {
        List<SearchResult> results = searchService.search(
            request.getSourceType(),
            request
        );
        return R.ok(results);
    }
    
    @Operation(summary = "混合检索", description = "融合多个数据源的检索结果")
    @PostMapping("/hybrid")
    public R<List<SearchResult>> hybridSearch(@Valid @RequestBody SearchRequest request) {
        List<SearchResult> results = searchService.hybridSearch(request);
        return R.ok(results);
    }
    
    @Operation(summary = "检索并重排", description = "检索后使用 BGE-Rerank 重排序")
    @PostMapping("/rerank")
    public R<List<SearchResult>> searchWithRerank(@Valid @RequestBody SearchRequest request) {
        List<SearchResult> results = searchService.searchWithRerank(request);
        return R.ok(results);
    }
}
```

---

## 📝 开发检查清单

### 代码规范
- [ ] 遵循 P3C 阿里巴巴编码规范
- [ ] 使用 Lombok 简化代码
- [ ] 添加完整的 Javadoc 注释
- [ ] 使用 `@Slf4j` 记录日志

### 异常处理
- [ ] 统一异常处理
- [ ] 友好的错误提示
- [ ] 记录详细的错误日志

### 性能优化
- [ ] 使用 Redis 缓存热点查询
- [ ] 并行查询多个数据源
- [ ] 设置合理的超时时间

### 测试
- [ ] 单元测试覆盖率 > 70%
- [ ] 集成测试
- [ ] 性能测试

---

**文档维护**: 开发团队  
**最后更新**: 2026-01-29

