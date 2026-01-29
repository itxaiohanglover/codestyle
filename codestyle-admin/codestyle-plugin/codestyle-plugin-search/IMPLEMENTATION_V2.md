# CodeStyle 检索模块实现规划 V2

> 基于 CodeStyle 项目规范的实现指南
>
> **版本**: 2.0  
> **日期**: 2026-01-29

---

## 📋 目录

1. [项目结构](#1-项目结构)
2. [核心代码示例](#2-核心代码示例)
3. [配置说明](#3-配置说明)
4. [实施步骤](#4-实施步骤)

---

## 1. 项目结构

```
codestyle-plugin-search/
├── src/main/java/top/codestyle/admin/search/
│   ├── model/                        # 数据模型
│   │   ├── SearchRequest.java
│   │   ├── SearchResult.java
│   │   └── SearchSourceType.java
│   ├── service/                      # 业务服务
│   │   ├── SearchService.java
│   │   ├── impl/SearchServiceImpl.java
│   │   ├── ElasticsearchSearchService.java
│   │   ├── impl/ElasticsearchSearchServiceImpl.java
│   │   ├── MilvusSearchService.java
│   │   ├── impl/MilvusSearchServiceImpl.java
│   │   ├── RerankService.java
│   │   └── impl/RerankServiceImpl.java
│   ├── helper/                       # 辅助工具类
│   │   ├── CacheHelper.java
│   │   ├── FusionHelper.java
│   │   └── FallbackHelper.java
│   ├── controller/                   # REST API
│   │   └── SearchController.java
│   ├── config/                       # 配置类
│   │   ├── SearchProperties.java
│   │   ├── ElasticsearchConfig.java
│   │   ├── MilvusConfig.java
│   │   └── CacheConfig.java
│   └── client/                       # 外部客户端
│       └── RerankClient.java
├── src/main/resources/
│   └── application-search.yml
└── pom.xml
```

---

## 2. 核心代码示例

### 2.1 SearchService - 检索编排服务

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchSearchService esSearchService;
    private final MilvusSearchService milvusSearchService;
    private final RerankService rerankService;
    private final Cache<String, List<SearchResult>> localCache;

    @Override
    public List<SearchResult> search(SearchSourceType type, SearchRequest request) {
        // 1. 检查缓存
        String cacheKey = CacheHelper.generateCacheKey(request);
        List<SearchResult> cached = getCachedResults(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 2. 根据类型执行检索
        List<SearchResult> results = switch (type) {
            case ELASTICSEARCH -> esSearchService.search(request);
            case MILVUS -> milvusSearchService.search(request);
            case HYBRID -> hybridSearch(request);
        };

        // 3. 写入缓存
        cacheResults(cacheKey, results);
        return results;
    }

    @Override
    public List<SearchResult> hybridSearch(SearchRequest request) {
        // 并行查询多个数据源
        CompletableFuture<List<SearchResult>> esFuture = 
            FallbackHelper.executeWithTimeout(
                () -> esSearchService.search(request),
                request.getTimeout()
            );
        
        CompletableFuture<List<SearchResult>> milvusFuture = 
            FallbackHelper.executeWithTimeout(
                () -> milvusSearchService.search(request),
                request.getTimeout()
            );

        // 等待所有查询完成
        List<SearchResult> allResults = Stream.of(esFuture, milvusFuture)
            .map(future -> {
                try {
                    return future.get(request.getTimeout(), TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    log.warn("检索超时或失败", e);
                    return Collections.<SearchResult>emptyList();
                }
            })
            .flatMap(List::stream)
            .collect(Collectors.toList());

        // RRF 融合
        return FusionHelper.reciprocalRankFusion(allResults);
    }

    @Override
    public List<SearchResult> searchWithRerank(SearchRequest request) {
        List<SearchResult> results = hybridSearch(request);
        
        if (request.getEnableRerank() && !results.isEmpty()) {
            try {
                return rerankService.rerank(request.getQuery(), results);
            } catch (Exception e) {
                log.error("重排失败，返回原始结果", e);
                return results;
            }
        }
        
        return results;
    }

    private List<SearchResult> getCachedResults(String key) {
        // L1: 本地缓存
        List<SearchResult> local = localCache.getIfPresent(key);
        if (local != null) {
            log.debug("命中本地缓存");
            return local;
        }

        // L2: Redis 缓存
        Optional<List<SearchResult>> redis = CacheHelper.getFromRedis(key);
        if (redis.isPresent()) {
            log.debug("命中 Redis 缓存");
            localCache.put(key, redis.get());
            return redis.get();
        }

        return null;
    }

    private void cacheResults(String key, List<SearchResult> results) {
        localCache.put(key, results);
        CacheHelper.setToRedis(key, results);
    }
}
```

### 2.2 FusionHelper - 融合算法助手

```java
public class FusionHelper {

    /**
     * RRF (Reciprocal Rank Fusion) 融合算法
     */
    public static List<SearchResult> reciprocalRankFusion(List<SearchResult> results) {
        Map<String, Double> scoreMap = new HashMap<>();
        Map<String, SearchResult> resultMap = new HashMap<>();

        // 按数据源分组
        Map<SearchSourceType, List<SearchResult>> grouped = results.stream()
            .collect(Collectors.groupingBy(SearchResult::getSourceType));

        // 计算 RRF 分数
        grouped.forEach((sourceType, sourceResults) -> {
            for (int i = 0; i < sourceResults.size(); i++) {
                SearchResult result = sourceResults.get(i);
                String id = result.getId();
                
                // RRF 公式: 1 / (k + rank)，k = 60
                double rrf = 1.0 / (60 + i + 1);
                
                scoreMap.merge(id, rrf, Double::sum);
                resultMap.putIfAbsent(id, result);
            }
        });

        // 按融合分数排序
        return scoreMap.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .map(entry -> {
                SearchResult result = resultMap.get(entry.getKey());
                result.setScore(entry.getValue());
                return result;
            })
            .collect(Collectors.toList());
    }
}
```

### 2.3 CacheHelper - 缓存助手

```java
public class CacheHelper {

    private static final String CACHE_PREFIX = "search:";
    private static final long CACHE_TTL = 3600L;

    public static String generateCacheKey(SearchRequest request) {
        String content = String.format("%s:%s:%d",
            request.getSourceType(),
            request.getQuery(),
            request.getTopK()
        );
        return CACHE_PREFIX + DigestUtil.md5Hex(content);
    }

    public static Optional<List<SearchResult>> getFromRedis(String key) {
        String json = RedisUtils.get(key);
        if (StrUtil.isBlank(json)) {
            return Optional.empty();
        }
        return Optional.of(JSONUtils.parseArray(json, SearchResult.class));
    }

    public static void setToRedis(String key, List<SearchResult> results) {
        RedisUtils.set(key, JSONUtils.toJsonStr(results), CACHE_TTL);
    }

    public static void evictCache(String pattern) {
        RedisUtils.deleteByPattern(CACHE_PREFIX + pattern);
    }
}
```

### 2.4 FallbackHelper - 容错助手

```java
public class FallbackHelper {

    public static <T> CompletableFuture<T> executeWithTimeout(
        Supplier<T> supplier,
        long timeout
    ) {
        return CompletableFuture.supplyAsync(supplier)
            .orTimeout(timeout, TimeUnit.MILLISECONDS)
            .exceptionally(ex -> {
                if (ex instanceof TimeoutException) {
                    throw new BusinessException("检索超时");
                }
                throw new BusinessException("检索失败: " + ex.getMessage());
            });
    }

    public static <T> T executeWithFallback(
        Supplier<T> supplier,
        T fallbackValue
    ) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return fallbackValue;
        }
    }
}
```

---

## 3. 配置说明

### 3.1 application-search.yml

```yaml
search:
  enabled: true
  
  # Elasticsearch 配置
  elasticsearch:
    enabled: true
    hosts: localhost:9200
    username: 
    password: 
    index: codestyle_templates
    
  # Milvus 配置
  milvus:
    enabled: false
    host: localhost
    port: 19530
    collection: codestyle_templates
    dimension: 1024
    
  # 重排配置
  rerank:
    enabled: false
    api-url: http://localhost:8001/rerank
    model: BAAI/bge-reranker-v2-m3
    top-k: 10
    
  # 缓存配置
  cache:
    enabled: true
    local:
      max-size: 1000
      ttl: 300  # 5分钟
    redis:
      ttl: 3600  # 1小时
```

### 3.2 CacheConfig.java

```java
@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, List<SearchResult>> searchLocalCache(
        SearchProperties properties
    ) {
        SearchProperties.CacheProperties.LocalProperties local = 
            properties.getCache().getLocal();
            
        return Caffeine.newBuilder()
            .maximumSize(local.getMaxSize())
            .expireAfterWrite(local.getTtl(), TimeUnit.SECONDS)
            .recordStats()
            .build();
    }
}
```

---

## 4. 实施步骤

### 阶段一：基础架构（第 1 周）

**Day 1-2: 项目初始化**
- [ ] 创建模块目录结构
- [ ] 配置 pom.xml
- [ ] 创建数据模型类
- [ ] 创建配置类

**Day 3-4: ES 检索实现**
- [ ] 实现 ElasticsearchSearchService
- [ ] 实现 SearchService 基础功能
- [ ] 实现 SearchController

**Day 5: 融合算法**
- [ ] 实现 FusionHelper
- [ ] 实现 RRF 算法
- [ ] 单元测试

### 阶段二：Milvus 集成（第 2 周）

**Day 1-2: Milvus 配置**
- [ ] 配置 Milvus 客户端
- [ ] 实现 MilvusSearchService
- [ ] 集成 Embedding 服务

**Day 3-4: 混合检索**
- [ ] 实现并行查询
- [ ] 实现混合检索
- [ ] 测试融合效果

**Day 5: 优化测试**
- [ ] 性能优化
- [ ] 单元测试
- [ ] 集成测试

### 阶段三：重排和缓存（第 3 周）

**Day 1-2: 重排实现**
- [ ] 实现 RerankService
- [ ] 集成 BGE-Rerank API
- [ ] 实现重试机制

**Day 3-4: 缓存实现**
- [ ] 实现 CacheHelper
- [ ] 配置本地缓存
- [ ] 配置 Redis 缓存
- [ ] 实现缓存失效

**Day 5: 测试优化**
- [ ] 缓存命中率测试
- [ ] 性能测试
- [ ] 优化调整

### 阶段四：容错和监控（第 4 周）

**Day 1-2: 容错机制**
- [ ] 实现 FallbackHelper
- [ ] 实现超时控制
- [ ] 实现降级策略

**Day 3-4: 监控指标**
- [ ] 添加性能指标
- [ ] 添加业务指标
- [ ] 配置告警

**Day 5: 文档完善**
- [ ] API 文档
- [ ] 使用文档
- [ ] 部署文档

---

## 📚 关键技术点

### 1. 命名规范

遵循 CodeStyle 项目规范：
- Service: 业务服务接口和实现
- Helper: 无状态工具类
- Config: 配置类
- Controller: REST 控制器

### 2. 异常处理

```java
// 使用 BusinessException
throw new BusinessException("检索服务暂时不可用");

// 全局异常处理器会自动捕获
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public R handleBusinessException(BusinessException e) {
        return R.fail(e.getMessage());
    }
}
```

### 3. 日志规范

```java
// 使用 @Slf4j
@Slf4j
@Service
public class SearchServiceImpl {
    public void search() {
        log.info("开始检索，查询: {}", query);
        log.debug("检索参数: {}", request);
        log.error("检索失败", e);
    }
}
```

### 4. 缓存使用

```java
// 多级缓存
// L1: Caffeine 本地缓存（快速，容量有限）
// L2: Redis 分布式缓存（共享，容量大）

// 读取顺序：L1 -> L2 -> 数据源
// 写入顺序：数据源 -> L2 -> L1
```

---

**文档维护**: CodeStyle Team  
**最后更新**: 2026-01-29

