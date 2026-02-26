/*
 * Copyright (c) 2022-present CodeStyle Authors. All Rights Reserved.
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

import cn.dev33.satoken.annotation.SaIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.service.SearchService;

import java.util.List;

/**
 * 检索 API
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Tag(name = "检索 API")
@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 模板检索接口（唯一接口）
     * <p>
     * 自动执行混合检索（ES + Milvus + RRF 融合）
     */
    @Operation(summary = "模板检索", description = "混合检索代码模板（ES + Milvus + RRF）")
    @PostMapping("/search/template")
    public List<SearchResult> searchTemplates(@Valid @RequestBody SearchRequest request) {
        return searchService.search(request);
    }

    @Operation(summary = "快速检索", description = "简化的检索接口，支持 GET 请求")
    @GetMapping("/search/quick")
    public List<SearchResult> quickSearch(@RequestParam String query, @RequestParam(defaultValue = "10") Integer topK) {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setTopK(topK);
        return searchService.search(request);
    }

    /**
     * Open API 检索接口
     * <p>
     * 使用 ContiNew 框架的 Open API 签名认证机制
     * <p>
     * 业务参数：
     * - query: 检索关键词
     * - topK: 返回结果数量（默认 10）
     *
     * @param query 检索关键词
     * @param topK  返回结果数量
     * @return 检索结果列表
     */
    @SaIgnore
    @Operation(summary = "Open API 检索", description = "基于 ContiNew Open API 签名认证的检索接口")
    @GetMapping("/open-api/search")
    public List<SearchResult> openApiSearch(@RequestParam String query,
                                            @RequestParam(defaultValue = "10") Integer topK) {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setTopK(topK);
        return searchService.search(request);
    }
}
