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

package top.codestyle.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import top.codestyle.properties.AsyncThreadPoolProperties;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author ChonghaoGao
 * @date 2025/11/12 22:06)
 */
@Configuration
public class AsyncConfig {

    @Autowired
    private AsyncThreadPoolProperties asyncThreadPoolProperties;

    @Bean("searchExecutor")
    public ThreadPoolTaskExecutor searchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncThreadPoolProperties.getCorePoolSize());
        executor.setMaxPoolSize(asyncThreadPoolProperties.getMaxPoolSize());
        executor.setQueueCapacity(asyncThreadPoolProperties.getQueueCapacity());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("search-");
        executor.initialize();
        return executor;
    }
}
