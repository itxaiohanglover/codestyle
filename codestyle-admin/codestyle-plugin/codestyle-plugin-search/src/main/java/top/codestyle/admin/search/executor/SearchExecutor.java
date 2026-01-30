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

package top.codestyle.admin.search.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;
import top.codestyle.admin.search.service.ElasticsearchSearchService;
import top.codestyle.admin.search.service.MilvusSearchService;
import top.codestyle.admin.search.spi.SearchProvider;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 检索执行器
 * <p>
 * 核心职责：
 * 1. 管理标准数据源（ES、Milvus）的检索
 * 2. 管理自定义 SPI Provider 的注册和调用
 * 3. 协调混合检索的并行执行
 *
 * @author CodeStyle Team
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchExecutor {

    private final ElasticsearchSearchService esSearchService;
    private final Optional<MilvusSearchService> milvusSearchService;

    /**
     * 通过 SPI 自动加载的自定义 Provider 列表
     */
    private final List<SearchProvider> customProviders = loadCustomProviders();

    /**
     * 加载自定义 SearchProvider
     * <p>
     * 使用 SPI 机制自动发现 META-INF/services/top.codestyle.admin.search.spi.SearchProvider
     */
    private List<SearchProvider> loadCustomProviders() {
        ServiceLoader<SearchProvider> loader = ServiceLoader.load(SearchProvider.class);
        List<SearchProvider> providers = new ArrayList<>();
        for (SearchProvider provider : loader) {
            providers.add(provider);
            log.info("加载自定义 SearchProvider: {}", provider.getName());
        }
        return providers;
    }

    /**
     * 执行单源检索
     *
     * @param type    数据源类型
     * @param request 检索请求
     * @return 检索结果
     */
    public List<SearchResult> execute(SearchSourceType type, SearchRequest request) {
        // 1. 优先使用标准 Service
        switch (type) {
            case ELASTICSEARCH:
                return esSearchService.search(request);
            case MILVUS:
                return milvusSearchService.map(service -> service.search(request)).orElse(Collections.emptyList());
            case CUSTOM:
                // 2. CUSTOM 类型使用自定义 Provider
                return executeByCustomProviders(Collections.singletonList(type), request);
            default:
                return Collections.emptyList();
        }
    }

    /**
     * 执行混合检索（ES + Milvus）
     *
     * @param request 检索请求
     * @return 检索结果
     */
    public List<SearchResult> executeHybrid(SearchRequest request) {
        // 并行执行 ES 和 Milvus 检索
        List<CompletableFuture<List<SearchResult>>> futures = new ArrayList<>();

        // ES 检索
        futures.add(CompletableFuture.supplyAsync(() -> esSearchService.search(request)));

        // Milvus 检索（如果可用）
        milvusSearchService.ifPresent(service -> futures.add(CompletableFuture.supplyAsync(() -> service
            .search(request))));

        // 等待所有检索完成
        return futures.stream().map(CompletableFuture::join).flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * 通过自定义 Provider 执行检索
     *
     * @param sourceTypes 支持的数据源类型
     * @param request     检索请求
     * @return 检索结果
     */
    public List<SearchResult> executeByCustomProviders(Collection<SearchSourceType> sourceTypes,
                                                       SearchRequest request) {
        return customProviders.stream()
            .filter(provider -> sourceTypes.stream().anyMatch(provider::supports))
            .sorted(Comparator.comparingInt(SearchProvider::getPriority))
            .findFirst()
            .map(provider -> {
                log.debug("使用自定义 Provider: {}", provider.getName());
                return provider.search(request);
            })
            .orElse(Collections.emptyList());
    }

    /**
     * 获取所有注册的自定义 Provider
     *
     * @return Provider 列表
     */
    public List<SearchProvider> getCustomProviders() {
        return Collections.unmodifiableList(customProviders);
    }
}
