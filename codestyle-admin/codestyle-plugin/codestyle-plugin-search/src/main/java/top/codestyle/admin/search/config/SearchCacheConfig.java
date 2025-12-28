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

package top.codestyle.admin.search.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 搜索缓存配置类
 * 启用异步和定时任务支持（用于缓存预热）
 * 
 * @author AI Assistant
 * @date 2025/12/23
 */
@Configuration
@EnableAsync
@EnableScheduling
@ConditionalOnProperty(name = "codestyle.search.enabled", havingValue = "true", matchIfMissing = true)
public class SearchCacheConfig {
    // 配置类，启用异步和定时任务
}
