# 设计变更说明 (V1 → V2)

> 从微服务风格调整为 CodeStyle 项目风格
>
> **日期**: 2026-01-29

---

## 📋 主要变更

### 1. 架构调整

#### ❌ V1 设计问题

```
使用了 SPI 架构模式：
- SearchProvider 接口（SPI 风格）
- SearchFacade 门面模式
- RerankProvider 接口

问题：
1. SPI 是微服务/插件化框架的概念
2. CodeStyle 本身就是模块化的，不需要 SPI
3. 命名风格与项目不一致
```

#### ✅ V2 设计改进

```
使用 CodeStyle 标准分层：
- Service 层：业务服务
- Helper 层：工具辅助类
- Config 层：配置管理

优势：
1. 符合 CodeStyle 项目规范
2. 简单直接，易于理解
3. 与现有代码风格一致
```

### 2. 命名变更对照表

| V1 命名 | V2 命名 | 说明 |
|---------|---------|------|
| `SearchProvider` | `ElasticsearchSearchService` | 具体的检索服务 |
| `SearchFacade` | `SearchService` | 检索编排服务 |
| `RerankProvider` | `RerankService` | 重排服务 |
| `spi/` 包 | `service/` 包 | 服务层 |
| - | `helper/` 包 | 工具类层（新增） |

### 3. 类结构对比

#### V1 结构（SPI 风格）

```
search/
├── spi/
│   ├── SearchProvider.java      ❌ SPI 接口
│   ├── SearchFacade.java         ❌ 门面接口
│   └── RerankProvider.java       ❌ SPI 接口
├── provider/
│   ├── ElasticsearchSearchProvider.java  ❌ Provider 命名
│   └── MilvusSearchProvider.java         ❌ Provider 命名
└── service/
    └── SearchServiceImpl.java
```

#### V2 结构（CodeStyle 风格）

```
search/
├── service/                      ✅ 业务服务层
│   ├── SearchService.java
│   ├── impl/SearchServiceImpl.java
│   ├── ElasticsearchSearchService.java
│   ├── impl/ElasticsearchSearchServiceImpl.java
│   ├── MilvusSearchService.java
│   ├── impl/MilvusSearchServiceImpl.java
│   ├── RerankService.java
│   └── impl/RerankServiceImpl.java
├── helper/                       ✅ 工具辅助层（新增）
│   ├── CacheHelper.java
│   ├── FusionHelper.java
│   └── FallbackHelper.java
├── controller/
│   └── SearchController.java
├── model/
│   ├── SearchRequest.java
│   ├── SearchResult.java
│   └── SearchSourceType.java
└── config/
    ├── SearchProperties.java
    ├── ElasticsearchConfig.java
    └── CacheConfig.java
```

---

## 🎯 核心改进点

### 1. 移除 SPI 抽象

#### V1 代码（过度抽象）

```java
// ❌ 不必要的 SPI 接口
public interface SearchProvider {
    boolean supports(SearchSourceType type);
    List<SearchResult> search(SearchRequest request);
    String getName();
    int getPriority();
}

// ❌ 不必要的门面接口
public interface SearchFacade {
    List<SearchResult> search(SearchSourceType type, SearchRequest request);
    List<SearchResult> hybridSearch(SearchRequest request);
}
```

#### V2 代码（简单直接）

```java
// ✅ 直接的服务接口
public interface ElasticsearchSearchService {
    List<SearchResult> search(SearchRequest request);
}

public interface MilvusSearchService {
    List<SearchResult> search(SearchRequest request);
}

// ✅ 编排服务
public interface SearchService {
    List<SearchResult> search(SearchSourceType type, SearchRequest request);
    List<SearchResult> hybridSearch(SearchRequest request);
    List<SearchResult> searchWithRerank(SearchRequest request);
}
```

### 2. 新增 Helper 层

V2 新增了 Helper 层，用于存放无状态的工具方法：

```java
// ✅ 缓存助手
public class CacheHelper {
    public static String generateCacheKey(SearchRequest request);
    public static Optional<List<SearchResult>> getFromRedis(String key);
    public static void setToRedis(String key, List<SearchResult> results);
}

// ✅ 融合算法助手
public class FusionHelper {
    public static List<SearchResult> reciprocalRankFusion(List<SearchResult> results);
}

// ✅ 容错助手
public class FallbackHelper {
    public static <T> CompletableFuture<T> executeWithTimeout(Supplier<T> supplier, long timeout);
    public static <T> T executeWithFallback(Supplier<T> supplier, T fallbackValue);
}
```

