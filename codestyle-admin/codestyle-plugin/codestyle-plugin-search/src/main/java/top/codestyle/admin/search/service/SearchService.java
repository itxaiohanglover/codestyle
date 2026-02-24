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
 * 检索服务接口（简化版）
 * <p>
 * 提供混合检索功能（ES + Milvus + RRF 融合）
 *
 * @author CodeStyle Team
 * @since 2.0.0
 */
public interface SearchService {

    /**
     * 混合检索（唯一接口）
     * <p>
     * 自动执行 ES + Milvus 并行检索，使用 RRF 算法融合结果
     *
     * @param request 检索请求
     * @return 检索结果列表
     */
    List<SearchResult> search(SearchRequest request);
}
