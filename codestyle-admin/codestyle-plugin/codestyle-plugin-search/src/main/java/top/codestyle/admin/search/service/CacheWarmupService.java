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

import java.util.Set;

/**
 * 缓存预热服务接口
 * 系统启动时和定时任务预热热点数据缓存
 * 
 * @author AI Assistant
 * @date 2025/12/23
 */
public interface CacheWarmupService {

    /**
     * 系统启动时预热热点数据
     * 异步执行，不阻塞应用启动
     */
    void warmupOnStartup();

    /**
     * 定时预热热点数据
     * 每30分钟执行一次，预热新发现的热点关键词
     */
    void scheduledWarmup();

    /**
     * 手动触发预热
     * 可用于管理接口或监控告警
     * 
     * @param queries 要预热的关键词列表
     */
    void manualWarmup(Set<String> queries);
}
