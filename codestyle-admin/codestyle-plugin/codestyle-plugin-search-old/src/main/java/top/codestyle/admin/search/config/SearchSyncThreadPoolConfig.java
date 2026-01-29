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

package top.codestyle.admin.search.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import top.codestyle.admin.search.config.SearchProperties.ThreadPoolConfig;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 搜索同步线程池配置
 * <p>
 * 提供线程池工厂方法，用于创建批量同步任务的线程池
 * </p>
 *
 * @author ChonghaoGao
 * @date 2025/12/23
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(name = "org.springframework.data.elasticsearch.core.ElasticsearchOperations")
public class SearchSyncThreadPoolConfig {

    private final SearchProperties searchProperties;

    /**
     * 创建搜索同步线程池
     * <p>
     * 注意：此线程池是临时创建的，使用完后需要调用 shutdown() 关闭
     * </p>
     *
     * @return ThreadPoolExecutor 实例
     */
    public ThreadPoolExecutor createThreadPool() {
        ThreadPoolConfig config = searchProperties.getThreadPool();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(config.getCorePoolSize(), config
            .getMaximumPoolSize(), config.getKeepAliveSeconds(), TimeUnit.SECONDS, new LinkedBlockingQueue<>(config
                .getQueueCapacity()), new SearchSyncThreadFactory(config
                    .getThreadNamePrefix()), new SearchSyncRejectedExecutionHandler());

        log.debug("创建搜索同步线程池: 核心线程数={}, 最大线程数={}, 队列容量={}", config.getCorePoolSize(), config
            .getMaximumPoolSize(), config.getQueueCapacity());

        return executor;
    }

    /**
     * 获取线程池配置信息
     *
     * @return 线程池配置
     */
    public ThreadPoolConfig getThreadPoolConfig() {
        return searchProperties.getThreadPool();
    }

    /**
     * 搜索同步线程工厂
     */
    private static class SearchSyncThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        SearchSyncThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            thread.setDaemon(false);
            return thread;
        }
    }

    /**
     * 搜索同步拒绝执行处理器
     * <p>
     * 当线程池队列满时，使用调用者运行策略，由提交任务的线程执行
     * </p>
     */
    private static class SearchSyncRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.warn("线程池队列已满，任务将由调用线程执行。活跃线程: {}, 队列大小: {}, 已完成任务: {}", executor.getActiveCount(), executor.getQueue()
                .size(), executor.getCompletedTaskCount());
            // 使用调用者运行策略，由提交任务的线程执行
            if (!executor.isShutdown()) {
                r.run();
            } else {
                throw new RejectedExecutionException("线程池已关闭，无法执行任务");
            }
        }
    }
}
