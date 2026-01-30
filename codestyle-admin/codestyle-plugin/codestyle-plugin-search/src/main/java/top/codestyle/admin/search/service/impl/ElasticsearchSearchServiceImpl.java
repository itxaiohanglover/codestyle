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

package top.codestyle.admin.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import top.codestyle.admin.search.config.SearchProperties;
import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;
import top.codestyle.admin.search.service.ElasticsearchSearchService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch 检索服务实现
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(ElasticsearchClient.class)
public class ElasticsearchSearchServiceImpl implements ElasticsearchSearchService {

    private final ElasticsearchClient esClient;
    private final SearchProperties searchProperties;

    @Override
    public List<SearchResult> search(SearchRequest request) {
        try {
            String index = searchProperties.getElasticsearch().getIndex();
            log.info("Elasticsearch 检索开始，索引: {}, 查询: {}", index, request.getQuery());

            // 构建 ES 查询
            SearchResponse<Document> response = esClient.search(s -> s.index(index)
                .query(q -> q.multiMatch(m -> m.query(request.getQuery()).fields("title^3", "content^2", "tags")))
                .size(request.getTopK())
                .highlight(h -> h.fields("content", f -> f.preTags("<em>")
                    .postTags("</em>")
                    .numberOfFragments(3)
                    .fragmentSize(150))), Document.class);

            // 转换结果
            List<SearchResult> results = convertToSearchResults(response);
            log.info("Elasticsearch 检索完成，返回 {} 条结果", results.size());

            return results;

        } catch (Exception e) {
            log.error("Elasticsearch 检索失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 转换 ES 响应为检索结果
     */
    private List<SearchResult> convertToSearchResults(SearchResponse<Document> response) {
        return response.hits().hits().stream().map(this::convertHit).collect(Collectors.toList());
    }

    /**
     * 转换单个 Hit
     */
    private SearchResult convertHit(Hit<Document> hit) {
        Document doc = hit.source();

        return SearchResult.builder()
            .id(hit.id())
            .sourceType(SearchSourceType.ELASTICSEARCH)
            .title(doc != null ? doc.getTitle() : "")
            .content(doc != null ? doc.getContent() : "")
            .score(hit.score())
            .rank(0)
            .highlight(extractHighlight(hit))
            .build();
    }

    /**
     * 提取高亮片段
     */
    private String extractHighlight(Hit<Document> hit) {
        if (hit.highlight() != null && hit.highlight().containsKey("content")) {
            return String.join(" ... ", hit.highlight().get("content"));
        }
        return null;
    }

    /**
     * ES 文档模型
     */
    @Data
    public static class Document {
        private String title;
        private String content;
        private List<String> tags;
        private String groupId;
        private String metaJson;
    }
}