**优势**：
- 符合 CodeStyle 项目的 Helper 命名习惯
- 无状态，易于测试
- 可复用性强

### 3. 增强容错机制

V2 新增了完善的容错机制：

```java
// ✅ 超时控制
CompletableFuture<List<SearchResult>> future = 
    FallbackHelper.executeWithTimeout(
        () -> esSearchService.search(request),
        request.getTimeout()
    );

// ✅ 降级策略
try {
    return doSearch(request);
} catch (Exception e) {
    log.error("检索失败，返回空结果", e);
    return Collections.emptyList();
}

// ✅ 重试机制
@Retryable(
    value = {RestClientException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
public List<SearchResult> rerank(String query, List<SearchResult> results) {
    // ...
}
```

### 4. 多级缓存策略

V2 实现了完整的多级缓存：

```
L1: Caffeine 本地缓存
    - 容量: 1000
    - TTL: 5 分钟
    - 优势: 极快，无网络开销
    
L2: Redis 分布式缓存
    - TTL: 1 小时
    - 优势: 共享，容量大
    
读取流程: L1 → L2 → 数据源
写入流程: 数据源 → L2 → L1
```

---

## 📊 对比总结

| 维度 | V1 | V2 | 改进 |
|------|----|----|------|
| **架构风格** | SPI + Provider | Service + Helper | ✅ 符合项目规范 |
| **命名规范** | Provider/Facade | Service/Helper | ✅ 统一命名风格 |
| **代码复杂度** | 较高（过度抽象） | 适中（恰到好处） | ✅ 降低复杂度 |
| **容错机制** | 基础 | 完善（超时/降级/重试） | ✅ 生产可用 |
| **缓存策略** | 单级 Redis | 多级（Caffeine + Redis） | ✅ 性能提升 |
| **可扩展性** | 通过 SPI | 通过 Spring 注入 | ✅ 更简单 |
| **学习成本** | 较高 | 较低 | ✅ 易于理解 |

---

## 🚀 迁移指南

如果已经实现了 V1 版本，可以按以下步骤迁移到 V2：

### Step 1: 重命名包和类

```bash
# 删除 spi 包
rm -rf src/main/java/top/codestyle/admin/search/spi/

# 重命名 provider 包为 service
mv src/main/java/top/codestyle/admin/search/provider/ \
   src/main/java/top/codestyle/admin/search/service/

# 重命名类
ElasticsearchSearchProvider.java → ElasticsearchSearchService.java
MilvusSearchProvider.java → MilvusSearchService.java
```

### Step 2: 创建 Helper 类

```bash
# 创建 helper 包
mkdir -p src/main/java/top/codestyle/admin/search/helper/

# 创建 Helper 类
touch src/main/java/top/codestyle/admin/search/helper/CacheHelper.java
touch src/main/java/top/codestyle/admin/search/helper/FusionHelper.java
touch src/main/java/top/codestyle/admin/search/helper/FallbackHelper.java
```

### Step 3: 重构 Service 实现

```java
// 移除 SPI 相关代码
// ❌ 删除
boolean supports(SearchSourceType type);
String getName();
int getPriority();

// ✅ 保留核心方法
List<SearchResult> search(SearchRequest request);
```

### Step 4: 添加容错和缓存

参考 V2 的 `SearchServiceImpl` 实现：
- 添加超时控制
- 添加降级策略
- 添加多级缓存

---

## 💡 设计理念

### V1 的问题

1. **过度设计**：引入了不必要的 SPI 抽象
2. **风格不一致**：使用了微服务框架的命名习惯
3. **功能不完善**：缺少容错和缓存机制

### V2 的优势

1. **简单直接**：直接使用 Spring 的依赖注入
2. **风格统一**：遵循 CodeStyle 项目规范
3. **功能完善**：完整的容错、缓存、监控

### 设计原则

> "简单是终极的复杂" - Leonardo da Vinci

- ✅ **KISS 原则**：保持简单直接
- ✅ **YAGNI 原则**：不做过度设计
- ✅ **项目一致性**：遵循现有规范
- ✅ **生产可用**：完善的容错和性能

---

## 📚 参考

- [CodeStyle 最佳实践](../../../CODESTYLE_BEST_PRACTICES.md)
- [CodeStyle 进阶实践](../../../CODESTYLE_BEST_PRACTICES_ADVANCED.md)
- [AssistantAgent 容错机制](https://java2ai.com/agents/assistantagent/features/search/advanced#5-搜索结果缓存)

---

**文档维护**: CodeStyle Team  
**最后更新**: 2026-01-29

