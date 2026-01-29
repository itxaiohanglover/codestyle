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

package top.codestyle.admin.search.util;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class CacheKeyGenerator {
    private static final String CACHE_KEY_PREFIX = "search:result:";

    /**
     * 生成唯一缓存键
     * 
     * @param query   搜索关键词
     * @param filters 过滤条件
     * @param sort    排序字段
     * @param page    页码
     * @param size    每页大小
     * @return 缓存键
     */
    public String generateCacheKey(String query, Map<String, Object> filters, String sort, int page, int size) {
        // 规范化查询参数
        String normalizedQuery = normalizeQuery(query);
        String normalizedFilters = normalizeFilters(filters);
        String normalizedSort = normalizeSort(sort);

        // 构建缓存键
        return String
            .format("%s%s:%s:%s:%d:%d", CACHE_KEY_PREFIX, normalizedQuery, normalizedFilters, normalizedSort, page, size);
    }

    /**
     * 生成简单缓存键（仅基于查询关键词）
     * 
     * @param query 搜索关键词
     * @return 缓存键
     */
    public String generateSimpleCacheKey(String query) {
        String normalizedQuery = normalizeQuery(query);
        return CACHE_KEY_PREFIX + normalizedQuery;
    }

    /**
     * 规范化查询关键词
     * 
     * @param query 搜索关键词
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
     * 规范化过滤条件
     * 
     * @param filters 过滤条件
     * @return 规范化后的过滤条件
     */
    private String normalizeFilters(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return "none";
        }
        // 排序Map以确保生成的键一致
        TreeMap<String, Object> sortedFilters = new TreeMap<>(filters);
        // 转换为字符串，处理数组值
        return sortedFilters.entrySet().stream().map(entry -> {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Object[]) {
                return key + ":" + Arrays.stream((Object[])value)
                    .map(String::valueOf)
                    .sorted()
                    .collect(Collectors.joining(","));
            } else if (value instanceof Iterable) {
                return key + ":" + StreamSupport.stream(((Iterable<?>)value).spliterator(), false)
                    .map(String::valueOf)
                    .sorted()
                    .collect(Collectors.joining(","));
            } else {
                return key + ":" + value;
            }
        }).collect(Collectors.joining(","));
    }

    /**
     * 规范化排序字段
     * 
     * @param sort 排序字段
     * @return 规范化后的排序字段
     */
    private String normalizeSort(String sort) {
        if (sort == null || sort.isEmpty()) {
            return "default";
        }
        // 去除首尾空格，转为小写
        return sort.trim().toLowerCase();
    }
}