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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.codestyle.admin.search.executor.SearchExecutor;
import top.codestyle.admin.search.helper.CacheHelper;
import top.codestyle.admin.search.helper.FallbackHelper;
import top.codestyle.admin.search.helper.FusionHelper;
import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;
import top.codestyle.admin.search.service.RerankService;
import top.codestyle.admin.search.service.SearchService;

/**
 * 检索服务实现
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchExecutor searchExecutor;
    private final Optional<RerankService> rerankService;
    private final Cache<String, List<SearchResult>> localCache;

    @Override
    public List<SearchResult> search(SearchSourceType type, SearchRequest request) {
        // 1. 检查缓存
        String cacheKey = CacheHelper.generateCacheKey(type, request);
        List<SearchResult> cached = getCachedResults(cacheKey);
        if (cached != null) {
            log.debug("命中缓存，返回缓存结果");
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

    @Override
    public List<SearchResult> hybridSearch(SearchRequest request) {
        log.info("执行混合检索，查询: {}", request.getQuery());

        // 1. 检查缓存
        String cacheKey = CacheHelper.generateCacheKey(SearchSourceType.HYBRID, request);
        List<SearchResult> cached = getCachedResults(cacheKey);
        if (cached != null) {
            log.debug("命中混合检索缓存");
            return cached;
        }

        // 2. 并行执行向量检索和关键词检索
        List<SearchResult> vectorResults = searchWithFallback(() -> searchExecutor
            .execute(SearchSourceType.MILVUS, request), Collections.emptyList());

        List<SearchResult> keywordResults = searchWithFallback(() -> searchExecutor
            .execute(SearchSourceType.ELASTICSEARCH, request), Collections.emptyList());

        // 3. 加权融合
        List<SearchResult> fusedResults = FusionHelper.weightedFusion(vectorResults, keywordResults, request
            .getVectorWeight(), request.getKeywordWeight());

        // 4. 写入缓存
        cacheResults(cacheKey, fusedResults);

        log.info("混合检索完成，返回 {} 条结果", fusedResults.size());
        return fusedResults.stream().limit(request.getTopK()).collect(Collectors.toList());
    }

    @Override
    public List<SearchResult> searchWithRerank(SearchRequest request) {
        log.info("执行检索并重排，查询: {}", request.getQuery());

        // 先执行混合检索
        List<SearchResult> results = hybridSearch(request);

        // 如果启用重排且有重排服务
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
    private List<SearchResult> searchWithFallback(java.util.function.Supplier<List<SearchResult>> supplier,
                                                  List<SearchResult> fallback) {
        try {
            return FallbackHelper.executeWithTimeout(supplier, 3000L)
                .get(3000L, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("检索失败，使用降级方案", e);
            return fallback;
        }
    }
}
