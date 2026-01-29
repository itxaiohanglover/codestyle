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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import top.codestyle.admin.search.service.HotKeyDetectionService;

import java.time.Duration;
import java.util.Set;

/**
 * 热点数据识别服务实现类
 * 使用滑动窗口算法统计访问频率，自动识别热点关键词
 *
 * @author chonghaoGao
 * @date 2025/12/23
 */
@Slf4j
@Service("hotKeyDetectionService")
@RequiredArgsConstructor
public class HotKeyDetectionServiceImpl implements HotKeyDetectionService {

    private static final String HOT_KEY_STAT_PREFIX = "search:hot:stat:";
    private static final String HOT_KEY_SET_KEY = "search:hot:keys";

    // 热点判定阈值（1小时内访问次数）
    private static final long HOT_KEY_THRESHOLD = 100L;

    // 统计时间窗口（1小时）
    private static final long STAT_WINDOW_SECONDS = 3600L;

    private final StringRedisTemplate redisTemplate;

    /**
     * 记录关键词访问
     * 
     * @param query 搜索关键词
     */
    @Override
    public void recordAccess(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }

        try {
            String normalizedQuery = normalizeQuery(query);
            String statKey = HOT_KEY_STAT_PREFIX + normalizedQuery;

            // 使用Redis INCR命令统计访问次数
            Long count = redisTemplate.opsForValue().increment(statKey);

            // 设置过期时间为统计窗口时间
            redisTemplate.expire(statKey, Duration.ofSeconds(STAT_WINDOW_SECONDS));

            // 如果达到热点阈值，加入热点集合
            if (count != null && count >= HOT_KEY_THRESHOLD) {
                markAsHotKey(normalizedQuery);
            }

            log.debug("记录关键词访问: query={}, count={}", normalizedQuery, count);

        } catch (Exception e) {
            log.error("记录关键词访问失败: query={}", query, e);
            // Redis异常不影响主流程
        }
    }

    /**
     * 判断是否为热点关键词
     * 
     * @param query 搜索关键词
     * @return true表示是热点关键词
     */
    @Override
    public boolean isHotKey(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }

        try {
            String normalizedQuery = normalizeQuery(query);

            // 方法1：检查热点集合（快速判断）
            Boolean isMember = redisTemplate.opsForSet().isMember(HOT_KEY_SET_KEY, normalizedQuery);
            if (Boolean.TRUE.equals(isMember)) {
                return true;
            }

            // 方法2：检查访问统计（精确判断）
            String statKey = HOT_KEY_STAT_PREFIX + normalizedQuery;
            String countStr = redisTemplate.opsForValue().get(statKey);
            if (countStr != null) {
                try {
                    long count = Long.parseLong(countStr);
                    if (count >= HOT_KEY_THRESHOLD) {
                        // 达到阈值，加入热点集合
                        markAsHotKey(normalizedQuery);
                        return true;
                    }
                } catch (NumberFormatException e) {
                    log.warn("解析访问统计失败: query={}, countStr={}", normalizedQuery, countStr);
                }
            }

            return false;

        } catch (Exception e) {
            log.error("判断热点关键词失败: query={}", query, e);
            return false;
        }
    }

    /**
     * 标记为热点关键词
     * 
     * @param query 搜索关键词
     */
    private void markAsHotKey(String query) {
        try {
            // 加入热点集合
            redisTemplate.opsForSet().add(HOT_KEY_SET_KEY, query);
            // 热点集合设置过期时间（24小时）
            redisTemplate.expire(HOT_KEY_SET_KEY, Duration.ofHours(24));

            log.info("标记为热点关键词: query={}", query);

        } catch (Exception e) {
            log.error("标记热点关键词失败: query={}", query, e);
        }
    }

    /**
     * 获取热点关键词列表
     * 
     * @param limit 返回数量限制
     * @return 热点关键词集合
     */
    @Override
    public Set<String> getHotKeys(int limit) {
        try {
            Set<String> hotKeys = redisTemplate.opsForSet().members(HOT_KEY_SET_KEY);
            if (hotKeys != null && hotKeys.size() > limit) {
                // 如果超过限制，返回前limit个
                return hotKeys.stream().limit(limit).collect(java.util.stream.Collectors.toSet());
            }
            return hotKeys != null ? hotKeys : java.util.Collections.emptySet();

        } catch (Exception e) {
            log.error("获取热点关键词列表失败", e);
            return java.util.Collections.emptySet();
        }
    }

    /**
     * 获取关键词访问次数
     * 
     * @param query 搜索关键词
     * @return 访问次数
     */
    @Override
    public long getAccessCount(String query) {
        if (query == null || query.trim().isEmpty()) {
            return 0L;
        }

        try {
            String normalizedQuery = normalizeQuery(query);
            String statKey = HOT_KEY_STAT_PREFIX + normalizedQuery;
            String countStr = redisTemplate.opsForValue().get(statKey);

            if (countStr != null) {
                return Long.parseLong(countStr);
            }

            return 0L;

        } catch (Exception e) {
            log.error("获取访问次数失败: query={}", query, e);
            return 0L;
        }
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
}
