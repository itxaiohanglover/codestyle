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

package top.codestyle.admin.search.service;

import top.codestyle.admin.search.model.dto.DataChangeMessage;

/**
 * 监控指标服务接口
 * 记录消息处理的关键指标
 * 
 * @author chonghaoGao
 * @date 2025/12/23
 */
public interface MetricsService {

    /**
     * 记录消息处理成功
     * 
     * @param message 数据变更消息
     */
    void recordSuccess(DataChangeMessage message);

    /**
     * 记录消息处理失败
     */
    void recordFailure();

    /**
     * 记录重复消息
     */
    void recordDuplicate();

    /**
     * 获取总处理数
     */
    long getTotalProcessed();

    /**
     * 获取成功数
     */
    long getSuccessCount();

    /**
     * 获取失败数
     */
    long getFailureCount();

    /**
     * 获取重复数
     */
    long getDuplicateCount();

    /**
     * 获取成功率
     */
    double getSuccessRate();

    /**
     * 获取统计信息
     */
    String getStatistics();
}
