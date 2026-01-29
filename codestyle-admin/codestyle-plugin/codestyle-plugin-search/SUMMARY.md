# CodeStyle 检索模块 - 重新规划总结

> 基于项目规范的重新设计
>
> **日期**: 2026-01-29  
> **状态**: 📝 规划完成，待实施

---

## 📋 当前状态

### ✅ 已完成

1. **V1 版本实现**（已废弃）
   - ❌ 使用了 SPI 架构（不适合 CodeStyle 项目）
   - ❌ 命名风格不一致（Provider/Facade）
   - ❌ 缺少容错和缓存机制
   - ✅ 基础的 ES 检索功能可用

2. **V2 版本设计**（当前版本）
   - ✅ 重新设计架构（Service + Helper）
   - ✅ 统一命名规范（符合 CodeStyle）
   - ✅ 完善容错机制（超时/降级/重试）
   - ✅ 多级缓存策略（Caffeine + Redis）
   - ✅ 详细的设计和实现文档

### 📄 文档清单

| 文档 | 说明 | 状态 |
|------|------|------|
| `DESIGN_V2.md` | V2 架构设计文档 | ✅ 完成 |
| `IMPLEMENTATION_V2.md` | V2 实现规划文档 | ✅ 完成 |
| `CHANGES_V1_TO_V2.md` | V1→V2 变更说明 | ✅ 完成 |
| `README.md` | 使用说明（待更新） | ⏳ 待更新 |

---

## 🎯 核心改进

### 1. 架构调整

```
V1 (SPI 风格) ❌              V2 (CodeStyle 风格) ✅
┌─────────────────┐          ┌─────────────────┐
│  SearchFacade   │          │  SearchService  │
│  (门面接口)      │    →     │  (编排服务)      │
└─────────────────┘          └─────────────────┘
        ↓                            ↓
┌─────────────────┐          ┌─────────────────┐
│ SearchProvider  │          │ ES/Milvus       │
│ (SPI 接口)      │    →     │ SearchService   │
└─────────────────┘          └─────────────────┘
        ↓                            ↓
┌─────────────────┐          ┌─────────────────┐
│ ES/Milvus       │          │  Helper 层       │
│ Provider 实现   │    →     │ (工具辅助类)     │
└─────────────────┘          └─────────────────┘
```

### 2. 命名对照

| 概念 | V1 命名 | V2 命名 |
|------|---------|---------|
| 检索编排 | SearchFacade | SearchService |
| ES 检索 | ElasticsearchSearchProvider | ElasticsearchSearchService |
| Milvus 检索 | MilvusSearchProvider | MilvusSearchService |
| 重排 | RerankProvider | RerankService |
| 工具类 | - | CacheHelper, FusionHelper |

### 3. 新增功能

#### 容错机制

```java
// ✅ 超时控制
FallbackHelper.executeWithTimeout(supplier, timeout)

// ✅ 降级策略
try {
    return doSearch();
} catch (Exception e) {
    return Collections.emptyList();
}

// ✅ 重试机制
@Retryable(maxAttempts = 3)
public List<SearchResult> rerank() { }
```

#### 多级缓存

```
L1: Caffeine (本地)
    ↓ Miss
L2: Redis (分布式)
    ↓ Miss
数据源 (ES/Milvus)
```

---

## 📂 V2 项目结构

