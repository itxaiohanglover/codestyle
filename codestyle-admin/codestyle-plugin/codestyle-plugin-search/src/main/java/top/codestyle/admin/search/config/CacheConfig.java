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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.codestyle.admin.search.model.SearchResult;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CacheConfig {

    private final SearchProperties searchProperties;

    @Bean
    public Cache<String, List<SearchResult>> searchLocalCache() {
        SearchProperties.CacheProperties.LocalProperties local = searchProperties.getCache().getLocal();

        return Caffeine.newBuilder()
            .maximumSize(local.getMaxSize())
            .expireAfterWrite(local.getTtl(), TimeUnit.SECONDS)
            .recordStats()
            .build();
    }
}
