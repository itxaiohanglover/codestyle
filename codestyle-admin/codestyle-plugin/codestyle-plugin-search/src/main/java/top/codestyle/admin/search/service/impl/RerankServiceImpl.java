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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import top.codestyle.admin.search.client.RerankClient;
import top.codestyle.admin.search.config.SearchProperties;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.service.RerankService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 重排服务实现
 * <p>
 * 使用 BGE-Rerank 模型对检索结果进行重排序
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.rerank", name = "enabled", havingValue = "true")
public class RerankServiceImpl implements RerankService {

    private final RerankClient rerankClient;
    private final SearchProperties searchProperties;

    @Override
    @Retryable(value = {RestClientException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public List<SearchResult> rerank(String query, List<SearchResult> results) {
        int topK = searchProperties.getRerank().getTopK();
        return rerank(query, results, topK);
    }

    @Override
    @Retryable(value = {RestClientException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public List<SearchResult> rerank(String query, List<SearchResult> results, int topK) {
        if (results == null || results.isEmpty()) {
            log.debug("检索结果为空，跳过重排");
            return results;
        }

        try {
            log.info("开始重排序，原始结果数: {}, 目标返回数: {}", results.size(), topK);

            // 1. 提取文本内容
            List<String> passages = results.stream()
                .map(r -> r.getTitle() + " " + r.getContent())
                .collect(Collectors.toList());

            // 2. 调用 BGE-Rerank API
            List<Double> scores = rerankClient.rerank(query, passages);

            // 3. 更新结果分数
            for (int i = 0; i < Math.min(results.size(), scores.size()); i++) {
                results.get(i).setScore(scores.get(i));
            }

            // 4. 按分数排序
            List<SearchResult> rerankedResults = results.stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(topK)
                .collect(Collectors.toList());

            // 5. 更新排名
            for (int i = 0; i < rerankedResults.size(); i++) {
                rerankedResults.get(i).setRank(i + 1);
            }

            log.info("重排序完成，返回 {} 条结果", rerankedResults.size());
            return rerankedResults;

        } catch (RestClientException e) {
            log.warn("重排 API 调用失败，将重试", e);
            throw e;
        } catch (Exception e) {
            log.error("重排失败，返回原始结果", e);
            return results.stream().limit(topK).collect(Collectors.toList());
        }
    }
}
