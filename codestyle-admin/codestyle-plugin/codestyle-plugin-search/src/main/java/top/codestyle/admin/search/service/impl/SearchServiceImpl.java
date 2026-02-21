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

package top.codestyle.admin.search.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.codestyle.admin.search.config.SearchProperties;
import top.codestyle.admin.search.helper.CacheHelper;
import top.codestyle.admin.search.helper.FallbackHelper;
import top.codestyle.admin.search.helper.FusionHelper;
import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.service.ElasticsearchSearchService;
import top.codestyle.admin.search.service.MilvusSearchService;
import top.codestyle.admin.search.service.SearchService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * 检索服务实现（简化版）
 * <p>
 * 核心职责：
 * 1. 缓存管理（L1 Caffeine + L2 Redis）
 * 2. 并行执行 ES 和 Milvus 检索
 * 3. RRF 融合结果
 * 4. 容错处理
 *
 * @author CodeStyle Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchSearchService esSearchService;
    private final Optional<MilvusSearchService> milvusSearchService;
    private final Cache<String, List<SearchResult>> localCache;
    private final SearchProperties properties;

    /**
     * 混合检索（唯一方法）
     * <p>
     * 执行流程：
     * 1. 检查缓存（L1 → L2）
     * 2. 并行执行 ES + Milvus 检索
     * 3. RRF 融合结果
     * 4. 写入缓存
     * 5. 返回结果
     */
    @Override
    public List<SearchResult> search(SearchRequest request) {
        // 1. 生成缓存 Key
        String cacheKey = CacheHelper.generateCacheKey(request);
        
        // 2. 检查缓存
        List<SearchResult> cached = getCachedResults(cacheKey);
        if (cached != null) {
            log.debug("命中缓存: {}, 返回 {} 条结果", cacheKey, cached.size());
            return cached;
        }

        // 3. 执行混合检索
        log.info("开始混合检索，查询: {}, topK: {}", request.getQuery(), request.getTopK());
        List<SearchResult> allResults = executeHybridSearch(request);

        // 4. RRF 融合
        List<SearchResult> fused = FusionHelper.reciprocalRankFusion(allResults);
        
        // 5. 写入缓存
        if (!fused.isEmpty()) {
            cacheResults(cacheKey, fused);
        }
        
        log.info("混合检索完成，查询: {}, 返回 {} 条结果", request.getQuery(), fused.size());
        return fused;
    }

    /**
     * 执行混合检索（ES + Milvus 并行）
     */
    private List<SearchResult> executeHybridSearch(SearchRequest request) {
        List<CompletableFuture<List<SearchResult>>> futures = new ArrayList<>();

        // ES 检索
        futures.add(CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("开始 ES 检索");
                return FallbackHelper.executeWithFallback(
                    () -> esSearchService.search(request),
                    Collections.emptyList()
                );
            } catch (Exception e) {
                log.error("ES 检索失败", e);
                return Collections.emptyList();
            }
        }));

        // Milvus 检索（如果启用）
        if (milvusSearchService.isPresent() && properties.getMilvus().getEnabled()) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    log.debug("开始 Milvus 检索");
                    return FallbackHelper.executeWithFallback(
                        () -> milvusSearchService.get().search(request),
                        Collections.emptyList()
                    );
            } catch (Exception e) {
                    log.error("Milvus 检索失败", e);
                    return Collections.emptyList();
            }
            }));
        }

        // 等待所有检索完成（带超时）
        return futures.stream()
            .map(future -> {
                try {
                    return future.get(request.getTimeout(), TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    log.warn("检索超时: {}ms", request.getTimeout());
                    return Collections.<SearchResult>emptyList();
                } catch (Exception e) {
                    log.error("检索异常", e);
                    return Collections.<SearchResult>emptyList();
                }
            })
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    /**
     * 获取缓存结果（L1 → L2）
     */
    private List<SearchResult> getCachedResults(String key) {
        // L1: 本地缓存
        List<SearchResult> local = localCache.getIfPresent(key);
        if (local != null) {
            log.debug("命中本地缓存 (L1)");
            return local;
        }

        // L2: Redis 缓存
        Optional<List<SearchResult>> redis = CacheHelper.getFromRedis(key);
        if (redis.isPresent()) {
            log.debug("命中 Redis 缓存 (L2)");
            // 回填本地缓存
            localCache.put(key, redis.get());
            return redis.get();
        }

        return null;
    }

    /**
     * 写入缓存（L1 + L2）
     */
    private void cacheResults(String key, List<SearchResult> results) {
        // 写入本地缓存
        localCache.put(key, results);
        
        // 写入 Redis 缓存
        try {
        CacheHelper.setToRedis(key, results);
            log.debug("写入缓存成功: {}", key);
        } catch (Exception e) {
            log.warn("写入 Redis 缓存失败", e);
        }
    }
}
