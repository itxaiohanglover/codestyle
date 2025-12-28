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

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import top.codestyle.admin.search.service.SearchCacheService;
import top.codestyle.admin.search.vo.RemoteMetaConfigVO;

import java.time.Duration;
import java.util.Optional;
import java.util.Iterator;

/**
 * 
 * Redis搜索缓存服务实现类
 * 
 * @author ChonghaoGao
 * @date 2025/12/22
 */
@Service("redisSearchCacheService")
@AllArgsConstructor
@ConditionalOnMissingBean(name = "jetCacheSearchCacheService")
public class RedisSearchCacheServiceImpl implements SearchCacheService {

    private final RedisTemplate<String, RemoteMetaConfigVO> redisTemplate;

    private final StringRedisTemplate stringRedisTemplate;

    private static final String CACHE_KEY_PREFIX = "search:result:";

    private static final long DEFAULT_TTL = 3600L; // 1 hour

    private static final long HOT_KEY_TTL = 7200L; // 2 hours for hot keys

    @Override
    public Optional<RemoteMetaConfigVO> getFromCache(String query) {
        String cacheKey = generateCacheKey(query);
        RemoteMetaConfigVO result = redisTemplate.opsForValue().get(cacheKey);
        // 更新热点统计
        if (result != null) {
            // 简易热点统计算法
            updateHotKeyStat(query);
        }
        return Optional.ofNullable(result);
    }

    @Override
    public void putToCache(String query, RemoteMetaConfigVO result) {
        String cacheKey = generateCacheKey(query);
        long ttl = isHotKey(query) ? HOT_KEY_TTL : DEFAULT_TTL;
        redisTemplate.opsForValue().set(cacheKey, result, Duration.ofSeconds(ttl));
    }

    @Override
    public void removeFromCache(String query) {
        String cacheKey = generateCacheKey(query);
        redisTemplate.delete(cacheKey);
    }

    @Override
    public void batchRemoveFromCache(Iterator<String> queries) {
        while (queries.hasNext()) {
            String query = queries.next();
            removeFromCache(query);
        }
    }

    /**
     * 生成缓存键
     * 
     * @param query 搜索关键词
     * @return 缓存键
     */
    private String generateCacheKey(String query) {
        return CACHE_KEY_PREFIX + query.toLowerCase().trim();
    }

    /**
     * 更新热点统计 简单热点统计
     * 
     * @param query 搜索关键词
     */
    private void updateHotKeyStat(String query) {
        String statKey = "search:hot:stat:" + query.toLowerCase().trim();
        // 使用StringRedisTemplate处理数字统计
        stringRedisTemplate.opsForValue().increment(statKey);
        // 设置统计过期时间为24小时
        stringRedisTemplate.expire(statKey, Duration.ofHours(24));
    }

    /**
     * 判断是否为热点关键词
     * 
     * @param query 搜索关键词
     * @return 是否为热点关键词
     */
    private boolean isHotKey(String query) {
        String statKey = "search:hot:stat:" + query.toLowerCase().trim();
        // 使用StringRedisTemplate的increment方法直接获取并更新计数，避免类型转换问题
        try {
            // 先检查键是否存在
            if (stringRedisTemplate.hasKey(statKey)) {
                // 如果键存在，获取当前值
                String value = stringRedisTemplate.opsForValue().get(statKey);
                if (value != null) {
                    try {
                        long count = Long.parseLong(value);
                        return count > 100; // 超过100次查询即为热点
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            // 出错时返回false
        }
        return false;
    }
}