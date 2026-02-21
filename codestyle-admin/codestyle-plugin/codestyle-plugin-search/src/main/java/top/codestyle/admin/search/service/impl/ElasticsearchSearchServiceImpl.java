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
            log.debug("开始 ES 检索，查询: {}, topK: {}", request.getQuery(), request.getTopK());

            // 1. 构建查询
            co.elastic.clients.elasticsearch.core.SearchRequest esRequest = buildSearchRequest(request);
            
            // 2. 执行查询
            SearchResponse<Map> response = esClient.search(esRequest, Map.class);

            // 3. 转换结果
            List<SearchResult> results = convertResults(response);
            
            log.debug("ES 检索完成，返回 {} 条结果", results.size());
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
     * 多字段加权检索：
     * - title^3: 标题权重最高
     * - content^2: 内容权重次之
     * - tags^1: 标签权重最低
     */
    private co.elastic.clients.elasticsearch.core.SearchRequest buildSearchRequest(SearchRequest request) {
        return co.elastic.clients.elasticsearch.core.SearchRequest.of(s -> s
            .index(properties.getElasticsearch().getIndex())
            .query(q -> q
                .multiMatch(m -> m
                    .query(request.getQuery())
                    .fields("title^3", "content^2", "tags^1")  // 字段加权
                    .type(TextQueryType.BestFields)
                )
            )
            .size(request.getTopK())
            .highlight(h -> h
                .fields("content", f -> f
                    .numberOfFragments(1)
                    .fragmentSize(200)
                )
            )
        );
    }

    /**
     * 转换 ES 结果为统一格式
     */
    private List<SearchResult> convertResults(SearchResponse<Map> response) {
        return response.hits().hits().stream()
            .map(this::convertHit)
            .collect(Collectors.toList());
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
            .content(getStringValue(source, "content"))
            .snippet(getStringValue(source, "snippet"))
            .score(hit.score())
            .highlight(extractHighlight(hit))
            .metadata(source)  // 完整的 metadata，包含所有字段（包括非索引字段）
            // MCP 必要字段（从 ES 索引中提取）
            .groupId(getStringValue(source, "groupId"))
            .artifactId(getStringValue(source, "artifactId"))
            .version(getStringValue(source, "version"))
            .fileType(getStringValue(source, "fileType"))
            .build();
        // 注意：filePath, filename, sha256 等非索引字段
        // 已经包含在 metadata 中，通过 SearchResult 的 getter 方法访问
    }

    /**
     * 提取高亮片段
     */
    private String extractHighlight(Hit<Map> hit) {
        if (hit.highlight() != null && hit.highlight().containsKey("content")) {
            List<String> fragments = hit.highlight().get("content");
            if (!fragments.isEmpty()) {
                return fragments.get(0);
            }
        }
        return null;
    }

    /**
     * 安全获取字符串值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}
