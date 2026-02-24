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

package top.codestyle.admin.search.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import top.codestyle.admin.search.config.SearchProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BGE-Rerank HTTP 客户端
 * <p>
 * 调用 BGE-Rerank API 进行重排序
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.rerank", name = "enabled", havingValue = "true")
public class RerankClient {

    private final RestTemplate restTemplate;
    private final SearchProperties searchProperties;

    /**
     * 调用 BGE-Rerank API
     *
     * @param query    查询文本
     * @param passages 文档列表
     * @return 重排分数列表
     */
    public List<Double> rerank(String query, List<String> passages) {
        String apiUrl = searchProperties.getRerank().getApiUrl();
        String model = searchProperties.getRerank().getModel();

        log.debug("调用 BGE-Rerank API: {}", apiUrl);

        // 构建请求
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("query", query);
        requestBody.put("passages", passages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // 调用 API
        RerankResponse response = restTemplate.postForObject(apiUrl, request, RerankResponse.class);

        if (response == null || response.getScores() == null) {
            log.warn("BGE-Rerank API 返回空结果");
            return List.of();
        }

        log.debug("BGE-Rerank API 返回 {} 个分数", response.getScores().size());
        return response.getScores();
    }

    /**
     * BGE-Rerank API 响应
     */
    @Data
    public static class RerankResponse {
        private List<Double> scores;
        private String model;
        private Integer usage;
    }
}
