# CodeStyle 检索模块设计文档 V2

> 基于 CodeStyle 项目规范的企业级多源混合检索模块
>
> **版本**: 2.0  
> **日期**: 2026-01-29  
> **状态**: 设计中

---

## 📋 目录

1. [设计目标](#1-设计目标)
2. [架构设计](#2-架构设计)
3. [核心组件](#3-核心组件)
4. [容错机制](#4-容错机制)
5. [缓存策略](#5-缓存策略)
6. [实现计划](#6-实现计划)

---

## 1. 设计目标

### 1.1 功能目标

- ✅ **多源检索**：支持 Elasticsearch、Milvus 等多种数据源
- ✅ **混合检索**：融合全文检索和向量检索结果
- ✅ **智能重排**：使用 BGE-Rerank 提升相关性
- ✅ **高可用**：完善的容错和降级机制
- ✅ **高性能**：多级缓存、并行查询

### 1.2 设计原则

- 🎯 **遵循 CodeStyle 规范**：命名、分层、异常处理
- 🎯 **简单直接**：不过度设计，不引入不必要的抽象
- 🎯 **易于扩展**：新增数据源只需添加新的 Service 实现
- 🎯 **生产可用**：完善的容错、缓存、监控

---

## 2. 架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                    Controller 层                         │
│              SearchController (REST API)                 │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                     Service 层                           │
│  ┌──────────────────────────────────────────────────┐  │
│  │  SearchService (检索编排服务)                      │  │
│  │  - 单源检索                                        │  │
│  │  - 混合检索 (RRF 融合)                            │  │
│  │  - 检索并重排                                      │  │
│  └──────────────────────────────────────────────────┘  │
│                            ↓                             │
│  ┌──────────────┬──────────────┬──────────────────┐   │
│  │ ES 检索服务   │ Milvus 检索   │ 重排服务          │   │
│  │ ElasticsearchSearchService                       │   │
│  │              │ MilvusSearchService                │   │
│  │              │              │ RerankService      │   │
│  └──────────────┴──────────────┴──────────────────┘   │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                    Helper 层                             │
│  ┌──────────────┬──────────────┬──────────────────┐   │
│  │ 缓存助手      │ 融合算法助手  │ 容错助手          │   │
│  │ CacheHelper  │ FusionHelper │ FallbackHelper   │   │
│  └──────────────┴──────────────┴──────────────────┘   │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                   外部依赖                               │
│  ┌──────────────┬──────────────┬──────────────────┐   │
│  │ Elasticsearch│ Milvus       │ BGE-Rerank API   │   │
│  │ Client       │ Client       │ HTTP Client      │   │
│  └──────────────┴──────────────┴──────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### 2.2 分层说明

| 层级 | 职责 | 示例 |
|------|------|------|
| **Controller** | 处理 HTTP 请求，参数校验 | `SearchController` |
| **Service** | 业务逻辑编排，事务管理 | `SearchService`, `ElasticsearchSearchService` |
| **Helper** | 工具类，无状态辅助方法 | `CacheHelper`, `FusionHelper` |
| **Model** | 数据模型定义 | `SearchRequest`, `SearchResult` |
| **Config** | 配置类 | `SearchProperties`, `ElasticsearchConfig` |

### 2.3 命名规范

参考 CodeStyle 项目现有命名：

```java
// ✅ 正确命名
SearchService                    // 业务服务接口
SearchServiceImpl                // 业务服务实现
ElasticsearchSearchService       // ES 检索服务
MilvusSearchService              // Milvus 检索服务
RerankService                    // 重排服务
CacheHelper                      // 缓存助手
FusionHelper                     // 融合算法助手

// ❌ 错误命名（微服务风格，不适用）
SearchProvider                   // 不使用 Provider
SearchFacade                     // 不使用 Facade
SearchSpi                        // 不使用 SPI
```

---

## 3. 核心组件

### 3.1 数据模型

#### SearchRequest - 检索请求

```java
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

#### SearchResult - 检索结果

```java
@Data
@Builder
@Schema(description = "检索结果")
public class SearchResult {
    
    @Schema(description = "文档 ID")
    private String id;
    
    @Schema(description = "数据源类型")
    private SearchSourceType sourceType;
    
    @Schema(description = "标题")
    private String title;
    
    @Schema(description = "内容")
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

#### SearchSourceType - 数据源类型

```java
@Getter
@RequiredArgsConstructor
public enum SearchSourceType {
    
    ELASTICSEARCH("Elasticsearch", "全文检索"),
    MILVUS("Milvus", "向量检索"),
    HYBRID("Hybrid", "混合检索");
    
    private final String code;
    private final String description;
}
```

### 3.2 Service 层

#### SearchService - 检索编排服务

```java
public interface SearchService {
    
    /**
     * 单源检索
     *
     * @param type    数据源类型
     * @param request 检索请求
     * @return 检索结果列表
     */
    List<SearchResult> search(SearchSourceType type, SearchRequest request);
    
    /**
     * 混合检索（ES + Milvus）
     *
     * @param request 检索请求
     * @return 检索结果列表
     */
    List<SearchResult> hybridSearch(SearchRequest request);
    
    /**
     * 检索并重排
     *
     * @param request 检索请求
     * @return 检索结果列表
     */
    List<SearchResult> searchWithRerank(SearchRequest request);
}
```

**实现要点**：
- 编排多个检索服务
- 使用 RRF 算法融合结果
- 调用重排服务优化排序
- 处理超时和异常

#### ElasticsearchSearchService - ES 检索服务

```java
public interface ElasticsearchSearchService {
    
    /**
     * 执行 ES 检索
     *
     * @param request 检索请求
     * @return 检索结果列表
     */
    List<SearchResult> search(SearchRequest request);
}
```

**实现要点**：
- 多字段加权检索（title^3, content^2, tags）
- 高亮显示
- 结果转换
- 异常处理

#### MilvusSearchService - Milvus 检索服务

```java
public interface MilvusSearchService {
    
    /**
     * 执行向量检索
     *
     * @param request 检索请求
     * @return 检索结果列表
     */
    List<SearchResult> search(SearchRequest request);
}
```

**实现要点**：
- 文本向量化（调用 Embedding 服务）
- 向量相似度检索
- 结果转换
- 异常处理

#### RerankService - 重排服务

```java
public interface RerankService {
    
    /**
     * 对检索结果进行重排序
     *
     * @param query   查询文本
     * @param results 原始检索结果
     * @return 重排后的结果
     */
    List<SearchResult> rerank(String query, List<SearchResult> results);
}
```

**实现要点**：
- 调用 BGE-Rerank API
- 更新结果分数和排名
- 异常处理和降级

### 3.3 Helper 层

#### CacheHelper - 缓存助手

```java
public class CacheHelper {
    
    /**
     * 生成缓存 Key
     *
     * @param request 检索请求
     * @return 缓存 Key
     */
    public static String generateCacheKey(SearchRequest request);
    
    /**
     * 获取缓存结果
     *
     * @param key 缓存 Key
     * @return 缓存结果
     */
    public static Optional<List<SearchResult>> getCache(String key);
    
    /**
     * 设置缓存
     *
     * @param key     缓存 Key
     * @param results 检索结果
     * @param ttl     过期时间（秒）
     */
    public static void setCache(String key, List<SearchResult> results, long ttl);
}
```

#### FusionHelper - 融合算法助手

```java
public class FusionHelper {
    
    /**
     * RRF (Reciprocal Rank Fusion) 融合算法
     *
     * @param results 多源检索结果
     * @return 融合后的结果
     */
    public static List<SearchResult> reciprocalRankFusion(List<SearchResult> results);
    
    /**
     * 加权融合算法
     *
     * @param results 多源检索结果
     * @param weights 权重配置
     * @return 融合后的结果
     */
    public static List<SearchResult> weightedFusion(
        List<SearchResult> results, 
        Map<SearchSourceType, Double> weights
    );
}
```

#### FallbackHelper - 容错助手

```java
public class FallbackHelper {
    
    /**
     * 执行带容错的检索
     *
     * @param searchFunction 检索函数
     * @param fallbackValue  降级返回值
     * @return 检索结果
     */
    public static <T> T executeWithFallback(
        Supplier<T> searchFunction,
        T fallbackValue
    );
    
    /**
     * 执行带超时的检索
     *
     * @param searchFunction 检索函数
     * @param timeout        超时时间（毫秒）
     * @return 检索结果
     */
    public static <T> CompletableFuture<T> executeWithTimeout(
        Supplier<T> searchFunction,
        long timeout
    );
}
```

---

## 4. 容错机制

参考：https://java2ai.com/agents/assistantagent/features/search/advanced#5-搜索结果缓存

### 4.1 超时控制

```java
@Service
public class SearchServiceImpl implements SearchService {
    
    @Override
    public List<SearchResult> hybridSearch(SearchRequest request) {
        // 并行查询多个数据源，设置超时
        List<CompletableFuture<List<SearchResult>>> futures = Arrays.asList(
            executeWithTimeout(() -> esSearchService.search(request), request.getTimeout()),
            executeWithTimeout(() -> milvusSearchService.search(request), request.getTimeout())
        );
        
        // 等待所有查询完成或超时
        List<SearchResult> allResults = futures.stream()
            .map(future -> {
                try {
                    return future.get(request.getTimeout(), TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    log.warn("检索超时", e);
                    return Collections.<SearchResult>emptyList();
                } catch (Exception e) {
                    log.error("检索失败", e);
                    return Collections.<SearchResult>emptyList();
                }
            })
            .flatMap(List::stream)
            .collect(Collectors.toList());
        
        return FusionHelper.reciprocalRankFusion(allResults);
    }
}
```

### 4.2 降级策略

```java
@Service
public class ElasticsearchSearchServiceImpl implements ElasticsearchSearchService {
    
    @Override
    public List<SearchResult> search(SearchRequest request) {
        try {
            // 尝试执行 ES 检索
            return doSearch(request);
        } catch (ElasticsearchException e) {
            log.error("ES 检索失败，返回空结果", e);
            // 降级：返回空结果
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("检索异常", e);
            throw new BusinessException("检索服务暂时不可用，请稍后重试");
        }
    }
}
```

### 4.3 重试机制

```java
@Service
public class RerankServiceImpl implements RerankService {
    
    @Retryable(
        value = {RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Override
    public List<SearchResult> rerank(String query, List<SearchResult> results) {
        try {
            // 调用 BGE-Rerank API
            return callRerankApi(query, results);
        } catch (RestClientException e) {
            log.warn("重排 API 调用失败，将重试", e);
            throw e;
        } catch (Exception e) {
            log.error("重排失败，返回原始结果", e);
            // 降级：返回原始结果
            return results;
        }
    }
}
```

### 4.4 熔断机制

```java
@Service
public class MilvusSearchServiceImpl implements MilvusSearchService {
    
    private final CircuitBreaker circuitBreaker;
    
    @Override
    public List<SearchResult> search(SearchRequest request) {
        return circuitBreaker.executeSupplier(() -> {
            try {
                return doSearch(request);
            } catch (Exception e) {
                log.error("Milvus 检索失败", e);
                throw new BusinessException("向量检索服务异常");
            }
        });
    }
}
```

**熔断配置**：

```yaml
resilience4j:
  circuitbreaker:
    instances:
      milvusSearch:
        failure-rate-threshold: 50          # 失败率阈值 50%
        wait-duration-in-open-state: 60s    # 熔断器打开后等待时间
        sliding-window-size: 10             # 滑动窗口大小
```

---

## 5. 缓存策略

参考：https://java2ai.com/agents/assistantagent/features/search/advanced#5-搜索结果缓存

### 5.1 多级缓存架构

```
┌─────────────────────────────────────────────────────────┐
│                   L1: 本地缓存                           │
│              Caffeine (容量 1000, TTL 5分钟)             │
└─────────────────────────────────────────────────────────┘
                            ↓ Miss
┌─────────────────────────────────────────────────────────┐
│                   L2: Redis 缓存                         │
│                  (TTL 1小时)                             │
└─────────────────────────────────────────────────────────┘
                            ↓ Miss
┌─────────────────────────────────────────────────────────┐
│                   数据源查询                             │
│            (ES / Milvus / BGE-Rerank)                   │
└─────────────────────────────────────────────────────────┘
```

### 5.2 缓存实现

#### 本地缓存（Caffeine）

```java
@Configuration
public class CacheConfig {
    
    @Bean
    public Cache<String, List<SearchResult>> localCache() {
        return Caffeine.newBuilder()
            .maximumSize(1000)                          // 最大容量
            .expireAfterWrite(5, TimeUnit.MINUTES)      // 写入后 5 分钟过期
            .recordStats()                              // 记录统计信息
            .build();
    }
}
```

#### Redis 缓存

```java
@Service
public class CacheHelper {
    
    private static final String CACHE_PREFIX = "search:";
    private static final long CACHE_TTL = 3600L; // 1 小时
    
    /**
     * 生成缓存 Key
     */
    public static String generateCacheKey(SearchRequest request) {
        // 使用 MD5 生成缓存 Key
        String keyContent = String.format("%s:%s:%d:%s",
            request.getSourceType(),
            request.getQuery(),
            request.getTopK(),
            JSONUtils.toJsonStr(request.getFilters())
        );
        return CACHE_PREFIX + DigestUtil.md5Hex(keyContent);
    }
    
    /**
     * 获取缓存
     */
    public static Optional<List<SearchResult>> getFromRedis(String key) {
        String json = RedisUtils.get(key);
        if (StrUtil.isBlank(json)) {
            return Optional.empty();
        }
        return Optional.of(JSONUtils.parseArray(json, SearchResult.class));
    }
    
    /**
     * 设置缓存
     */
    public static void setToRedis(String key, List<SearchResult> results) {
        RedisUtils.set(key, JSONUtils.toJsonStr(results), CACHE_TTL);
    }
}
```

### 5.3 缓存使用

```java
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    
    private final Cache<String, List<SearchResult>> localCache;
    
    @Override
    public List<SearchResult> search(SearchSourceType type, SearchRequest request) {
        String cacheKey = CacheHelper.generateCacheKey(request);
        
        // 1. 尝试从本地缓存获取
        List<SearchResult> results = localCache.getIfPresent(cacheKey);
        if (results != null) {
            log.debug("命中本地缓存: {}", cacheKey);
            return results;
        }
        
        // 2. 尝试从 Redis 获取
        Optional<List<SearchResult>> redisResults = CacheHelper.getFromRedis(cacheKey);
        if (redisResults.isPresent()) {
            log.debug("命中 Redis 缓存: {}", cacheKey);
            results = redisResults.get();
            // 回填本地缓存
            localCache.put(cacheKey, results);
            return results;
        }
        
        // 3. 执行检索
        log.debug("缓存未命中，执行检索: {}", cacheKey);
        results = doSearch(type, request);
        
        // 4. 写入缓存
        localCache.put(cacheKey, results);
        CacheHelper.setToRedis(cacheKey, results);
        
        return results;
    }
}
```

### 5.4 缓存失效策略

```java
@Service
public class SearchCacheManager {
    
    /**
     * 清除指定查询的缓存
     */
    public void evictCache(String query) {
        String pattern = CacheHelper.CACHE_PREFIX + "*" + query + "*";
        RedisUtils.deleteByPattern(pattern);
    }
    
    /**
     * 清除所有检索缓存
     */
    public void evictAllCache() {
        String pattern = CacheHelper.CACHE_PREFIX + "*";
        RedisUtils.deleteByPattern(pattern);
    }
    
    /**
     * 数据更新时清除相关缓存
     */
    @EventListener
    public void onDataChange(DataChangeEvent event) {
        log.info("数据变更，清除检索缓存");
        evictAllCache();
    }
}
```

---

## 6. 实现计划

### 阶段一：基础架构（1 周）

**目标**：搭建项目结构，实现 ES 检索

**任务清单**：
- [ ] 创建模块结构
- [ ] 定义数据模型（SearchRequest, SearchResult, SearchSourceType）
- [ ] 实现 SearchService 接口和基础实现
- [ ] 实现 ElasticsearchSearchService
- [ ] 实现 SearchController
- [ ] 实现 FusionHelper（RRF 算法）
- [ ] 编写配置类

**交付物**：
- 可运行的 ES 检索功能
- REST API 可访问
- 基础文档

### 阶段二：Milvus 集成（1 周）

**目标**：实现向量检索和混合检索

**任务清单**：
- [ ] 实现 MilvusSearchService
- [ ] 集成 Embedding 服务
- [ ] 实现混合检索（ES + Milvus）
- [ ] 实现 RRF 融合算法
- [ ] 编写单元测试

**交付物**：
- 向量检索功能
- 混合检索功能
- 单元测试

### 阶段三：重排和缓存（1 周）

**目标**：实现重排序和多级缓存

**任务清单**：
- [ ] 实现 RerankService
- [ ] 集成 BGE-Rerank API
- [ ] 实现 CacheHelper
- [ ] 实现本地缓存（Caffeine）
- [ ] 实现 Redis 缓存
- [ ] 实现缓存失效策略

**交付物**：
- 重排序功能
- 多级缓存
- 缓存管理

### 阶段四：容错和优化（1 周）

**目标**：完善容错机制和性能优化

**任务清单**：
- [ ] 实现 FallbackHelper
- [ ] 实现超时控制
- [ ] 实现重试机制
- [ ] 实现熔断机制
- [ ] 性能测试和调优
- [ ] 监控指标

**交付物**：
- 完善的容错机制
- 性能优化
- 监控指标

### 阶段五：测试和文档（1 周）

**目标**：完善测试和文档

**任务清单**：
- [ ] 单元测试（覆盖率 > 80%）
- [ ] 集成测试
- [ ] 性能测试
- [ ] API 文档
- [ ] 使用文档
- [ ] 部署文档

**交付物**：
- 完整的测试
- 完善的文档
- 生产可用

---

## 📚 参考资料

- [CodeStyle 最佳实践](../../../CODESTYLE_BEST_PRACTICES.md)
- [CodeStyle 进阶实践](../../../CODESTYLE_BEST_PRACTICES_ADVANCED.md)
- [AssistantAgent 检索架构](https://java2ai.com/agents/assistantagent/features/search/advanced)
- [Elasticsearch 官方文档](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- [Milvus 官方文档](https://milvus.io/docs)

---

**文档维护**: CodeStyle Team  
**最后更新**: 2026-01-29

