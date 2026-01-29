# CodeStyle 检索模块设计文档

> 基于 AssistantAgent 架构的企业级多源混合检索模块
> 
> **版本**: 1.0.0  
> **日期**: 2026-01-29

---

## 📋 目录

1. [模块概述](#1-模块概述)
2. [架构设计](#2-架构设计)
3. [核心功能](#3-核心功能)
4. [技术选型](#4-技术选型)
5. [实现计划](#5-实现计划)
6. [API 设计](#6-api-设计)

---

## 1. 模块概述

### 1.1 设计目标

基于 AssistantAgent 的 SearchProvider SPI 架构，实现支持多数据源的统一检索模块：

- ✅ **多源检索**：支持 Elasticsearch、Milvus 向量数据库
- ✅ **混合检索**：关键词检索 + 向量检索 + 混合检索
- ✅ **智能重排**：BGE-Rerank 模型重排序
- ✅ **统一接口**：SPI 机制，易于扩展
- ✅ **高性能**：并行查询、结果缓存

### 1.2 参考架构

借鉴 AssistantAgent 的设计理念：

```
┌─────────────────────────────────────────────────────────────┐
│                    SearchFacade (统一入口)                    │
└─────────────────────────────────────────────────────────────┘
                            ↓
        ┌───────────────────┼───────────────────┐
        ↓                   ↓                   ↓
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ Elasticsearch│    │   Milvus     │    │   Custom     │
│  Provider    │    │   Provider   │    │   Provider   │
└──────┬───────┘    └──────┬───────┘    └──────┬───────┘
       ↓                   ↓                   ↓
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ 全文检索      │    │ 向量检索      │    │ 业务数据源    │
│ BM25 算法    │    │ 语义相似度    │    │ API 接口     │
└──────────────┘    └──────────────┘    └──────────────┘
                            ↓
                    ┌──────────────┐
                    │ BGE-Rerank   │
                    │ 重排序引擎    │
                    └──────────────┘
```

---

## 2. 架构设计

### 2.1 模块结构

```
codestyle-plugin-search/
├── src/main/java/top/codestyle/admin/search/
│   ├── spi/                          # SPI 接口定义
│   │   ├── SearchProvider.java       # 检索提供者接口
│   │   ├── RerankProvider.java       # 重排提供者接口
│   │   └── SearchFacade.java         # 检索门面接口
│   ├── provider/                     # 检索提供者实现
│   │   ├── ElasticsearchSearchProvider.java
│   │   ├── MilvusSearchProvider.java
│   │   └── HybridSearchProvider.java
│   ├── rerank/                       # 重排实现
│   │   ├── BgeRerankProvider.java
│   │   └── RerankService.java
│   ├── model/                        # 数据模型
│   │   ├── SearchRequest.java
│   │   ├── SearchResult.java
│   │   ├── SearchSourceType.java
│   │   └── RerankRequest.java
│   ├── service/                      # 业务服务
│   │   ├── SearchService.java
│   │   └── impl/SearchServiceImpl.java
│   ├── controller/                   # REST API
│   │   └── SearchController.java
│   ├── config/                       # 配置类
│   │   ├── SearchProperties.java
│   │   ├── ElasticsearchConfig.java
│   │   ├── MilvusConfig.java
│   │   └── RerankConfig.java
│   └── util/                         # 工具类
│       ├── VectorUtils.java
│       └── SearchUtils.java
├── src/main/resources/
│   ├── application-search.yml        # 模块配置
│   └── META-INF/spring.factories     # SPI 注册
└── pom.xml
```

### 2.2 核心接口设计

#### SearchProvider 接口

```java
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

#### SearchFacade 接口

```java
/**
 * 检索门面接口
 * 统一管理多个 SearchProvider
 */
public interface SearchFacade {
    
    /**
     * 单源检索
     */
    List<SearchResult> search(SearchSourceType type, SearchRequest request);
    
    /**
     * 多源混合检索
     */
    List<SearchResult> hybridSearch(SearchRequest request);
    
    /**
     * 检索并重排
     */
    List<SearchResult> searchWithRerank(SearchRequest request);
}
```

---

## 3. 核心功能

### 3.1 Elasticsearch 检索

**功能**：全文检索、BM25 算法

```java
@Component
public class ElasticsearchSearchProvider implements SearchProvider {
    
    @Autowired
    private ElasticsearchClient esClient;
    
    @Override
    public boolean supports(SearchSourceType type) {
        return SearchSourceType.ELASTICSEARCH == type;
    }
    
    @Override
    public List<SearchResult> search(SearchRequest request) {
        // 1. 构建 ES 查询
        SearchRequest esRequest = SearchRequest.of(s -> s
            .index("codestyle_templates")
            .query(q -> q
                .multiMatch(m -> m
                    .query(request.getQuery())
                    .fields("title^3", "content^2", "tags")
                )
            )
            .size(request.getTopK())
        );
        
        // 2. 执行查询
        SearchResponse<Document> response = esClient.search(esRequest, Document.class);
        
        // 3. 转换结果
        return convertToSearchResults(response);
    }
}
```

### 3.2 Milvus 向量检索

**功能**：语义相似度检索

```java
@Component
public class MilvusSearchProvider implements SearchProvider {
    
    @Autowired
    private MilvusClient milvusClient;
    
    @Autowired
    private EmbeddingService embeddingService;
    
    @Override
    public boolean supports(SearchSourceType type) {
        return SearchSourceType.MILVUS == type;
    }
    
    @Override
    public List<SearchResult> search(SearchRequest request) {
        // 1. 生成查询向量
        List<Float> queryVector = embeddingService.embed(request.getQuery());
        
        // 2. 构建 Milvus 查询
        SearchParam searchParam = SearchParam.newBuilder()
            .withCollectionName("codestyle_templates")
            .withVectorFieldName("embedding")
            .withVectors(Collections.singletonList(queryVector))
            .withTopK(request.getTopK())
            .withMetricType(MetricType.COSINE)
            .build();
        
        // 3. 执行查询
        R<SearchResults> response = milvusClient.search(searchParam);
        
        // 4. 转换结果
        return convertToSearchResults(response.getData());
    }
}
```

### 3.3 混合检索

**功能**：融合关键词检索和向量检索结果

```java
@Component
public class HybridSearchProvider implements SearchProvider {
    
    @Autowired
    private List<SearchProvider> providers;
    
    @Override
    public List<SearchResult> search(SearchRequest request) {
        // 1. 并行查询多个数据源
        List<CompletableFuture<List<SearchResult>>> futures = providers.stream()
            .filter(p -> p.supports(request.getSourceType()))
            .map(p -> CompletableFuture.supplyAsync(() -> p.search(request)))
            .collect(Collectors.toList());
        
        // 2. 等待所有查询完成
        List<SearchResult> allResults = futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        
        // 3. 融合排序（RRF 算法）
        return reciprocalRankFusion(allResults);
    }
    
    /**
     * Reciprocal Rank Fusion (RRF) 算法
     */
    private List<SearchResult> reciprocalRankFusion(List<SearchResult> results) {
        Map<String, Double> scoreMap = new HashMap<>();
        
        for (SearchResult result : results) {
            String id = result.getId();
            double rrf = 1.0 / (60 + result.getRank());
            scoreMap.merge(id, rrf, Double::sum);
        }
        
        return results.stream()
            .sorted((a, b) -> Double.compare(
                scoreMap.get(b.getId()), 
                scoreMap.get(a.getId())
            ))
            .collect(Collectors.toList());
    }
}
```

### 3.4 BGE-Rerank 重排

**功能**：使用 BGE-Rerank 模型对检索结果重排序

```java
@Component
public class BgeRerankProvider implements RerankProvider {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${search.rerank.api-url}")
    private String rerankApiUrl;
    
    @Override
    public List<SearchResult> rerank(String query, List<SearchResult> results) {
        // 1. 构建重排请求
        RerankRequest request = RerankRequest.builder()
            .query(query)
            .passages(results.stream()
                .map(SearchResult::getContent)
                .collect(Collectors.toList()))
            .topK(results.size())
            .build();
        
        // 2. 调用 BGE-Rerank API
        RerankResponse response = restTemplate.postForObject(
            rerankApiUrl, 
            request, 
            RerankResponse.class
        );
        
        // 3. 根据重排分数排序
        return reorderResults(results, response.getScores());
    }
}
```

---

## 4. 技术选型

### 4.1 依赖管理

```xml
<dependencies>
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
    
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Redis (缓存) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    
    <!-- Hutool -->
    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-all</artifactId>
    </dependency>
</dependencies>
```

### 4.2 配置示例

```yaml
# application-search.yml
search:
  # 是否启用检索模块
  enabled: true
  
  # Elasticsearch 配置
  elasticsearch:
    enabled: true
    hosts: localhost:9200
    username: elastic
    password: password
    index: codestyle_templates
    
  # Milvus 配置
  milvus:
    enabled: true
    host: localhost
    port: 19530
    collection: codestyle_templates
    dimension: 1024
    
  # 混合检索配置
  hybrid:
    enabled: true
    # 融合策略: RRF, WEIGHTED
    fusion-strategy: RRF
    
  # 重排配置
  rerank:
    enabled: true
    provider: BGE
    api-url: http://localhost:8001/rerank
    model: BAAI/bge-reranker-v2-m3
    top-k: 10
    
  # 缓存配置
  cache:
    enabled: true
    ttl: 3600  # 秒
```

---

## 5. 实现计划

### 5.1 第一阶段：基础架构（Week 1）

**任务**：
- [x] 创建模块结构
- [ ] 定义 SPI 接口
- [ ] 实现配置类
- [ ] 搭建单元测试框架

**交付物**：
- `SearchProvider` 接口
- `SearchFacade` 接口
- `SearchProperties` 配置类

### 5.2 第二阶段：Elasticsearch 集成（Week 2）

**任务**：
- [ ] 实现 `ElasticsearchSearchProvider`
- [ ] 配置 ES 客户端
- [ ] 实现全文检索
- [ ] 编写单元测试

**交付物**：
- Elasticsearch 检索功能
- 测试用例

### 5.3 第三阶段：Milvus 集成（Week 3）

**任务**：
- [ ] 实现 `MilvusSearchProvider`
- [ ] 配置 Milvus 客户端
- [ ] 实现向量检索
- [ ] 集成 Embedding 服务

**交付物**：
- Milvus 向量检索功能
- Embedding 服务集成

### 5.4 第四阶段：混合检索与重排（Week 4）

**任务**：
- [ ] 实现 `HybridSearchProvider`
- [ ] 实现 RRF 融合算法
- [ ] 集成 BGE-Rerank
- [ ] 性能优化

**交付物**：
- 混合检索功能
- BGE-Rerank 重排功能

### 5.5 第五阶段：API 与文档（Week 5）

**任务**：
- [ ] 实现 REST API
- [ ] 编写 API 文档
- [ ] 性能测试
- [ ] 部署文档

**交付物**：
- REST API
- 完整文档

---

## 6. API 设计

### 6.1 检索 API

```java
@Tag(name = "检索 API")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    
    private final SearchService searchService;
    
    /**
     * 单源检索
     */
    @Operation(summary = "单源检索")
    @PostMapping("/single")
    public R<List<SearchResult>> singleSearch(@RequestBody SearchRequest request) {
        List<SearchResult> results = searchService.search(
            request.getSourceType(), 
            request
        );
        return R.ok(results);
    }
    
    /**
     * 混合检索
     */
    @Operation(summary = "混合检索")
    @PostMapping("/hybrid")
    public R<List<SearchResult>> hybridSearch(@RequestBody SearchRequest request) {
        List<SearchResult> results = searchService.hybridSearch(request);
        return R.ok(results);
    }
    
    /**
     * 检索并重排
     */
    @Operation(summary = "检索并重排")
    @PostMapping("/rerank")
    public R<List<SearchResult>> searchWithRerank(@RequestBody SearchRequest request) {
        List<SearchResult> results = searchService.searchWithRerank(request);
        return R.ok(results);
    }
}
```

### 6.2 请求模型

```java
@Data
@Schema(description = "检索请求")
public class SearchRequest {
    
    @Schema(description = "查询文本", example = "如何配置 MySQL 连接池")
    @NotBlank(message = "查询文本不能为空")
    private String query;
    
    @Schema(description = "数据源类型", example = "ELASTICSEARCH")
    private SearchSourceType sourceType;
    
    @Schema(description = "返回结果数量", example = "10")
    private Integer topK = 10;
    
    @Schema(description = "是否启用重排", example = "true")
    private Boolean enableRerank = false;
    
    @Schema(description = "过滤条件")
    private Map<String, Object> filters;
}
```

### 6.3 响应模型

```java
@Data
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
}
```

---

## 📚 参考资料

1. **AssistantAgent 架构**：`ASSISTANT_AGENT_ARCHITECTURE_ANALYSIS.md`
2. **CodeStyle 最佳实践**：`CODESTYLE_BEST_PRACTICES.md`
3. **Elasticsearch 官方文档**：https://www.elastic.co/guide/
4. **Milvus 官方文档**：https://milvus.io/docs
5. **BGE-Rerank 模型**：https://huggingface.co/BAAI/bge-reranker-v2-m3

---

**文档维护**: 开发团队  
**最后更新**: 2026-01-29

