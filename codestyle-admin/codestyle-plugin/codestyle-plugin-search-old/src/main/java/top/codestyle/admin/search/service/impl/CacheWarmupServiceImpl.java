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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import top.codestyle.admin.search.repository.RemoteSearchESRepositoryImpl;
import top.codestyle.admin.search.service.CacheWarmupService;
import top.codestyle.admin.search.service.SearchCacheService;
import top.codestyle.admin.search.vo.RemoteMetaConfigVO;

import jakarta.annotation.PostConstruct;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 缓存预热服务实现类
 * 系统启动时和定时任务预热热点数据缓存
 * 
 * @author chonghaoGao
 * @date 2025/12/23
 */
@Slf4j
@Service("cacheWarmupService")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "codestyle.search.enabled", havingValue = "true", matchIfMissing = true)
public class CacheWarmupServiceImpl implements CacheWarmupService {

    private final HotKeyDetectionServiceImpl hotKeyDetectionService;

    private final SearchCacheService searchCacheService;

    private final RemoteSearchESRepositoryImpl remoteSearchESRepositoryImpl;

    // 预热的热点关键词数量
    private static final int WARMUP_COUNT = 100;

    /**
     * 系统启动时预热热点数据
     * 异步执行，不阻塞应用启动
     */
    @PostConstruct
    @Async
    @Override
    public void warmupOnStartup() {
        log.info("开始缓存预热...");

        try {
            // 获取热点关键词列表
            Set<String> hotKeys = hotKeyDetectionService.getHotKeys(WARMUP_COUNT);

            if (hotKeys == null || hotKeys.isEmpty()) {
                log.info("暂无热点关键词，跳过缓存预热");
                return;
            }

            log.info("发现{}个热点关键词，开始预热缓存", hotKeys.size());

            // 异步预热每个热点关键词
            CompletableFuture<?>[] futures = hotKeys.stream()
                .map(this::warmupSingleKey)
                .toArray(CompletableFuture[]::new);

            // 等待所有预热任务完成
            CompletableFuture.allOf(futures).join();

            log.info("缓存预热完成，共预热{}个热点关键词", hotKeys.size());

        } catch (Exception e) {
            log.error("缓存预热失败", e);
        }
    }

    /**
     * 定时预热热点数据
     * 每30分钟执行一次，预热新发现的热点关键词
     */
    @Scheduled(fixedRate = 1800000) // 30分钟
    @Async
    @Override
    public void scheduledWarmup() {
        log.info("定时缓存预热开始...");

        try {
            // 获取热点关键词列表
            Set<String> hotKeys = hotKeyDetectionService.getHotKeys(WARMUP_COUNT);

            if (hotKeys == null || hotKeys.isEmpty()) {
                log.debug("暂无热点关键词，跳过定时预热");
                return;
            }

            int warmedCount = 0;
            for (String hotKey : hotKeys) {
                // 检查缓存是否已存在
                if (!searchCacheService.getFromCache(hotKey).isPresent()) {
                    warmupSingleKey(hotKey).join();
                    warmedCount++;
                }
            }

            if (warmedCount > 0) {
                log.info("定时缓存预热完成，新增预热{}个热点关键词", warmedCount);
            } else {
                log.debug("定时缓存预热完成，所有热点关键词已在缓存中");
            }

        } catch (Exception e) {
            log.error("定时缓存预热失败", e);
        }
    }

    /**
     * 预热单个关键词
     * 
     * @param query 搜索关键词
     * @return CompletableFuture
     */
    @Async
    private CompletableFuture<Void> warmupSingleKey(String query) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 从ES查询数据
                RemoteMetaConfigVO result = remoteSearchESRepositoryImpl.searchWithAggregations(query).orElse(null);

                if (result != null) {
                    // 写入缓存
                    searchCacheService.putToCache(query, result);
                    log.debug("预热缓存成功: query={}", query);
                } else {
                    log.debug("预热缓存跳过（无结果）: query={}", query);
                }

            } catch (Exception e) {
                log.warn("预热缓存失败: query={}", query, e);
            }
        });
    }

    /**
     * 手动触发预热
     * 可用于管理接口或监控告警
     * 
     * @param queries 要预热的关键词列表
     */
    @Async
    @Override
    public void manualWarmup(Set<String> queries) {
        if (queries == null || queries.isEmpty()) {
            return;
        }

        log.info("手动触发缓存预热，关键词数量: {}", queries.size());

        CompletableFuture<?>[] futures = queries.stream().map(this::warmupSingleKey).toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        log.info("手动缓存预热完成，关键词数量: {}", queries.size());
    }
}