```
codestyle-plugin-search/
├── src/main/java/top/codestyle/admin/search/
│   ├── model/                        # 数据模型
│   │   ├── SearchRequest.java        ✅ 检索请求
│   │   ├── SearchResult.java         ✅ 检索结果
│   │   └── SearchSourceType.java     ✅ 数据源类型
│   │
│   ├── service/                      # 业务服务层
│   │   ├── SearchService.java        📝 检索编排服务接口
│   │   ├── impl/
│   │   │   └── SearchServiceImpl.java 📝 检索编排服务实现
│   │   ├── ElasticsearchSearchService.java 📝 ES 检索接口
│   │   ├── impl/
│   │   │   └── ElasticsearchSearchServiceImpl.java 📝 ES 检索实现
│   │   ├── MilvusSearchService.java  📝 Milvus 检索接口
│   │   ├── impl/
│   │   │   └── MilvusSearchServiceImpl.java 📝 Milvus 检索实现
│   │   ├── RerankService.java        📝 重排接口
│   │   └── impl/
│   │       └── RerankServiceImpl.java 📝 重排实现
│   │
│   ├── helper/                       # 工具辅助层
│   │   ├── CacheHelper.java          📝 缓存助手
│   │   ├── FusionHelper.java         📝 融合算法助手
│   │   └── FallbackHelper.java       📝 容错助手
│   │
│   ├── controller/                   # REST API
│   │   └── SearchController.java     📝 检索控制器
│   │
│   ├── config/                       # 配置类
│   │   ├── SearchProperties.java     📝 配置属性
│   │   ├── ElasticsearchConfig.java  📝 ES 配置
│   │   ├── MilvusConfig.java         📝 Milvus 配置
│   │   └── CacheConfig.java          📝 缓存配置
│   │
│   └── client/                       # 外部客户端
│       └── RerankClient.java         📝 重排 HTTP 客户端
│
├── src/main/resources/
│   └── application-search.yml        📝 模块配置
│
├── pom.xml                           📝 Maven 配置
├── DESIGN_V2.md                      ✅ 设计文档
├── IMPLEMENTATION_V2.md              ✅ 实现规划
├── CHANGES_V1_TO_V2.md               ✅ 变更说明
└── README.md                         ⏳ 使用说明（待更新）

图例：
✅ 已完成（V1 遗留，需重构）
📝 待实现（V2 新设计）
⏳ 待更新
```

---

## 🚀 下一步计划

### 方案 A：重构 V1 代码

**优势**：
- 保留已有的 ES 检索功能
- 渐进式重构，风险较低

**步骤**：
1. 重命名类和包（Provider → Service）
2. 移除 SPI 相关代码
3. 创建 Helper 层
4. 添加容错和缓存
5. 更新文档

**预计时间**：2-3 天

### 方案 B：全新实现 V2

**优势**：
- 代码更清晰，无历史包袱
- 完全符合新设计

**步骤**：
1. 删除 V1 代码（保留 model 层）
2. 按 V2 设计重新实现
3. 逐步添加功能
4. 完善测试和文档

**预计时间**：1 周

### 推荐方案：方案 B（全新实现）

**理由**：
1. V1 代码量不大，重构成本不高
2. 全新实现更符合设计理念
3. 避免历史包袱，代码更清晰
4. 可以边实现边优化

---

## 📋 实施检查清单

### 阶段一：基础架构（3 天）

**Day 1: 清理和初始化**
- [ ] 删除 V1 的 spi 和 provider 包
- [ ] 保留 model 层（SearchRequest, SearchResult, SearchSourceType）
- [ ] 创建 V2 的目录结构（service, helper, config）
- [ ] 更新 pom.xml（添加 Caffeine 依赖）

**Day 2: Service 层实现**
- [ ] 实现 ElasticsearchSearchService
- [ ] 实现 SearchService（编排服务）
- [ ] 实现 SearchController

**Day 3: Helper 层实现**
- [ ] 实现 FusionHelper（RRF 算法）
- [ ] 实现 CacheHelper（多级缓存）
- [ ] 实现 FallbackHelper（容错）

### 阶段二：Milvus 集成（2 天）

**Day 4: Milvus 配置**
- [ ] 配置 Milvus 客户端
- [ ] 实现 MilvusSearchService
- [ ] 集成 Embedding 服务

**Day 5: 混合检索**
- [ ] 实现并行查询
- [ ] 实现混合检索
- [ ] 测试融合效果

### 阶段三：重排和优化（2 天）

**Day 6: 重排实现**
- [ ] 实现 RerankService
- [ ] 集成 BGE-Rerank API
- [ ] 实现重试机制

**Day 7: 测试和文档**
- [ ] 单元测试
- [ ] 集成测试
- [ ] 更新 README.md
- [ ] API 文档

---

## 💡 关键技术点

### 1. 依赖注入（不是 SPI）

