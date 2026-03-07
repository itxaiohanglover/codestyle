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

package top.codestyle.admin.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import top.codestyle.admin.search.config.SearchProperties;
import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;
import top.codestyle.admin.search.service.ElasticsearchSearchService;
import top.codestyle.admin.search.util.SearchTenantUtils;
import top.continew.starter.core.exception.BusinessException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Elasticsearch 检索服务实现
 * <p>
 * 核心职责：
 * 1. ES 客户端配置
 * 2. 多字段加权检索
 * 3. 高亮显示
 * 4. 结果转换
 *
 * @author CodeStyle Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ElasticsearchSearchServiceImpl implements ElasticsearchSearchService {

    private final ElasticsearchClient esClient;
    private final SearchProperties properties;

    @Override
    public List<SearchResult> search(SearchRequest request) {
        try {
            log.info("开始 ES 检索，查询: {}, topK: {}, requestTenantId={}", request.getQuery(), request.getTopK(), request
                .getTenantId());

            // 1. 构建查询
            co.elastic.clients.elasticsearch.core.SearchRequest esRequest = buildSearchRequest(request);
            log.info("ES 检索请求已构建: index={}, tenantFilter={}, query={}", properties.getElasticsearch()
                .getIndex(), request.getTenantId() != null ? request.getTenantId() : 0L, request.getQuery());

            // 2. 执行查询
            SearchResponse<Map> response = esClient.search(esRequest, Map.class);

            // 3. 转换结果
            List<SearchResult> results = convertResults(response);

            log.info("ES 检索完成，返回 {} 条结果", results.size());
            return results;

        } catch (IOException e) {
            log.error("ES 检索失败，查询: {}", request.getQuery(), e);
            throw new BusinessException("Elasticsearch 检索服务异常");
        } catch (Exception e) {
            log.error("ES 检索异常，查询: {}", request.getQuery(), e);
            throw new BusinessException("检索服务异常");
        }
    }

    /**
     * 构建 ES 查询请求
     * <p>
     * 模板级元数据检索：
     * - name/artifactId: 标题和模板标识
     * - description/summary: 模板描述摘要
     * - tags.text: 中文标签检索
     */
    private co.elastic.clients.elasticsearch.core.SearchRequest buildSearchRequest(SearchRequest request) {
        Long tenantId = SearchTenantUtils.resolveSearchTenantId(request);
        return co.elastic.clients.elasticsearch.core.SearchRequest.of(s -> s.index(properties.getElasticsearch()
            .getIndex())
            .query(q -> q.bool(b -> b.must(m -> m.multiMatch(mm -> mm.query(request.getQuery())
                .fields("name^4", "artifactId.text^3", "description^3", "summary^2", "tags.text^3")
                .type(TextQueryType.BestFields)
                .operator(Operator.Or))).filter(f -> f.term(t -> t.field("tenantId").value(String.valueOf(tenantId))))))
            .size(request.getTopK())
            .highlight(h -> h.fields("description", f -> f.numberOfFragments(1).fragmentSize(160))
                .fields("summary", f -> f.numberOfFragments(1).fragmentSize(160))));
    }

    /**
     * 转换 ES 结果为统一格式
     */
    private List<SearchResult> convertResults(SearchResponse<Map> response) {
        return response.hits().hits().stream().map(this::convertHit).collect(Collectors.toList());
    }

    /**
     * 转换单个 Hit
     */
    private SearchResult convertHit(Hit<Map> hit) {
        Map<String, Object> source = hit.source();

        return SearchResult.builder()
            .id(hit.id())
            .sourceType(SearchSourceType.ELASTICSEARCH)
            .title(getStringValue(source, "title"))
            .content(getStringValue(source, "description"))
            .snippet(extractSnippet(hit, source))
            .score(hit.score())
            .highlight(extractHighlight(hit))
            .metadata(source)
            .groupId(getStringValue(source, "groupId"))
            .artifactId(getStringValue(source, "artifactId"))
            .version(getStringValue(source, "version"))
            .fileType(getStringValue(source, "fileType"))
            .build();
    }

    /**
     * 提取高亮片段
     */
    private String extractHighlight(Hit<Map> hit) {
        if (hit.highlight() != null) {
            if (hit.highlight().containsKey("description") && !hit.highlight().get("description").isEmpty()) {
                return hit.highlight().get("description").get(0);
            }
            if (hit.highlight().containsKey("summary") && !hit.highlight().get("summary").isEmpty()) {
                return hit.highlight().get("summary").get(0);
            }
        }
        return null;
    }

    private String extractSnippet(Hit<Map> hit, Map<String, Object> source) {
        String highlight = extractHighlight(hit);
        if (highlight != null) {
            return highlight;
        }
        String summary = getStringValue(source, "summary");
        if (summary != null) {
            return summary;
        }
        return getStringValue(source, "description");
    }

    /**
     * 安全获取字符串值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}
