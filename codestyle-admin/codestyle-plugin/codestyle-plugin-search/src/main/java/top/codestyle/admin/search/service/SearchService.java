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

package top.codestyle.admin.search.service;

import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;

import java.util.List;

/**
 * 检索服务接口
 * <p>
 * 提供单源检索、混合检索、检索并重排等功能
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
public interface SearchService {

    /**
     * 单源检索
     *
     * @param type    数据源类型
     * @param request 检索请求
     * @return 检索结果列表
     */
    List<SearchResult> search(SearchSourceType type, SearchRequest request);

    /**
     * 混合检索（ES + Milvus）
     *
     * @param request 检索请求
     * @return 检索结果列表
     */
    List<SearchResult> hybridSearch(SearchRequest request);

    /**
     * 检索并重排
     *
     * @param request 检索请求
     * @return 检索结果列表
     */
    List<SearchResult> searchWithRerank(SearchRequest request);
}

