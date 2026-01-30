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

import top.continew.starter.core.exception.BusinessException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * 容错助手
 * <p>
 * 提供超时控制、降级策略等容错功能
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
public class FallbackHelper {

    /**
     * 执行带超时控制的操作
     *
     * @param supplier 操作函数
     * @param timeout  超时时间（毫秒）
     * @param <T>      返回类型
     * @return CompletableFuture
     */
    public static <T> CompletableFuture<T> executeWithTimeout(Supplier<T> supplier, long timeout) {
        return CompletableFuture.supplyAsync(supplier).orTimeout(timeout, TimeUnit.MILLISECONDS).exceptionally(ex -> {
            if (ex.getCause() instanceof TimeoutException) {
                throw new BusinessException("检索超时");
            }
            throw new BusinessException("检索失败: " + ex.getMessage());
        });
    }

    /**
     * 执行带降级策略的操作
     *
     * @param supplier      操作函数
     * @param fallbackValue 降级返回值
     * @param <T>           返回类型
     * @return 操作结果或降级值
     */
    public static <T> T executeWithFallback(Supplier<T> supplier, T fallbackValue) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return fallbackValue;
        }
    }

    /**
     * 执行带重试的操作
     *
     * @param supplier   操作函数
     * @param maxRetries 最大重试次数
     * @param <T>        返回类型
     * @return 操作结果
     */
    public static <T> T executeWithRetry(Supplier<T> supplier, int maxRetries) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < maxRetries) {
            try {
                return supplier.get();
            } catch (Exception e) {
                lastException = e;
                attempts++;
                if (attempts < maxRetries) {
                    try {
                        Thread.sleep(1000L * attempts); // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new BusinessException("重试被中断");
                    }
                }
            }
        }

        throw new BusinessException("操作失败，已重试 " + maxRetries + " 次", lastException);
    }
}
