# CodeStyle 检索模块混合架构设计文档

> 基于 AssistantAgent SPI 架构与 CodeStyle Service + Helper 模式的混合设计方案
>
> **版本**: 2.0.0
> **日期**: 2026-01-30
> **状态**: 设计完成

---

## 📋 目录

1. [设计目标](#1-设计目标)
2. [整体架构](#2-整体架构)
3. [核心接口设计](#3-核心接口设计)
4. [数据模型](#4-数据模型)
5. [Service 层设计](#5-service-层设计)
6. [Helper 层设计](#6-helper-层设计)
7. [配置设计](#7-配置设计)
8. [检索服务实现](#8-检索服务实现)
9. [SPI 扩展示例](#9-spi-扩展示例)
10. [配置文件](#10-配置文件)
11. [REST API 设计](#11-rest-api-设计)
12. [实现计划](#12-实现计划)
13. [目录结构](#13-目录结构)
14. [API 使用示例](#14-api-使用示例)

---

## 1. 设计目标

### 1.1 功能目标

- ✅ **向量检索（Milvus）**：基于语义相似度的向量检索
- ✅ **关键词检索**：支持 Elasticsearch 全文检索和 Milvus 关键词检索
- ✅ **混合检索**：融合向量和关键词检索结果，支持加权配置
- ✅ **智能重排**：使用 BGE-Rerank 提升相关性
- ✅ **轻量级扩展**：通过 SPI 机制支持运行时动态注册新数据源

### 1.2 设计原则

- 🎯 **混合模式**：保留 CodeStyle 的 Service + Helper 模式，仅在数据源扩展时引入轻量级 SPI
- 🎯 **保持风格**：遵循 CodeStyle 项目的命名和分层规范
- 🎯 **简单直接**：不过度设计，不引入不必要的抽象
- 🎯 **易于扩展**：新增数据源只需实现 SearchProvider 接口并通过 SPI 注册

---

## 2. 整体架构

```
┌────────────────────────────────────────────────────────────┐
│                    Controller 层                          │
│              SearchController (REST API)                    │
└────────────────────────────────────────────────────────────┘
                            ↓
┌────────────────────────────────────────────────────────────┐
│                     Service 层                           │
│  ┌──────────────────────────────────────────────────┐     │
│  │  SearchService (统一检索服务)                      │     │
│  │  - search()        单源/混合检索                   │     │
│  │  - searchWithRerank() 检索+重排                  │     │
│  └──────────────────────────────────────────────────┘     │
│                            ↓                               │
│  ┌──────────────────────────────────────────────────┐     │
│  │  SearchExecutor (检索执行器 - 核心)               │     │
│  │  - 注册和管理 SearchProvider                      │     │
│  │  - 调度 Provider 执行检索                      │     │
│  └──────────────────────────────────────────────────┘     │
│         ↓                              ↓                  │
│  ┌──────────────────┐      ┌──────────────────┐        │
│  │ ES SearchService │      │ Milvus SearchSvc │        │
│  │ (标准实现)       │      │ (标准实现)       │        │
│  └──────────────────┘      └──────────────────┘        │
│                            ↓                               │
│  ┌──────────────────────────────────────────────────┐     │
│  │  PluginProviderRegistry (SPI 扩展点)              │     │
│  │  - 自动注册自定义 SearchProvider                  │     │
│  └──────────────────────────────────────────────────┘     │
└────────────────────────────────────────────────────────────┘
                            ↓
┌────────────────────────────────────────────────────────────┐
│                    Helper 层                             │
│  ┌──────────────┬──────────────┬──────────────────┐    │
│  │ FusionHelper  │ CacheHelper  │ FallbackHelper   │    │
│  │ RRF 融合算法  │ 多级缓存      │ 容错降级         │    │
│  └──────────────┴──────────────┴──────────────────┘    │
└────────────────────────────────────────────────────────────┘
                            ↓
┌────────────────────────────────────────────────────────────┐
│                   外部依赖                               │
│  ┌──────────────┬──────────────┬──────────────────┐    │
│  │ Elasticsearch│ Milvus       │ BGE-Rerank API  │    │
│  │ Client       │ Client       │ HTTP Client     │    │
│  └──────────────┴──────────────┴──────────────────┘    │
└────────────────────────────────────────────────────────────┘
```

### 2.1 分层说明

| 层级 | 职责 | 示例 |
|------|------|------|
| **Controller** | 处理 HTTP 请求，参数校验 | `SearchController` |
| **Service** | 业务逻辑编排，事务管理 | `SearchService`, `ElasticsearchSearchService` |
| **Executor** | 核心检索执行器，管理 Provider | `SearchExecutor` |
| **Helper** | 工具类，无状态辅助方法 | `CacheHelper`, `FusionHelper` |
| **SPI** | 扩展点，支持动态注册新数据源 | `SearchProvider` |
| **Model** | 数据模型定义 | `SearchRequest`, `SearchResult` |
| **Config** | 配置类 | `SearchProperties` |

---

## 3. 核心接口设计

### 3.1 SearchProvider SPI 接口

这是轻量级插件机制的核心，支持动态扩展数据源：

```java
package top.codestyle.admin.search.spi;

import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;

import java.util.List;

/**
 * 检索提供者 SPI 接口
 * <p>
 * 用于支持运行时动态注册新的数据源检索能力
 * 自定义数据源实现此接口并通过 SPI 注册即可被自动发现和使用
 *
 * @author CodeStyle Team
 * @since 2.0.0
 */
public interface SearchProvider {

    /**
     * 判断是否支持指定的数据源类型
     *
     * @param type 数据源类型
     * @return 是否支持
     */
    boolean supports(SearchSourceType type);

    /**
     * 执行检索
     *
     * @param request 检索请求
     * @return 检索结果列表
     */
    List<SearchResult> search(SearchRequest request);

    /**
     * 获取 Provider 优先级（数值越小优先级越高）
     * <p>
     * 当多个 Provider 支持同一类型时，使用优先级最高的
     *
     * @return 优先级，默认 100
     */
    default int getPriority() {
        return 100;
    }

    /**
     * 获取 Provider 名称，用于日志和监控
     *
     * @return Provider 名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
```

---

## 4. 数据模型

### 4.1 SearchRequest - 检索请求

```java
package top.codestyle.admin.search.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 检索请求
 *
 * @author CodeStyle Team
 * @since 2.0.0
 */
@Data
@Schema(description = "检索请求")
public class SearchRequest {

    @Schema(description = "查询文本", example = "如何配置 MySQL 连接池")
    @NotBlank(message = "查询文本不能为空")
    private String query;

    @Schema(description = "数据源类型", example = "HYBRID")
    private SearchSourceType sourceType = SearchSourceType.HYBRID;

    @Schema(description = "返回结果数量", example = "10")
    private Integer topK = 10;

    @Schema(description = "是否启用重排", example = "true")
    private Boolean enableRerank = false;

    @Schema(description = "过滤条件")
    private Map<String, Object> filters;

    @Schema(description = "超时时间（毫秒）", example = "5000")
    private Long timeout = 5000L;

    @Schema(description = "向量检索权重（混合检索时，0-1之间）", example = "0.6")
    private Double vectorWeight = 0.6;

    @Schema(description = "关键词检索权重（混合检索时，0-1之间）", example = "0.4")
    private Double keywordWeight = 0.4;
}
```

### 4.2 SearchSourceType - 数据源类型

```java
package top.codestyle.admin.search.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 检索数据源类型
 *
 * @author CodeStyle Team
 * @since 2.0.0
 */
@Getter
@RequiredArgsConstructor
public enum SearchSourceType {

    /**
     * Elasticsearch 全文检索（关键词检索）
     */
    ELASTICSEARCH("Elasticsearch", "全文检索"),

    /**
     * Milvus 向量检索
     */
    MILVUS("Milvus", "向量检索"),

    /**
     * Milvus 关键词检索（使用稀疏向量或全文索引）
     */
    MILVUS_KEYWORD("MilvusKeyword", "Milvus 关键词检索"),

    /**
     * 混合检索（向量 + 关键词）
     */
    HYBRID("Hybrid", "混合检索"),

    /**
     * 自定义数据源
     */
    CUSTOM("Custom", "自定义数据源");

    private final String code;
    private final String description;
}
```

### 4.3 SearchResult - 检索结果

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
 *
 * @author CodeStyle Team
 * @since 2.0.0
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

---

## 5. Service 层设计

### 5.1 SearchExecutor - 核心执行器

负责管理 Provider 的注册和调度：

```java
package top.codestyle.admin.search.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;
import top.codestyle.admin.search.service.ElasticsearchSearchService;
import top.codestyle.admin.search.service.MilvusSearchService;
import top.codestyle.admin.search.spi.SearchProvider;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 检索执行器
 * <p>
 * 核心职责：
 * 1. 管理标准数据源（ES、Milvus）的检索
 * 2. 管理自定义 SPI Provider 的注册和调用
 * 3. 协调混合检索的并行执行
 *
 * @author CodeStyle Team
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchExecutor {

    private final ElasticsearchSearchService esSearchService;
    private final Optional<MilvusSearchService> milvusSearchService;

    /**
     * 通过 SPI 自动加载的自定义 Provider 列表
     */
    private final List<SearchProvider> customProviders = loadCustomProviders();

    /**
     * 加载自定义 SearchProvider
     * <p>
     * 使用 SPI 机制自动发现 META-INF/services/top.codestyle.admin.search.spi.SearchProvider
     */
    private List<SearchProvider> loadCustomProviders() {
        ServiceLoader<SearchProvider> loader = ServiceLoader.load(SearchProvider.class);
        List<SearchProvider> providers = new ArrayList<>();
        for (SearchProvider provider : loader) {
            providers.add(provider);
            log.info("加载自定义 SearchProvider: {}", provider.getName());
        }
        return providers;
    }

    /**
     * 执行单源检索
     *
     * @param type    数据源类型
     * @param request 检索请求
     * @return 检索结果
     */
    public List<SearchResult> execute(SearchSourceType type, SearchRequest request) {
        // 1. 优先使用标准 Service
        switch (type) {
            case ELASTICSEARCH:
                return esSearchService.search(request);
            case MILVUS:
                return milvusSearchService
                    .map(service -> service.search(request))
                    .orElse(Collections.emptyList());
            case CUSTOM:
                // 2. CUSTOM 类型使用自定义 Provider
                return executeByCustomProviders(Collections.singletonList(type), request);
            default:
                return Collections.emptyList();
        }
    }

    /**
     * 执行混合检索（ES + Milvus）
     *
     * @param request 检索请求
     * @return 检索结果
     */
    public List<SearchResult> executeHybrid(SearchRequest request) {
        // 并行执行 ES 和 Milvus 检索
        List<CompletableFuture<List<SearchResult>>> futures = new ArrayList<>();

        // ES 检索
        futures.add(CompletableFuture.supplyAsync(() -> esSearchService.search(request)));

        // Milvus 检索（如果可用）
        milvusSearchService.ifPresent(service ->
            futures.add(CompletableFuture.supplyAsync(() -> service.search(request)))
        );

        // 等待所有检索完成
        return futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    /**
     * 通过自定义 Provider 执行检索
     *
     * @param sourceTypes 支持的数据源类型
     * @param request     检索请求
     * @return 检索结果
     */
    public List<SearchResult> executeByCustomProviders(
        Collection<SearchSourceType> sourceTypes,
        SearchRequest request
    ) {
        return customProviders.stream()
            .filter(provider -> sourceTypes.stream().anyMatch(provider::supports))
            .sorted(Comparator.comparingInt(SearchProvider::getPriority))
            .findFirst()
            .map(provider -> {
                log.debug("使用自定义 Provider: {}", provider.getName());
                return provider.search(request);
            })
            .orElse(Collections.emptyList());
    }

    /**
     * 获取所有注册的自定义 Provider
     *
     * @return Provider 列表
     */
    public List<SearchProvider> getCustomProviders() {
        return Collections.unmodifiableList(customProviders);
    }
}
```

### 5.2 SearchService - 统一检索服务

```java
package top.codestyle.admin.search.service;

import top.codestyle.admin.search.executor.SearchExecutor;
import top.codestyle.admin.search.helper.CacheHelper;
import top.codestyle.admin.search.helper.FallbackHelper;
import top.codestyle.admin.search.helper.FusionHelper;
import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 统一检索服务
 * <p>
 * 提供：
 * - 单源检索（ES / Milvus / MilvusKeyword / 自定义）
 * - 混合检索（向量 + 关键词）
 * - 检索 + 重排
 *
 * @author CodeStyle Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchExecutor searchExecutor;
    private final Optional<RerankService> rerankService;
    private final Cache<String, List<SearchResult>> localCache;

    /**
     * 单源检索
     *
     * @param type    数据源类型
     * @param request 检索请求
     * @return 检索结果
     */
    public List<SearchResult> search(SearchSourceType type, SearchRequest request) {
        // 1. 检查缓存
        String cacheKey = CacheHelper.generateCacheKey(type, request);
        List<SearchResult> cached = getCachedResults(cacheKey);
        if (cached != null) {
            log.debug("命中缓存: {}", cacheKey);
            return cached;
        }

        // 2. 执行检索
        List<SearchResult> results;
        try {
            results = searchExecutor.execute(type, request);
        } catch (Exception e) {
            log.error("检索失败: type={}, query={}", type, request.getQuery(), e);
            return Collections.emptyList();
        }

        // 3. 写入缓存
        cacheResults(cacheKey, results);
        return results;
    }

    /**
     * 混合检索（向量 + 关键词）
     *
     * @param request 检索请求
     * @return 检索结果
     */
    public List<SearchResult> hybridSearch(SearchRequest request) {
        String cacheKey = CacheHelper.generateCacheKey(SearchSourceType.HYBRID, request);

        // 1. 检查缓存
        List<SearchResult> cached = getCachedResults(cacheKey);
        if (cached != null) {
            log.debug("命中混合检索缓存");
            return cached;
        }

        // 2. 并行执行向量检索和关键词检索
        List<SearchResult> vectorResults = searchWithFallback(
            () -> searchExecutor.execute(SearchSourceType.MILVUS, request),
            Collections.emptyList()
        );

        List<SearchResult> keywordResults = searchWithFallback(
            () -> searchExecutor.execute(SearchSourceType.ELASTICSEARCH, request),
            Collections.emptyList()
        );

        // 3. 加权融合
        List<SearchResult> fusedResults = FusionHelper.weightedFusion(
            vectorResults,
            keywordResults,
            request.getVectorWeight(),
            request.getKeywordWeight()
        );

        // 4. 写入缓存
        cacheResults(cacheKey, fusedResults);
        return fusedResults.stream()
            .limit(request.getTopK())
            .toList();
    }

    /**
     * 检索并重排
     *
     * @param request 检索请求
     * @return 重排后的检索结果
     */
    public List<SearchResult> searchWithRerank(SearchRequest request) {
        // 1. 先执行混合检索
        List<SearchResult> results = hybridSearch(request);

        // 2. 如果启用重排且有重排服务
        if (request.getEnableRerank() && !results.isEmpty() && rerankService.isPresent()) {
            try {
                log.info("开始重排序，原始结果数: {}", results.size());
                results = rerankService.get().rerank(request.getQuery(), results);
                log.info("重排序完成");
            } catch (Exception e) {
                log.error("重排失败，返回原始结果", e);
            }
        } else if (request.getEnableRerank() && rerankService.isEmpty()) {
            log.warn("重排服务未启用，返回原始结果");
        }

        return results;
    }

    /**
     * 获取缓存结果
     */
    private List<SearchResult> getCachedResults(String key) {
        // L1: 本地缓存
        List<SearchResult> local = localCache.getIfPresent(key);
        if (local != null) {
            return local;
        }

        // L2: Redis 缓存
        Optional<List<SearchResult>> redis = CacheHelper.getFromRedis(key);
        if (redis.isPresent()) {
            localCache.put(key, redis.get());
            return redis.get();
        }

        return null;
    }

    /**
     * 缓存结果
     */
    private void cacheResults(String key, List<SearchResult> results) {
        localCache.put(key, results);
        CacheHelper.setToRedis(key, results);
    }

    /**
     * 带降级的检索
     */
    private List<SearchResult> searchWithFallback(
        java.util.function.Supplier<List<SearchResult>> supplier,
        List<SearchResult> fallback
    ) {
        try {
            return FallbackHelper.executeWithTimeout(supplier, 3000L);
        } catch (Exception e) {
            log.warn("检索失败，使用降级方案", e);
            return fallback;
        }
    }
}
```

---

## 6. Helper 层设计

### 6.1 FusionHelper - 融合算法助手

```java
package top.codestyle.admin.search.helper;

import lombok.extern.slf4j.Slf4j;
import top.codestyle.admin.search.model.SearchResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 融合算法助手
 * <p>
 * 提供多种检索结果融合策略
 *
 * @author CodeStyle Team
 * @since 2.0.0
 */
@Slf4j
public final class FusionHelper {

    private FusionHelper() {
    }

    /**
     * 加权融合算法
     * <p>
     * 根据配置的权重融合向量和关键词检索结果
     *
     * @param vectorResults   向量检索结果
     * @param keywordResults  关键词检索结果
     * @param vectorWeight    向量检索权重（0-1）
     * @param keywordWeight   关键词检索权重（0-1）
     * @return 融合后的结果
     */
    public static List<SearchResult> weightedFusion(
        List<SearchResult> vectorResults,
        List<SearchResult> keywordResults,
        double vectorWeight,
        double keywordWeight
    ) {
        Map<String, SearchResult> resultMap = new LinkedHashMap<>();

        // 处理向量检索结果
        for (SearchResult result : vectorResults) {
            String id = result.getId();
            double weightedScore = result.getScore() * vectorWeight;

            SearchResult merged = resultMap.get(id);
            if (merged == null) {
                merged = SearchResult.builder()
                    .id(id)
                    .title(result.getTitle())
                    .content(result.getContent())
                    .snippet(result.getSnippet())
                    .metadata(result.getMetadata())
                    .build();
                resultMap.put(id, merged);
            }
            merged.setScore(merged.getScore() + weightedScore);
        }

        // 处理关键词检索结果
        for (SearchResult result : keywordResults) {
            String id = result.getId();
            double weightedScore = result.getScore() * keywordWeight;

            SearchResult merged = resultMap.get(id);
            if (merged == null) {
                merged = SearchResult.builder()
                    .id(id)
                    .title(result.getTitle())
                    .content(result.getContent())
                    .snippet(result.getSnippet())
                    .highlight(result.getHighlight())
                    .metadata(result.getMetadata())
                    .build();
                resultMap.put(id, merged);
            } else {
                merged.setScore(merged.getScore() + weightedScore);
                // 优先使用关键词检索的高亮
                if (result.getHighlight() != null) {
                    merged.setHighlight(result.getHighlight());
                }
            }
        }

        // 按融合分数排序
        return resultMap.values().stream()
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .collect(Collectors.toList());
    }

    /**
     * RRF (Reciprocal Rank Fusion) 融合算法
     * <p>
     * 基于排名的融合，适用于不同检索分数体系的结果融合
     *
     * @param allResults 所有检索结果
     * @return 融合后的结果
     */
    public static List<SearchResult> reciprocalRankFusion(List<SearchResult> allResults) {
        Map<String, Double> scoreMap = new HashMap<>();
        Map<String, SearchResult> resultData = new HashMap<>();

        for (SearchResult result : allResults) {
            String id = result.getId();
            int rank = result.getRank() != null ? result.getRank() : 1;

            // RRF 公式: 1 / (k + rank)，通常 k 取 60
            double rrfScore = 1.0 / (60 + rank);
            scoreMap.merge(id, rrfScore, Double::sum);

            if (!resultData.containsKey(id)) {
                resultData.put(id, result);
            }
        }

        // 按 RRF 分数排序
        return scoreMap.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .map(entry -> {
                SearchResult result = resultData.get(entry.getKey());
                result.setScore(entry.getValue());
                return result;
            })
            .collect(Collectors.toList());
    }
}
```

### 6.2 CacheHelper - 缓存助手

```java
package top.codestyle.admin.search.helper;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;
import top.continew.starter.data.redis.core.utils.RedisUtils;

import java.util.Optional;

/**
 * 缓存助手
 * <p>
 * 提供多级缓存支持：L1(Caffeine) + L2(Redis)
 *
 * @author CodeStyle Team
 * @since 2.0.0
 */
@Slf4j
public final class CacheHelper {

    private static final String CACHE_PREFIX = "search:";
    private static final long CACHE_TTL = 3600L; // 1小时

    private CacheHelper() {
    }

    /**
     * 生成缓存 Key
     *
     * @param type    数据源类型
     * @param request 检索请求
     * @return 缓存 Key
     */
    public static String generateCacheKey(SearchSourceType type, SearchRequest request) {
        String keyContent = String.format("%s:%s:%d:%d:%s",
            type.getCode(),
            request.getQuery(),
            request.getTopK(),
            request.getEnableRerank(),
            JSONUtil.toJsonStr(request.getFilters())
        );
        return CACHE_PREFIX + DigestUtil.md5Hex(keyContent);
    }

    /**
     * 从 Redis 获取缓存
     *
     * @param key 缓存 Key
     * @return 缓存结果
     */
    public static Optional<List<SearchResult>> getFromRedis(String key) {
        String json = RedisUtils.get(key);
        if (StrUtil.isBlank(json)) {
            return Optional.empty();
        }
        try {
            return Optional.of(JSONUtil.toList(json, SearchResult.class));
        } catch (Exception e) {
            log.error("解析缓存结果失败: key={}", key, e);
            return Optional.empty();
        }
    }

    /**
     * 设置 Redis 缓存
     *
     * @param key     缓存 Key
     * @param results 检索结果
     */
    public static void setToRedis(String key, List<SearchResult> results) {
        if (results == null || results.isEmpty()) {
            return;
        }
        try {
            RedisUtils.set(key, JSONUtil.toJsonStr(results), CACHE_TTL);
        } catch (Exception e) {
            log.error("设置缓存失败: key={}", key, e);
        }
    }

    /**
     * 删除指定模式的缓存
     *
     * @param pattern 模式
     */
    public static void deleteByPattern(String pattern) {
        try {
            RedisUtils.deleteByPattern(pattern);
        } catch (Exception e) {
            log.error("删除缓存失败: pattern={}", pattern, e);
        }
    }
}
```

### 6.3 FallbackHelper - 容错助手

```java
package top.codestyle.admin.search.helper;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 容错助手
 * <p>
 * 提供超时控制和降级策略
 *
 * @author CodeStyle Team
 * @since 2.0.0
 */
@Slf4j
public final class FallbackHelper {

    private FallbackHelper() {
    }

    /**
     * 执行带超时的操作
     *
     * @param supplier 操作函数
     * @param timeout 超时时间（毫秒）
     * @param <T>     返回类型
     * @return 操作结果
     * @throws RuntimeException 超时或执行失败
     */
    public static <T> T executeWithTimeout(Supplier<T> supplier, long timeout) {
        try {
            CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier);
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("操作超时: timeout={}ms", timeout, e);
            throw new RuntimeException("操作超时", e);
        }
    }

    /**
     * 执行带降级的操作
     *
     * @param supplier     操作函数
     * @param fallback     降级返回值
     * @param errorMessage 错误消息
     * @param <T>          返回类型
     * @return 操作结果或降级值
     */
    public static <T> T executeWithFallback(
        Supplier<T> supplier,
        T fallback,
        String errorMessage
    ) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.warn("{}: {}", errorMessage, e.getMessage());
            return fallback;
        }
    }
}
```

---

## 7. 配置设计

### 7.1 SearchProperties - 检索配置属性

```java
package top.codestyle.admin.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 检索配置属性
 *
 * @author CodeStyle Team
 * @since 2.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "search")
public class SearchProperties {

    /**
     * 是否启用检索模块
     */
    private Boolean enabled = true;

    /**
     * Elasticsearch 配置
     */
    private ElasticsearchConfig elasticsearch = new ElasticsearchConfig();

    /**
     * Milvus 配置
     */
    private MilvusConfig milvus = new MilvusConfig();

    /**
     * 重排配置
     */
    private RerankConfig rerank = new RerankConfig();

    /**
     * 缓存配置
     */
    private CacheConfig cache = new CacheConfig();

    @Data
    public static class ElasticsearchConfig {
        private Boolean enabled = true;
        private String[] hosts = {"localhost:9200"};
        private String username;
        private String password;
        private String indexPrefix = "codestyle";
        private Integer timeout = 5000;
    }

    @Data
    public static class MilvusConfig {
        private Boolean enabled = true;
        private String host = "localhost";
        private Integer port = 19530;
        private String collection = "codestyle_vectors";
        private Integer dimension = 1024;
        private Boolean keywordEnabled = true;
    }

    @Data
    public static class RerankConfig {
        private Boolean enabled = false;
        private String apiUrl;
        private String model = "BAAI/bge-reranker-v2-m3";
        private Integer topK = 10;
    }

    @Data
    public static class CacheConfig {
        private Boolean enabled = true;
        private Long localCacheTtl = 300L;    // 5分钟
        private Long redisCacheTtl = 3600L;   // 1小时
        private Integer localCacheMaxSize = 1000;
    }
}
```

---

## 8. 检索服务实现

### 8.1 ElasticsearchSearchServiceImpl 实现

```java
package top.codestyle.admin.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.codestyle.admin.search.config.SearchProperties;
import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;
import top.codestyle.admin.search.service.ElasticsearchSearchService;
import top.continew.starter.core.exception.BusinessException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch 检索服务实现
 * <p>
 * 支持全文检索、多字段加权、高亮显示
 *
 * @author CodeStyle Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchSearchServiceImpl implements ElasticsearchSearchService {

    private final ElasticsearchClient esClient;
    private final SearchProperties properties;

    @Override
    public List<SearchResult> search(SearchRequest request) {
        if (!properties.getElasticsearch().getEnabled()) {
            log.warn("Elasticsearch 检索未启用");
            return List.of();
        }

        try {
            // 构建查询
            Query query = buildQuery(request);

            // 构建 ES 搜索请求
            SearchRequest esRequest = SearchRequest.of(s -> s
                .index(properties.getElasticsearch().getIndexPrefix() + "_*")
                .query(query)
                .size(request.getTopK())
                .highlight(h -> h
                    .fields("title", hf -> hf
                        .preTags("<em>")
                        .postTags("</em>")
                        .fragmentSize(150)
                        .numberOfFragments(1)
                    )
                    .fields("content", hf -> hf
                        .preTags("<em>")
                        .postTags("</em>")
                        .fragmentSize(200)
                        .numberOfFragments(2)
                    )
                )
            );

            // 执行查询
            long startTime = System.currentTimeMillis();
            SearchResponse<Map> response = esClient.search(esRequest, Map.class);
            long duration = System.currentTimeMillis() - startTime;

            log.info("ES 检索完成: query={}, 耗时={}ms, 结果数={}",
                request.getQuery(), duration, response.hits().total().value());

            // 转换结果
            return convertResults(response);

        } catch (IOException e) {
            log.error("ES 检索失败: query={}", request.getQuery(), e);
            throw new BusinessException("检索服务暂时不可用");
        }
    }

    /**
     * 构建查询条件
     */
    private Query buildQuery(SearchRequest request) {
        // 多字段加权查询: title^3, content^2, tags
        return Query.of(q -> q
            .multiMatch(m -> m
                .query(request.getQuery())
                .fields("title^3", "content^2", "tags")
                .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                .minimumShouldMatch("50%")
            )
        );
    }

    /**
     * 转换 ES 结果为 SearchResult
     */
    private List<SearchResult> convertResults(SearchResponse<Map> response) {
        List<SearchResult> results = new ArrayList<>();
        int rank = 1;

        for (Hit<Map> hit : response.hits().hits()) {
            Map<String, Object> source = hit.source();

            SearchResult result = SearchResult.builder()
                .id(hit.id())
                .sourceType(SearchSourceType.ELASTICSEARCH)
                .title(getString(source, "title"))
                .content(getString(source, "content"))
                .snippet(getString(source, "snippet"))
                .score(hit.score())
                .rank(rank++)
                .metadata(extractMetadata(source))
                .build();

            // 提取高亮
            if (hit.highlight() != null) {
                result.setHighlight(extractHighlight(hit.highlight()));
            }

            results.add(result);
        }

        return results;
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Map<String, Object> extractMetadata(Map<String, Object> source) {
        // 提取元数据字段（非标题、内容字段）
        return source.entrySet().stream()
            .filter(e -> !List.of("id", "title", "content", "snippet").contains(e.getKey()))
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
    }

    private String extractHighlight(Map<String, List<String>> highlights) {
        StringBuilder sb = new StringBuilder();
        highlights.values().stream()
            .limit(3)
            .forEach(list -> list.forEach(sb::append));
        return sb.length() > 0 ? sb.toString() : null;
    }
}
```

### 8.2 MilvusSearchServiceImpl 实现

```java
package top.codestyle.admin.search.service.impl;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.SearchParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.SearchResultWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import top.codestyle.admin.search.config.SearchProperties;
import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;
import top.codestyle.admin.search.service.EmbeddingService;
import top.codestyle.admin.search.service.MilvusSearchService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Milvus 向量检索服务实现
 * <p>
 * 支持语义相似度检索
 *
 * @author CodeStyle Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.milvus", name = "enabled", havingValue = "true")
public class MilvusSearchServiceImpl implements MilvusSearchService {

    private final MilvusServiceClient milvusClient;
    private final EmbeddingService embeddingService;
    private final SearchProperties properties;

    @Override
    public List<SearchResult> search(SearchRequest request) {
        try {
            // 1. 生成查询向量
            float[] queryVector = embeddingService.encode(request.getQuery());

            // 2. 构建 Milvus 查询
            SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(properties.getMilvus().getCollection())
                .withVectorFieldName("embedding")
                .withVectors(Collections.singletonList(queryVector))
                .withTopK(request.getTopK())
                .withMetricType(io.milvus.common.utils.ParamUtils.MetricType.COSINE)
                .build();

            // 3. 执行查询
            long startTime = System.currentTimeMillis();
            SearchResultWrapper response = milvusClient.search(searchParam);
            long duration = System.currentTimeMillis() - startTime;

            log.info("Milvus 检索完成: query={}, 耗时={}ms, 结果数={}",
                request.getQuery(), duration, response.getSearchResults().size());

            // 4. 转换结果
            return convertResults(response);

        } catch (Exception e) {
            log.error("Milvus 检索失败: query={}", request.getQuery(), e);
            return List.of();
        }
    }

    @Override
    public float[] textToVector(String text) {
        return embeddingService.encode(text);
    }

    private List<SearchResult> convertResults(SearchResultWrapper response) {
        List<SearchResult> results = new ArrayList<>();
        var searchResults = response.getSearchResults();

        for (var ids = searchResults.getIDScore(0); ids != null && ids.hasNext(); ) {
            var idScore = ids.next();
            String id = idScore.getLongID().toString();
            float score = idScore.getScore();

            // 需要从 Milvus 获取完整文档内容
            // 这里简化处理，实际应通过 ID 查询或使用 expr 过滤

            SearchResult result = SearchResult.builder()
                .id(id)
                .sourceType(SearchSourceType.MILVUS)
                .score((double) score)
                .build();

            results.add(result);
        }

        return results;
    }
}
```

---

## 9. SPI 扩展示例

### 9.1 WikiSearchProvider - 自定义数据源示例

```java
package top.codestyle.admin.search.spi;

import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;

import java.util.List;

/**
 * Wiki 数据源检索示例
 * <p>
 * 自定义数据源实现 SearchProvider 接口，
 * 通过 SPI 注册即可自动被 SearchExecutor 发现和使用
 *
 * @author CodeStyle Team
 * @since 2.0.0
 */
public class WikiSearchProvider implements SearchProvider {

    @Override
    public boolean supports(SearchSourceType type) {
        return SearchSourceType.CUSTOM == type;
    }

    @Override
    public List<SearchResult> search(SearchRequest request) {
        // 实现自定义检索逻辑
        // 例如：调用 Wiki API、数据库查询等

        // 示例：返回模拟数据
        return List.of(
            SearchResult.builder()
                .id("wiki-001")
                .sourceType(SearchSourceType.CUSTOM)
                .title("Wiki 文档示例")
                .content("这是来自 Wiki 的检索结果")
                .score(0.95)
                .build()
        );
    }

    @Override
    public String getName() {
        return "WikiSearchProvider";
    }

    @Override
    public int getPriority() {
        return 50; // 较高优先级
    }
}
```

### 9.2 SPI 注册文件

`META-INF/services/top.codestyle.admin.search.spi.SearchProvider`：

```
top.codestyle.admin.search.spi.WikiSearchProvider
```

---

## 10. 配置文件

### 10.1 application-search.yml

```yaml
# application-search.yml
search:
  # 是否启用检索模块
  enabled: true

  # Elasticsearch 配置
  elasticsearch:
    enabled: true
    hosts:
      - localhost:9200
    username: elastic
    password: ${ES_PASSWORD:password}
    index-prefix: codestyle
    timeout: 5000

  # Milvus 配置
  milvus:
    enabled: true
    host: localhost
    port: 19530
    collection: codestyle_vectors
    dimension: 1024
    # 是否启用 Milvus 关键词检索
    keyword-enabled: true

  # 重排配置
  rerank:
    enabled: false
    api-url: http://localhost:8001/rerank
    model: BAAI/bge-reranker-v2-m3
    top-k: 10

  # 缓存配置
  cache:
    enabled: true
    local-cache-ttl: 300      # 本地缓存 5 分钟
    redis-cache-ttl: 3600     # Redis 缓存 1 小时
    local-cache-max-size: 1000 # 本地缓存最大 1000 条
```

---

## 11. REST API 设计

### 11.1 SearchController - API 控制器

```java
package top.codestyle.admin.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;
import top.codestyle.admin.search.service.SearchService;
import top.continew.starter.web.model.R;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 检索 API 控制器
 *
 * @author CodeStyle Team
 * @since 2.0.0
 */
@Tag(name = "检索 API")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 单源检索
     *
     * @param request 检索请求
     * @return 检索结果
     */
    @Operation(summary = "单源检索", description = "支持 ES、Milvus、MilvusKeyword、CUSTOM")
    @PostMapping("/single")
    public R<List<SearchResult>> singleSearch(@Valid @RequestBody SearchRequest request) {
        List<SearchResult> results = searchService.search(request.getSourceType(), request);
        return R.ok(results);
    }

    /**
     * ES 全文检索（关键词检索）
     */
    @Operation(summary = "Elasticsearch 全文检索")
    @PostMapping("/es")
    public R<List<SearchResult>> esSearch(@Valid @RequestBody SearchRequest request) {
        request.setSourceType(SearchSourceType.ELASTICSEARCH);
        List<SearchResult> results = searchService.search(SearchSourceType.ELASTICSEARCH, request);
        return R.ok(results);
    }

    /**
     * Milvus 向量检索
     */
    @Operation(summary = "Milvus 向量检索")
    @PostMapping("/milvus")
    public R<List<SearchResult>> milvusSearch(@Valid @RequestBody SearchRequest request) {
        request.setSourceType(SearchSourceType.MILVUS);
        List<SearchResult> results = searchService.search(SearchSourceType.MILVUS, request);
        return R.ok(results);
    }

    /**
     * Milvus 关键词检索
     */
    @Operation(summary = "Milvus 关键词检索")
    @PostMapping("/milvus-keyword")
    public R<List<SearchResult>> milvusKeywordSearch(@Valid @RequestBody SearchRequest request) {
        request.setSourceType(SearchSourceType.MILVUS_KEYWORD);
        List<SearchResult> results = searchService.search(SearchSourceType.MILVUS_KEYWORD, request);
        return R.ok(results);
    }

    /**
     * 混合检索（向量 + 关键词）
     */
    @Operation(summary = "混合检索", description = "融合向量检索和关键词检索结果")
    @PostMapping("/hybrid")
    public R<List<SearchResult>> hybridSearch(@Valid @RequestBody SearchRequest request) {
        List<SearchResult> results = searchService.hybridSearch(request);
        return R.ok(results);
    }

    /**
     * 检索并重排
     */
    @Operation(summary = "检索并重排", description = "执行混合检索后使用 BGE-Rerank 重排序")
    @PostMapping("/rerank")
    public R<List<SearchResult>> searchWithRerank(@Valid @RequestBody SearchRequest request) {
        request.setEnableRerank(true);
        List<SearchResult> results = searchService.searchWithRerank(request);
        return R.ok(results);
    }
}
```

### 11.2 API 说明

| API 端点 | 说明 | 检索方式 |
|----------|------|----------|
| `POST /api/search/single` | 单源检索 | 根据请求中的 sourceType 执行 |
| `POST /api/search/es` | ES 全文检索 | 关键词检索 |
| `POST /api/search/milvus` | Milvus 向量检索 | 向量检索（语义相似度） |
| `POST /api/search/milvus-keyword` | Milvus 关键词检索 | Milvus 关键词检索 |
| `POST /api/search/hybrid` | 混合检索 | 向量 + 关键词加权融合 |
| `POST /api/search/rerank` | 检索并重排 | 混合检索 + BGE-Rerank 重排 |

---

## 12. 实现计划

### 阶段一：基础架构（1 周）

**目标**：搭建项目结构，定义核心接口

**任务清单**：
- [ ] 创建 `spi.SearchProvider` 接口
- [ ] 创建 `executor.SearchExecutor` 核心执行器
- [ ] 定义数据模型（SearchRequest, SearchResult, SearchSourceType）
- [ ] 实现配置类（SearchProperties）
- [ ] 实现缓存助手（CacheHelper）
- [ ] 实现容错助手（FallbackHelper）
- [ ] 实现融合算法助手（FusionHelper）

**交付物**：
- SPI 扩展机制可用
- 核心执行器完成
- 基础配置完成

### 阶段二：Elasticsearch 集成（1 周）

**目标**：实现 ES 全文检索

**任务清单**：
- [ ] 实现 `ElasticsearchSearchServiceImpl`
- [ ] 实现 `ElasticsearchConfig` 配置类
- [ ] 实现多字段加权查询
- [ ] 实现高亮显示
- [ ] 单元测试

**交付物**：
- ES 检索功能可用
- 支持全文检索和高亮

### 阶段三：Milvus 集成（1 周）

**目标**：实现向量检索和关键词检索

**任务清单**：
- [ ] 实现 `MilvusSearchServiceImpl`
- [ ] 实现 `MilvusConfig` 配置类
- [ ] 集成 Embedding 服务
- [ ] 实现 Milvus 关键词检索（可选）
- [ ] 单元测试

**交付物**：
- 向量检索功能可用
- 支持 Milvus 关键词检索

### 阶段四：混合检索与重排（1 周）

**目标**：实现混合检索和重排序

**任务清单**：
- [ ] 完善 `SearchService` 的混合检索逻辑
- [ ] 实现 `RerankServiceImpl`
- [ ] 集成 BGE-Rerank API
- [ ] 实现加权融合算法
- [ ] 实现 RRF 融合算法
- [ ] 集成测试

**交付物**：
- 混合检索功能可用
- 重排功能可用

### 阶段五：API 与文档（1 周）

**目标**：完善 REST API 和文档

**任务清单**：
- [ ] 实现 `SearchController`
- [ ] 编写 API 文档（Swagger）
- [ ] 编写使用文档
- [ ] 编写 SPI 扩展指南
- [ ] 编写配置说明

**交付物**：
- REST API 可用
- 完整文档

### 阶段六：测试与优化（1 周）

**目标**：完善测试和性能优化

**任务清单**：
- [ ] 单元测试（覆盖率 > 80%）
- [ ] 集成测试
- [ ] 性能测试和调优
- [ ] 压力测试
- [ ] 监控指标

**交付物**：
- 完整的测试套件
- 性能优化完成

---

## 13. 目录结构

```
codestyle-plugin-search/
├── src/main/java/top/codestyle/admin/search/
│   ├── spi/
│   │   └── SearchProvider.java              # SPI 扩展接口
│   ├── executor/
│   │   └── SearchExecutor.java              # 核心执行器
│   ├── service/
│   │   ├── SearchService.java               # 统一检索服务
│   │   ├── ElasticsearchSearchService.java  # ES 检索服务
│   │   ├── MilvusSearchService.java        # Milvus 检索服务
│   │   ├── EmbeddingService.java           # Embedding 服务
│   │   ├── RerankService.java             # 重排服务
│   │   └── impl/
│   │       ├── SearchServiceImpl.java
│   │       ├── ElasticsearchSearchServiceImpl.java
│   │       ├── MilvusSearchServiceImpl.java
│   │       └── RerankServiceImpl.java
│   ├── helper/
│   │   ├── FusionHelper.java               # 融合算法助手
│   │   ├── CacheHelper.java               # 缓存助手
│   │   └── FallbackHelper.java            # 容错助手
│   ├── model/
│   │   ├── SearchRequest.java
│   │   ├── SearchResult.java
│   │   └── SearchSourceType.java
│   ├── config/
│   │   ├── SearchProperties.java
│   │   ├── ElasticsearchConfig.java
│   │   ├── MilvusConfig.java
│   │   └── CacheConfig.java
│   ├── controller/
│   │   └── SearchController.java
│   └── client/
│       ├── RerankClient.java
│       └── EmbeddingClient.java
├── src/main/resources/
│   ├── META-INF/services/
│   │   └── top.codestyle.admin.search.spi.SearchProvider  # SPI 注册文件
│   └── application-search.yml
└── docs/
    ├── API.md
    ├── SPI_EXTENSION.md
    └── DEPLOYMENT.md
```

---

## 14. API 使用示例

### 14.1 cURL 示例

```bash
# 1. ES 全文检索（关键词检索）
curl -X POST http://localhost:8080/api/search/es \
  -H "Content-Type: application/json" \
  -d '{
    "query": "如何配置 MySQL 连接池",
    "topK": 10
  }'

# 2. Milvus 向量检索（语义相似度）
curl -X POST http://localhost:8080/api/search/milvus \
  -H "Content-Type: application/json" \
  -d '{
    "query": "数据库连接池配置",
    "topK": 10
  }'

# 3. Milvus 关键词检索
curl -X POST http://localhost:8080/api/search/milvus-keyword \
  -H "Content-Type: application/json" \
  -d '{
    "query": "MySQL 连接池",
    "topK": 10
  }'

# 4. 混合检索（向量 60% + 关键词 40%）
curl -X POST http://localhost:8080/api/search/hybrid \
  -H "Content-Type: application/json" \
  -d '{
    "query": "MySQL 连接池",
    "topK": 10,
    "vectorWeight": 0.6,
    "keywordWeight": 0.4
  }'

# 5. 检索并重排
curl -X POST http://localhost:8080/api/search/rerank \
  -H "Content-Type: application/json" \
  -d '{
    "query": "数据库配置",
    "topK": 10,
    "enableRerank": true
  }'
```

### 14.2 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "doc-001",
      "sourceType": "HYBRID",
      "title": "MySQL 连接池配置最佳实践",
      "snippet": "MySQL 连接池配置对于应用性能至关重要...",
      "content": "完整的文档内容...",
      "score": 0.95,
      "rank": 1,
      "highlight": "<em>MySQL 连接池</em>配置...",
      "metadata": {
        "author": "张三",
        "updatedAt": "2026-01-30"
      }
    }
  ]
}
```

---

## 15. 设计总结

### 15.1 核心特性

| 特性 | 说明 |
|------|------|
| **向量检索** | 通过 Milvus 实现语义相似度检索 |
| **关键词检索** | 支持 Elasticsearch 全文检索和 Milvus 关键词检索 |
| **混合检索** | 加权融合向量和关键词检索结果 |
| **轻量级扩展** | 通过 SPI 机制支持运行时动态注册新数据源 |
| **保持风格** | 遵循 CodeStyle 项目的命名和分层规范 |
| **高性能** | 多级缓存、并行查询、融合优化 |
| **高可用** | 超时控制、降级策略、重试机制 |

### 15.2 技术亮点

1. **混合架构模式**：融合了 AssistantAgent 的 SPI 扩展能力与 CodeStyle 的 Service + Helper 模式
2. **轻量级扩展**：仅在数据源扩展时使用 SPI，保持架构简洁
3. **灵活融合策略**：支持加权融合和 RRF 融合两种策略
4. **多级缓存**：L1 (Caffeine) + L2 (Redis) 两级缓存，提升性能
5. **完善的容错**：超时控制、降级策略、重试机制，保障服务可用性

---

## 📚 参考资料

- [AssistantAgent 架构](../../reference/AssistantAgent/README_zh.md)
- [CodeStyle 最佳实践](../../CODESTYLE_BEST_PRACTICES.md)
- [Elasticsearch 官方文档](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- [Milvus 官方文档](https://milvus.io/docs)
- [BGE-Rerank 模型](https://huggingface.co/BAAI/bge-reranker-v2-m3)

---

**文档维护**: CodeStyle Team
**最后更新**: 2026-01-30
