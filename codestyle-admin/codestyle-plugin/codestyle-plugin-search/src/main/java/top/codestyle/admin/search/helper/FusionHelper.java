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

package top.codestyle.admin.search.helper;

import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 融合算法助手
 * <p>
 * 提供多源检索结果的融合算法
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
public class FusionHelper {

    private static final int RRF_K = 60;

    /**
     * RRF (Reciprocal Rank Fusion) 融合算法
     * <p>
     * 公式: score = 1 / (k + rank)，k = 60
     *
     * @param results 多源检索结果
     * @return 融合后的结果
     */
    public static List<SearchResult> reciprocalRankFusion(List<SearchResult> results) {
        Map<String, Double> scoreMap = new HashMap<>();
        Map<String, SearchResult> resultMap = new HashMap<>();

        // 按数据源分组
        Map<SearchSourceType, List<SearchResult>> grouped = results.stream()
            .collect(Collectors.groupingBy(SearchResult::getSourceType));

        // 计算 RRF 分数
        grouped.forEach((sourceType, sourceResults) -> {
            for (int i = 0; i < sourceResults.size(); i++) {
                SearchResult result = sourceResults.get(i);
                String id = result.getId();

                // RRF 公式: 1 / (k + rank)
                double rrf = 1.0 / (RRF_K + i + 1);

                scoreMap.merge(id, rrf, Double::sum);
                resultMap.putIfAbsent(id, result);
            }
        });

        // 按融合分数排序
        return scoreMap.entrySet()
            .stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .map(entry -> {
                SearchResult result = resultMap.get(entry.getKey());
                result.setScore(entry.getValue());
                return result;
            })
            .collect(Collectors.toList());
    }

    /**
     * 加权融合算法
     *
     * @param results 多源检索结果
     * @param weights 权重配置
     * @return 融合后的结果
     */
    public static List<SearchResult> weightedFusion(List<SearchResult> results, Map<SearchSourceType, Double> weights) {
        Map<String, Double> scoreMap = new HashMap<>();
        Map<String, SearchResult> resultMap = new HashMap<>();

        results.forEach(result -> {
            String id = result.getId();
            Double weight = weights.getOrDefault(result.getSourceType(), 1.0);
            Double score = result.getScore() * weight;

            scoreMap.merge(id, score, Double::sum);
            resultMap.putIfAbsent(id, result);
        });

        // 按加权分数排序
        return scoreMap.entrySet()
            .stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .map(entry -> {
                SearchResult result = resultMap.get(entry.getKey());
                result.setScore(entry.getValue());
                return result;
            })
            .collect(Collectors.toList());
    }

    /**
     * 加权融合算法
     * <p>
     * 根据配置的权重融合向量和关键词检索结果
     *
     * @param vectorResults  向量检索结果
     * @param keywordResults 关键词检索结果
     * @param vectorWeight   向量检索权重（0-1）
     * @param keywordWeight  关键词检索权重（0-1）
     * @return 融合后的结果
     */
    public static List<SearchResult> weightedFusion(List<SearchResult> vectorResults,
                                                    List<SearchResult> keywordResults,
                                                    double vectorWeight,
                                                    double keywordWeight) {
        Map<String, SearchResult> resultMap = new HashMap<>();

        // 处理向量检索结果
        for (SearchResult result : vectorResults) {
            String id = result.getId();
            double weightedScore = result.getScore() * vectorWeight;

            SearchResult merged = resultMap.get(id);
            if (merged == null) {
                merged = SearchResult.builder()
                    .id(id)
                    .title(result.getTitle())
                    .content(result.getContent())
                    .snippet(result.getSnippet())
                    .metadata(result.getMetadata())
                    .build();
                resultMap.put(id, merged);
            }
            merged.setScore(merged.getScore() + weightedScore);
        }

        // 处理关键词检索结果
        for (SearchResult result : keywordResults) {
            String id = result.getId();
            double weightedScore = result.getScore() * keywordWeight;

            SearchResult merged = resultMap.get(id);
            if (merged == null) {
                merged = SearchResult.builder()
                    .id(id)
                    .title(result.getTitle())
                    .content(result.getContent())
                    .snippet(result.getSnippet())
                    .highlight(result.getHighlight())
                    .metadata(result.getMetadata())
                    .build();
                resultMap.put(id, merged);
            } else {
                merged.setScore(merged.getScore() + weightedScore);
                // 优先使用关键词检索的高亮
                if (result.getHighlight() != null) {
                    merged.setHighlight(result.getHighlight());
                }
            }
        }

        // 按融合分数排序
        return resultMap.values()
            .stream()
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .collect(Collectors.toList());
    }
}
