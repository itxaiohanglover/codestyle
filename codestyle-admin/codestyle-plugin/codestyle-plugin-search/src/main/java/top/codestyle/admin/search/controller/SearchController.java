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

package top.codestyle.admin.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.service.SearchService;
import top.continew.starter.web.model.R;

import java.util.List;

/**
 * 检索 API
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Tag(name = "检索 API")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "单源检索", description = "从指定数据源检索")
    @PostMapping("/single")
    public R<List<SearchResult>> singleSearch(@Valid @RequestBody SearchRequest request) {
        List<SearchResult> results = searchService.search(request.getSourceType(), request);
        return R.ok(results);
    }

    @Operation(summary = "混合检索", description = "融合多个数据源的检索结果")
    @PostMapping("/hybrid")
    public R<List<SearchResult>> hybridSearch(@Valid @RequestBody SearchRequest request) {
        List<SearchResult> results = searchService.hybridSearch(request);
        return R.ok(results);
    }

    @Operation(summary = "检索并重排", description = "检索后使用 BGE-Rerank 重排序")
    @PostMapping("/rerank")
    public R<List<SearchResult>> searchWithRerank(@Valid @RequestBody SearchRequest request) {
        List<SearchResult> results = searchService.searchWithRerank(request);
        return R.ok(results);
    }

    @Operation(summary = "快速检索", description = "简化的检索接口，支持 GET 请求")
    @GetMapping("/quick")
    public R<List<SearchResult>> quickSearch(@RequestParam String query,
                                             @RequestParam(defaultValue = "10") Integer topK) {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setTopK(topK);

        List<SearchResult> results = searchService.hybridSearch(request);
        return R.ok(results);
    }
}
