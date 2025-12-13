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

package top.codestyle.repository;

import co.elastic.clients.elasticsearch._types.ScriptSortType;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.json.JsonData;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.client.elc.*;
import org.springframework.data.elasticsearch.core.AggregationsContainer;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;
import top.codestyle.pojo.dto.HomePageSearchResultDTO;
import top.codestyle.pojo.dto.TimeRangeParamDTO;
import top.codestyle.pojo.entity.CodeStyleTemplateDO;
import top.codestyle.pojo.enums.TemplateSortField;
import top.codestyle.pojo.enums.TemplateDateRangeFilter;
import top.codestyle.properties.ElasticsearchSearchProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class CodeStyleTemplateRepository {

    private final ElasticsearchTemplate elasticsearchTemplate;

    private final ElasticsearchSearchProperties properties;

    /**
     * 搜索方法，返回分页 DTO
     */
    public HomePageSearchResultDTO searchByKeywordWithParams(String keyword,
                                                             Pageable pageable,
                                                             TemplateSortField popularitySortField,
                                                             SortOrder order,
                                                             TimeRangeParamDTO timeRangeParamDTO) {
        try {
            NativeQuery nativeQuery = createCompleteNativeQuery(keyword, pageable, popularitySortField, order, timeRangeParamDTO);

            SearchHits<CodeStyleTemplateDO> hits = elasticsearchTemplate
                .search(nativeQuery, CodeStyleTemplateDO.class, IndexCoordinates.of("template_index"));
            // 后端这里是0-based分页
            long total = hits.getTotalHits();
            int pageSize = pageable.getPageSize();
            int pageNumber = pageable.getPageNumber();

            long totalPages = (total + pageSize - 1) / pageSize;
            log.info("打印一下总数：{}", hits.getTotalHits());
            if (total == 0) {
                // 返回空结果而非抛异常
                return new HomePageSearchResultDTO(0, List.of(), List.of());
            }

            // 修正 pageNumber 超过最后一页的判断
            if (pageNumber < 0 || pageNumber >= totalPages || pageSize <= 0) {
                throw new RuntimeException("Repository层遭遇不合理的分页设置，当前页: " + pageNumber + " 期望最大页: " + (totalPages - 1) + " ;pageSize数目: " + pageSize + " 当前关键词最大数目: " + total);
            }

            log.info("查询到的原始数据：{} 条目", hits.getSearchHits().size());
            // 2. 获取聚合结果
            log.info("search {} 关键词查到了：{}条", keyword, hits.getTotalHits());
            AggregationsContainer<?> container = hits.getAggregations();
            List<String> topTags = new ArrayList<>();

            if (container instanceof ElasticsearchAggregations elasticAggs) {

                ElasticsearchAggregation tagsAgg = elasticAggs.get("tagsAgg");
                if (tagsAgg != null) {
                    tagsAgg.aggregation();
                    if (tagsAgg.aggregation().getAggregate().sterms() != null) {
                        tagsAgg.aggregation()
                            .getAggregate()
                            .sterms()
                            .buckets()
                            .array()
                            .forEach(bucket -> topTags.add(bucket.key().stringValue()));
                    }
                }
            }

            return new HomePageSearchResultDTO(hits.getTotalHits(), hits.getSearchHits(), topTags);
        } catch (Exception e) {
            throw new RuntimeException("搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建 NativeQuery，含脚本排序 + 动态字段排序 + 小标签聚合
     */
    private NativeQuery createCompleteNativeQuery(String keyword,
                                                  Pageable pageable,
                                                  TemplateSortField sortField, // 新增：排序字段
                                                  SortOrder sortOrder,         // 新增：排序方式
                                                  TimeRangeParamDTO timeRangeParamDTO) {

        Query baseQuery = buildBoolQuery(keyword, timeRangeParamDTO);

        // 权重参数
        double hotScoreWeight = properties.getHotScoreWeight() != null ? properties.getHotScoreWeight() : 1.0;
        double likeWeight = properties.getUserActionSort().getTotalLikeCountWeight() != null
            ? properties.getUserActionSort().getTotalLikeCountWeight()
            : 1.0;
        double favoriteWeight = properties.getUserActionSort().getTotalFavoriteCountWeight() != null
            ? properties.getUserActionSort().getTotalFavoriteCountWeight()
            : 1.0;

        // 构建基本 QueryBuilder
        NativeQueryBuilder builder = NativeQuery.builder().withQuery(baseQuery).withPageable(pageable);

        handleSortField(sortField, sortOrder, builder, hotScoreWeight, likeWeight, favoriteWeight);

        //  动态 tags 聚合查询的构建（TopK）
        handleAggField(builder);

        // 配置：超时 + trackHits
        if (properties.getTimeoutMs() != null) {
            builder.withTimeout(Duration.ofMillis(properties.getTimeoutMs()));
        }

        if (properties.getTrackTotalHits() != null) {
            builder.withTrackTotalHits(properties.getTrackTotalHits());
        }

        return builder.build();
    }

    private void handleAggField(NativeQueryBuilder builder) {
        int topK = properties.getQueryConfig().getAggTopKth();
        builder.withAggregation("tagsAgg", Aggregation.of(a -> a.terms(t -> t.field("searchTags.keyword").size(topK))));
    }

    /**
     * 动态排序逻辑处理
     */
    private void handleSortField(TemplateSortField sortField,
                                 SortOrder sortOrder,
                                 NativeQueryBuilder builder,
                                 double hotScoreWeight,
                                 double likeWeight,
                                 double favoriteWeight) {

        if (sortField == null || sortField == TemplateSortField.HOT_SCORE) {

            // 默认脚本排序
            builder.withSort(s -> s.script(scr -> scr.type(ScriptSortType.Number)
                .script(scriptBuilder -> scriptBuilder.inline(inline -> inline
                    .source("doc['hotScoreWeight'].value * " + hotScoreWeight + " + doc['totalLikeCount'].value * " + likeWeight + " + doc['totalFavoriteCount'].value * " + favoriteWeight)))
                .order(sortOrder != null ? sortOrder : SortOrder.Desc)));

        } else {

            // 字段升降序排列（like / favorite / createTime / updateTime / editTime ）
            String fieldName;

            switch (sortField) {
                case LIKE_COUNT -> fieldName = "totalLikeCount";
                case FAVORITE_COUNT -> fieldName = "totalFavoriteCount";
                case CREATE_TIME -> fieldName = "createTime";
                case UPDATE_TIME -> fieldName = "updateTime";
                case EDIT_TIME -> fieldName = "editTime";
                default -> fieldName = "createTime";
            }

            builder.withSort(s -> s.field(f -> f.field(fieldName)
                .order(sortOrder != null ? sortOrder : SortOrder.Desc)));
        }
    }

    /**
     * 构建 BoolQuery，包括多字段匹配、短语提升、私有空间过滤
     */
    private Query buildBoolQuery(String keyword, TimeRangeParamDTO timeParam) {

        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        // 私有空间过滤
        boolBuilder.filter(TermQuery.of(t -> t.field("isPrivate").value(0))._toQuery());

        // 时间字段的过滤
        Query timeRangeQuery = buildTimeRangeQuery(timeParam);
        if (timeRangeQuery != null) {
            boolBuilder.filter(timeRangeQuery);
        }

        // 多字段匹配
        MultiMatchQuery.Builder multiBuilder = new MultiMatchQuery.Builder().query(keyword)
            .fields("nameCh", "nameEn", "description", "searchTags")
            .type(TextQueryType.CrossFields);

        if (properties.getMinimumShouldMatch() != null) {
            multiBuilder.minimumShouldMatch(properties.getMinimumShouldMatch());
        }

        boolBuilder.must(multiBuilder.build()._toQuery());

        // 短语提升
        applyPhraseBoost(boolBuilder, "nameCh", properties.getPhraseSlops().getNameChSlop(), properties
            .getBoostFactors()
            .getNameChBoost(), keyword);
        applyPhraseBoost(boolBuilder, "nameEn", properties.getPhraseSlops().getNameEnSlop(), properties
            .getBoostFactors()
            .getNameEnBoost(), keyword);
        applyPhraseBoost(boolBuilder, "description", properties.getPhraseSlops().getDescriptionSlop(), properties
            .getBoostFactors()
            .getDescriptionBoost(), keyword);
        applyPhraseBoost(boolBuilder, "searchTags", properties.getPhraseSlops().getSearchTagsSlop(), properties
            .getBoostFactors()
            .getSearchTagsBoost(), keyword);

        return boolBuilder.build()._toQuery();
    }

    private Query buildTimeRangeQuery(TimeRangeParamDTO param) {

        if (param == null || param.getRangeType() == TemplateDateRangeFilter.NONE) {
            return null;
        }

        long now = System.currentTimeMillis();
        long start;
        long end = now;

        switch (param.getRangeType()) {

            case LAST_1_DAY -> start = now - 24 * 3600 * 1000L;

            case LAST_7_DAYS -> start = now - 7L * 24 * 3600 * 1000L;

            case LAST_30_DAYS -> start = now - 30L * 24 * 3600 * 1000L;

            case LAST_180_DAYS -> start = now - 180L * 24 * 3600 * 1000L;

            case CUSTOM -> {
                if (param.getStartTime() == null || param.getEndTime() == null || param.getEndTime() < param
                    .getStartTime() || param.getStartTime() < 0)
                    return null; // 不合法就不加过滤
                start = param.getStartTime();
                end = param.getEndTime();
            }

            default -> {
                return null;
            }
        }
        long finalStart = start;
        long finalEnd = end;

        String field;

        switch (param.getTimeFilterField()) {
            case UPDATE_TIME -> field = "updateTime";
            case EDIT_TIME -> field = "editTime";
            case CREATE_TIME -> field = "createTime"; // 默认
            default -> field = "createTime";
        }

        //  构建 RangeQuery（动态字段）
        return RangeQuery.of(r -> r.field(field).gte(JsonData.of(finalStart)).lte(JsonData.of(finalEnd)))._toQuery();
    }

    /*
    * 增强单个字段得分的逻辑
    * */
    private void applyPhraseBoost(BoolQuery.Builder boolBuilder,
                                  String field,
                                  Integer slop,
                                  Double boost,
                                  String keyword) {
        if (boost != null && boost > 0) {
            boolBuilder.should(MatchPhraseQuery.of(mp -> {
                mp.field(field).query(keyword).slop(slop != null ? slop : 0).boost(boost.floatValue());
                return mp;
            })._toQuery());
        }
    }

    @PostConstruct
    public void logNumOfAllDoc() {
        try {
            NativeQuery countQuery = NativeQuery.builder().withQuery(qb -> qb.matchAll(m -> m)).build();
            long totalCount = elasticsearchTemplate.count(countQuery, CodeStyleTemplateDO.class);
            log.info("索引 {} 中的总文档条目: {}", CodeStyleTemplateDO.class.getSimpleName(), totalCount);
        } catch (Exception e) {
            log.info("ES初始化查询失败 异常原因: {}", e.getMessage());
        }
    }
}
