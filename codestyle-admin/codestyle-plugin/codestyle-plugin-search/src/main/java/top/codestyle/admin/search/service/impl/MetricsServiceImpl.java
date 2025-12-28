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
import org.springframework.stereotype.Service;
import top.codestyle.admin.search.model.dto.DataChangeMessage;
import top.codestyle.admin.search.service.MetricsService;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 监控指标服务实现类
 * 记录消息处理的关键指标
 * 
 * @author chonghaoGao
 * @date 2025/12/23
 */
@Slf4j
@Service("metricsService")
@RequiredArgsConstructor
public class MetricsServiceImpl implements MetricsService {

    // 消息处理计数器
    private final AtomicLong totalProcessed = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);
    private final AtomicLong duplicateCount = new AtomicLong(0);

    // 按操作类型统计
    private final AtomicLong insertCount = new AtomicLong(0);
    private final AtomicLong updateCount = new AtomicLong(0);
    private final AtomicLong deleteCount = new AtomicLong(0);

    /**
     * 记录消息处理成功
     * 
     * @param message 数据变更消息
     */
    @Override
    public void recordSuccess(DataChangeMessage message) {
        totalProcessed.incrementAndGet();
        successCount.incrementAndGet();

        // 按操作类型统计
        if (message != null && message.getOperationType() != null) {
            switch (message.getOperationType()) {
                case "INSERT":
                    insertCount.incrementAndGet();
                    break;
                case "UPDATE":
                    updateCount.incrementAndGet();
                    break;
                case "DELETE":
                    deleteCount.incrementAndGet();
                    break;
            }
        }
    }

    /**
     * 记录消息处理失败
     */
    @Override
    public void recordFailure() {
        totalProcessed.incrementAndGet();
        failureCount.incrementAndGet();
    }

    /**
     * 记录重复消息
     */
    @Override
    public void recordDuplicate() {
        duplicateCount.incrementAndGet();
    }

    /**
     * 获取总处理数
     */
    @Override
    public long getTotalProcessed() {
        return totalProcessed.get();
    }

    /**
     * 获取成功数
     */
    @Override
    public long getSuccessCount() {
        return successCount.get();
    }

    /**
     * 获取失败数
     */
    @Override
    public long getFailureCount() {
        return failureCount.get();
    }

    /**
     * 获取重复数
     */
    @Override
    public long getDuplicateCount() {
        return duplicateCount.get();
    }

    /**
     * 获取成功率
     */
    @Override
    public double getSuccessRate() {
        long total = totalProcessed.get();
        if (total == 0) {
            return 0.0;
        }
        return (double)successCount.get() / total * 100;
    }

    /**
     * 获取统计信息
     */
    @Override
    public String getStatistics() {
        return String
            .format("Total: %d, Success: %d, Failure: %d, Duplicate: %d, SuccessRate: %.2f%%, " + "Insert: %d, Update: %d, Delete: %d", totalProcessed
                .get(), successCount.get(), failureCount.get(), duplicateCount.get(), getSuccessRate(), insertCount
                    .get(), updateCount.get(), deleteCount.get());
    }
}
