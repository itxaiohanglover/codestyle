/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this fileInfo except in compliance with the License.
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

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import top.codestyle.admin.search.service.CacheInvalidationService;

import java.util.Set;

/**
 * 缓存失效服务实现类
 * 
 * @author chonghaoGao
 * @date 2025/12/23
 */
@Service("cacheInvalidationService")
@RequiredArgsConstructor
public class CacheInvalidationServiceImpl implements CacheInvalidationService {

    private final RedisTemplate<String, ?> redisTemplate;
    private static final String CACHE_KEY_PREFIX = "search:result:";

    /**
     * 基于关键词的缓存失效
     * 
     * @param keyword 关键词
     */
    @Override
    public void invalidateByKeyword(String keyword) {
        // 精确匹配失效
        String exactKey = CACHE_KEY_PREFIX + keyword.toLowerCase().trim();
        redisTemplate.delete(exactKey);

        // 模糊匹配失效（包含该关键词的所有缓存）
        String pattern = CACHE_KEY_PREFIX + "*" + keyword.toLowerCase().trim() + "*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 基于字段的缓存失效
     * 
     * @param field 字段名
     * @param value 字段值
     */
    @Override
    public void invalidateByField(String field, String value) {
        // 对于groupId和artifactId字段，需要特殊处理
        if ("groupId".equals(field) || "artifactId".equals(field)) {
            // 模糊匹配包含该字段值的缓存
            String pattern = CACHE_KEY_PREFIX + "*:" + field + ":" + value + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        }
        // 其他字段根据实际情况处理
    }

    /**
     * 批量失效缓存
     * 
     * @param patterns 缓存键模式列表
     */
    @Override
    public void batchInvalidateByPatterns(Set<String> patterns) {
        for (String pattern : patterns) {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        }
    }

    /**
     * 失效所有搜索缓存
     */
    @Override
    public void invalidateAll() {
        String pattern = CACHE_KEY_PREFIX + "*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 基于语义分析的智能缓存失效
     * 
     * @param content 变更内容
     */
    @Override
    public void intelligentInvalidate(String content) {
        if (content == null || content.isEmpty()) {
            return;
        }

        // 提取关键词进行失效
        String[] keywords = extractKeywords(content);
        for (String keyword : keywords) {
            invalidateByKeyword(keyword);
        }
    }

    /**
     * 从内容中提取关键词
     * 
     * @param content 内容
     * @return 关键词数组
     */
    private String[] extractKeywords(String content) {
        // 简单的关键词提取逻辑，可以根据实际需求扩展
        return content.toLowerCase().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", " ").trim().split("\\s+");
    }
}
