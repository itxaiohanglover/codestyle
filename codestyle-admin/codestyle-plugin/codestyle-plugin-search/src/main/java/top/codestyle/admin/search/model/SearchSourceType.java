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

package top.codestyle.admin.search.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 检索数据源类型
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum SearchSourceType {

    /**
     * Elasticsearch 全文检索
     */
    ELASTICSEARCH("Elasticsearch", "全文检索"),

    /**
     * Milvus 向量检索
     */
    MILVUS("Milvus", "向量检索"),

    /**
     * 混合检索
     */
    HYBRID("Hybrid", "混合检索"),

    /**
     * 自定义数据源
     */
    CUSTOM("Custom", "自定义数据源");

    private final String code;
    private final String description;
}
