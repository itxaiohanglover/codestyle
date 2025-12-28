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

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import top.codestyle.admin.search.config.SearchProperties;
import top.codestyle.admin.search.service.SearchCacheService;
import top.codestyle.admin.search.vo.RemoteMetaConfigVO;

import java.util.Iterator;
import java.util.Optional;

/**
 * 基于JetCache的搜索缓存服务实现类
 * 支持多级缓存（Caffeine本地缓存 + Redis分布式缓存）和热点数据识别
 * 
 * 注意：此实现优先于RedisSearchCacheServiceImpl使用（通过@Primary或条件注解）
 * 
 * @author chonghaoGao
 * @date 2025/12/23
 */
@Slf4j
@Service("jetCacheSearchCacheService")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "codestyle.search.enabled", havingValue = "true", matchIfMissing = true)
public class JetCacheSearchCacheServiceImpl implements SearchCacheService {

    private final SearchProperties searchProperties;
    private final HotKeyDetectionServiceImpl hotKeyDetectionService;

    /**
     * JetCache多级缓存实例
     * L1: Caffeine本地缓存（1000条，2小时）
     * L2: Redis分布式缓存（无限制，2小时）
     * 热点数据自动延长TTL
     */
    /**
     * JetCache多级缓存实例
     * 注意：@CreateCache注解的expire是固定的，如果需要动态TTL，需要在put时使用put(key, value, expire, timeUnit)
     */
    @CreateCache(name = "searchResultCache", cacheType = CacheType.BOTH,  // 多级缓存（L1: Caffeine + L2: Redis）
                 localLimit = 1000,            // 本地缓存最大1000条
                 expire = 7200,                // 默认2小时（秒），实际会根据热点数据动态调整
                 timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    private Cache<String, RemoteMetaConfigVO> searchResultCache;

    private static final String CACHE_KEY_PREFIX = "search:result:";

    @Override
    public Optional<RemoteMetaConfigVO> getFromCache(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Optional.empty();
        }

        // 如果缓存未初始化，直接返回空（降级到ES查询）
        if (searchResultCache == null) {
            log.debug("缓存未初始化，降级到ES查询: query={}", query);
            return Optional.empty();
        }

        try {
            String cacheKey = generateCacheKey(query);
            RemoteMetaConfigVO result = searchResultCache.get(cacheKey);

            if (result != null) {
                // 记录缓存命中，更新热点统计
                hotKeyDetectionService.recordAccess(query);
                log.debug("缓存命中: query={}", query);
                return Optional.of(result);
            }

            log.debug("缓存未命中: query={}", query);
            return Optional.empty();

        } catch (Exception e) {
            log.error("从缓存获取数据失败: query={}", query, e);
            // 缓存异常时返回空，降级到ES查询
            return Optional.empty();
        }
    }

    @Override
    public void putToCache(String query, RemoteMetaConfigVO result) {
        if (query == null || query.trim().isEmpty() || result == null) {
            return;
        }

        // 如果缓存未初始化，直接返回（不写入缓存）
        if (searchResultCache == null) {
            log.debug("缓存未初始化，跳过写入: query={}", query);
            return;
        }

        try {
            String cacheKey = generateCacheKey(query);

            // 判断是否为热点数据，动态调整TTL
            long ttlSeconds = calculateTTL(query);

            // 使用JetCache的put方法，支持自定义TTL
            // 注意：JetCache的put(key, value, expire, timeUnit)方法支持动态TTL
            searchResultCache.put(cacheKey, result, ttlSeconds, java.util.concurrent.TimeUnit.SECONDS);

            if (hotKeyDetectionService.isHotKey(query)) {
                log.debug("热点数据缓存: query={}, ttl={}秒", query, ttlSeconds);
            } else {
                log.debug("普通数据缓存: query={}, ttl={}秒", query, ttlSeconds);
            }

        } catch (Exception e) {
            log.error("写入缓存失败: query={}", query, e);
            // 缓存写入失败不影响主流程
        }
    }

    @Override
    public void removeFromCache(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }

        // 如果缓存未初始化，直接返回
        if (searchResultCache == null) {
            return;
        }

        try {
            String cacheKey = generateCacheKey(query);
            searchResultCache.remove(cacheKey);
            log.debug("删除缓存: query={}", query);
        } catch (Exception e) {
            log.error("删除缓存失败: query={}", query, e);
        }
    }

    @Override
    public void batchRemoveFromCache(Iterator<String> queries) {
        if (queries == null) {
            return;
        }

        int count = 0;
        while (queries.hasNext()) {
            String query = queries.next();
            removeFromCache(query);
            count++;
        }

        log.info("批量删除缓存完成: count={}", count);
    }

    /**
     * 生成缓存键
     * 
     * @param query 搜索关键词
     * @return 缓存键
     */
    private String generateCacheKey(String query) {
        // 规范化查询关键词（去除空格、统一小写）
        String normalizedQuery = normalizeQuery(query);
        return CACHE_KEY_PREFIX + normalizedQuery;
    }

    /**
     * 规范化查询关键词
     * 
     * @param query 原始查询关键词
     * @return 规范化后的查询关键词
     */
    private String normalizeQuery(String query) {
        if (query == null) {
            return "";
        }
        // 去除首尾空格，转为小写，去除多余空格
        return query.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    /**
     * 计算缓存TTL（秒）
     * 热点数据使用更长的TTL
     * 
     * @param query 搜索关键词
     * @return TTL（秒）
     */
    private long calculateTTL(String query) {
        if (hotKeyDetectionService.isHotKey(query)) {
            // 热点数据：使用热点缓存过期时间
            return searchProperties.getHotKeyCacheExpire() != null ? searchProperties.getHotKeyCacheExpire() : 14400L; // 默认4小时
        } else {
            // 普通数据：使用默认缓存过期时间
            return searchProperties.getCacheExpire() != null ? searchProperties.getCacheExpire() : 3600L; // 默认1小时
        }
    }
}
