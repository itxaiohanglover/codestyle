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

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.SearchResults;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.dml.SearchParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import top.codestyle.admin.search.config.SearchProperties;
import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;
import top.codestyle.admin.search.service.EmbeddingService;
import top.codestyle.admin.search.service.MilvusSearchService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Milvus 向量检索服务实现
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.milvus", name = "enabled", havingValue = "true")
@ConditionalOnBean(MilvusServiceClient.class)
public class MilvusSearchServiceImpl implements MilvusSearchService {

    private final MilvusServiceClient milvusClient;
    private final SearchProperties searchProperties;
    private final EmbeddingService embeddingService;

    @Override
    public List<SearchResult> search(SearchRequest request) {
        try {
            String collection = searchProperties.getMilvus().getCollection();
            log.info("Milvus 检索开始，集合: {}, 查询: {}", collection, request.getQuery());

            // 1. 将查询文本转换为向量
            float[] queryVector = textToVector(request.getQuery());

            // 2. 构建 Milvus 检索参数
            SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(collection)
                .withMetricType(MetricType.L2)
                .withOutFields(List.of("id", "title", "content"))
                .withTopK(request.getTopK())
                .withVectors(List.of(queryVector))
                .withVectorFieldName("embedding")
                .build();

            // 3. 执行检索
            R<SearchResults> response = milvusClient.search(searchParam);

            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("Milvus 检索失败: {}", response.getMessage());
                return Collections.emptyList();
            }

            // 4. 转换结果
            List<SearchResult> results = convertToSearchResults(response.getData());
            log.info("Milvus 检索完成，返回 {} 条结果", results.size());

            return results;

        } catch (Exception e) {
            log.error("Milvus 检索失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public float[] textToVector(String text) {
        return embeddingService.encode(text);
    }

    /**
     * 转换 Milvus 响应为检索结果
     */
    private List<SearchResult> convertToSearchResults(SearchResults searchResults) {
        List<SearchResult> results = new ArrayList<>();

        if (searchResults.getResults() == null || searchResults.getResults().getFieldsDataList().isEmpty()) {
            return results;
        }

        // 获取结果数量
        int numResults = (int)searchResults.getResults().getTopK();

        for (int i = 0; i < numResults; i++) {
            try {
                SearchResult result = SearchResult.builder()
                    .id(String.valueOf(i))
                    .sourceType(SearchSourceType.MILVUS)
                    .title("") // 从 fields 中提取
                    .content("") // 从 fields 中提取
                    .score(0.0) // 从 distances 中提取
                    .rank(i)
                    .build();

                results.add(result);
            } catch (Exception e) {
                log.warn("转换 Milvus 结果失败", e);
            }
        }

        return results;
    }
}
