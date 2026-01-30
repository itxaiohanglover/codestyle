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

package top.codestyle.admin.search.helper;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;
import top.continew.starter.cache.redisson.util.RedisUtils;

/**
 * 缓存助手
 * <p>
 * 提供检索结果的缓存管理功能
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
public class CacheHelper {

    private static final String CACHE_PREFIX = "search:";
    private static final long CACHE_TTL = 3600L; // 1 小时

    /**
     * 生成缓存 Key
     *
     * @param request 检索请求
     * @return 缓存 Key
     */
    public static String generateCacheKey(SearchRequest request) {
        String content = String.format("%s:%s:%d", request.getSourceType(), request.getQuery(), request.getTopK());
        return CACHE_PREFIX + DigestUtil.md5Hex(content);
    }

    /**
     * 生成缓存 Key
     *
     * @param type    数据源类型
     * @param request 检索请求
     * @return 缓存 Key
     */
    public static String generateCacheKey(SearchSourceType type, SearchRequest request) {
        String content = String.format("%s:%s:%d:%b:%s", type.getCode(), request.getQuery(), request.getTopK(), request
            .getEnableRerank(), JSONUtil.toJsonStr(request.getFilters()));
        return CACHE_PREFIX + DigestUtil.md5Hex(content);
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
        return Optional.of(JSONUtil.toList(json, SearchResult.class));
    }

    /**
     * 设置 Redis 缓存
     *
     * @param key     缓存 Key
     * @param results 检索结果
     */
    public static void setToRedis(String key, List<SearchResult> results) {
        RedisUtils.set(key, JSONUtil.toJsonStr(results), Duration.ofSeconds(CACHE_TTL));
    }

    /**
     * 清除缓存
     *
     * @param pattern 缓存 Key 模式
     */
    public static void evictCache(String pattern) {
        RedisUtils.deleteByPattern(CACHE_PREFIX + pattern);
    }

    /**
     * 清除所有检索缓存
     */
    public static void evictAllCache() {
        RedisUtils.deleteByPattern(CACHE_PREFIX + "*");
    }
}