```java
// ✅ V2: 使用 Spring 依赖注入
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    
    private final ElasticsearchSearchService esSearchService;
    private final MilvusSearchService milvusSearchService;
    private final RerankService rerankService;
    
    // Spring 自动注入，简单直接
}
```

### 2. Helper 工具类

```java
// ✅ V2: 无状态工具类
public class CacheHelper {
    
    // 静态方法，无状态
    public static String generateCacheKey(SearchRequest request) {
        // ...
    }
    
    public static Optional<List<SearchResult>> getFromRedis(String key) {
        // ...
    }
}
```

### 3. 容错模式

```java
// ✅ V2: 完善的容错
public List<SearchResult> hybridSearch(SearchRequest request) {
    // 1. 超时控制
    CompletableFuture<List<SearchResult>> future = 
        FallbackHelper.executeWithTimeout(supplier, timeout);
    
    // 2. 异常处理
    try {
        return future.get(timeout, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
        log.warn("检索超时");
        return Collections.emptyList();  // 降级
    }
}
```

### 4. 多级缓存

```java
// ✅ V2: L1 + L2 缓存
public List<SearchResult> search(SearchRequest request) {
    String key = CacheHelper.generateCacheKey(request);
    
    // L1: 本地缓存
    List<SearchResult> local = localCache.getIfPresent(key);
    if (local != null) return local;
    
    // L2: Redis 缓存
    Optional<List<SearchResult>> redis = CacheHelper.getFromRedis(key);
    if (redis.isPresent()) {
        localCache.put(key, redis.get());
        return redis.get();
    }
    
    // 数据源查询
    List<SearchResult> results = doSearch(request);
    
    // 写入缓存
    localCache.put(key, results);
    CacheHelper.setToRedis(key, results);
    
    return results;
}
```

---

## 📊 对比总结

| 维度 | V1 | V2 | 改进 |
|------|----|----|------|
| 架构风格 | SPI + Provider | Service + Helper | ✅ 符合项目规范 |
| 代码行数 | ~1200 | ~1500（预计） | ➕ 增加容错和缓存 |
| 复杂度 | 较高 | 适中 | ✅ 降低学习成本 |
| 容错机制 | 基础 | 完善 | ✅ 生产可用 |
| 缓存策略 | 单级 | 多级 | ✅ 性能提升 |
| 可维护性 | 中 | 高 | ✅ 代码更清晰 |

---

## 🎯 成功标准

### 功能完整性
- ✅ ES 全文检索
- ✅ Milvus 向量检索
- ✅ 混合检索（RRF 融合）
- ✅ BGE-Rerank 重排
- ✅ 多级缓存
- ✅ 容错降级

### 性能指标
- ✅ 单次检索 < 500ms
- ✅ 缓存命中率 > 50%
- ✅ 并发支持 > 100 QPS

### 代码质量
- ✅ 单元测试覆盖率 > 80%
- ✅ 无 Spotless 格式问题
- ✅ 无 SonarQube 严重问题

### 文档完整性
- ✅ 设计文档
- ✅ 实现规划
- ✅ API 文档
- ✅ 使用说明

---

## 📚 参考文档

- [DESIGN_V2.md](DESIGN_V2.md) - V2 架构设计
- [IMPLEMENTATION_V2.md](IMPLEMENTATION_V2.md) - V2 实现规划
- [CHANGES_V1_TO_V2.md](CHANGES_V1_TO_V2.md) - V1→V2 变更说明
- [CodeStyle 最佳实践](../../../CODESTYLE_BEST_PRACTICES.md)
- [CodeStyle 进阶实践](../../../CODESTYLE_BEST_PRACTICES_ADVANCED.md)

---

## ✅ 下一步行动

### 立即执行

1. **确认方案**：选择方案 A（重构）或方案 B（重写）
2. **清理代码**：删除或重构 V1 代码
3. **开始实施**：按照 V2 设计开始编码

### 需要决策

- [ ] 选择实施方案（A 或 B）
- [ ] 确认实施时间表
- [ ] 分配开发资源

---

**文档维护**: CodeStyle Team  
**最后更新**: 2026-01-29  
**状态**: 📝 等待实施决策

