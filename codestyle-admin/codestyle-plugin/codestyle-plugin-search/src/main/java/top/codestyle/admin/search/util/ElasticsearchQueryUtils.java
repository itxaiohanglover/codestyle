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

package top.codestyle.admin.search.util;

import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;

/**
 * Elasticsearch 查询构建工具类
 * <p>
 * 用于封装 Elasticsearch Java Client 的查询构建逻辑，隐藏实现细节
 * </p>
 *
 * @author ChonghaoGao
 * @date 2025/12/23
 */
public final class ElasticsearchQueryUtils {

    private ElasticsearchQueryUtils() {
        // 工具类不允许实例化
    }

    /**
     * 构建 Elasticsearch Java Client 的 Query 对象
     * <p>
     * 使用完全限定名隐藏类型细节，避免与 Spring Data Elasticsearch 的 Query 接口冲突
     * </p>
     *
     * @param multiMatchQuery MultiMatchQuery 对象
     * @return Elasticsearch Query 对象
     */
    public static Query buildElasticsearchQuery(MultiMatchQuery multiMatchQuery) {
        return Query.of(q -> q.multiMatch(multiMatchQuery));
    }

    /**
     * 将字符串类型的 multi_match 查询类型转换为枚举类型
     *
     * @param type 查询类型字符串（best_fields, most_fields, phrase, phrase_prefix, cross_fields）
     * @return TextQueryType 枚举值
     */
    public static TextQueryType getMultiMatchQueryType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return TextQueryType.CrossFields;
        }
        return switch (type.toLowerCase().trim()) {
            case "best_fields" -> TextQueryType.BestFields;
            case "most_fields" -> TextQueryType.MostFields;
            case "phrase" -> TextQueryType.Phrase;
            case "phrase_prefix" -> TextQueryType.PhrasePrefix;
            default -> TextQueryType.CrossFields;
        };
    }
}
