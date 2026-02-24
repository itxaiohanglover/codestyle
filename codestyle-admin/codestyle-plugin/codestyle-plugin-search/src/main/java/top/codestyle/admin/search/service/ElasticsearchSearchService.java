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

package top.codestyle.admin.search.service;

import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;

import java.util.List;

/**
 * Elasticsearch 检索服务接口
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
public interface ElasticsearchSearchService {

    /**
     * 执行 Elasticsearch 检索
     *
     * @param request 检索请求
     * @return 检索结果列表
     */
    List<SearchResult> search(SearchRequest request);
}
