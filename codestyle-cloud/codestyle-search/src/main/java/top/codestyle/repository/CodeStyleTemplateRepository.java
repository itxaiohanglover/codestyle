package top.codestyle.repository;

import co.elastic.clients.elasticsearch._types.ScriptSortType;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;
import top.codestyle.entity.es.pojo.CodeStyleTemplateDO;
import top.codestyle.entity.es.vo.HomePageSearchResultVO;
import top.codestyle.properties.ElasticsearchSearchProperties;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class CodeStyleTemplateRepository {

    private final ElasticsearchTemplate elasticsearchTemplate;

    private final ElasticsearchSearchProperties properties;

    /**
     * 搜索方法，返回分页 VO
     */
    public Page<HomePageSearchResultVO> searchByKeywordWithParams(String keyword, Pageable pageable) {
        try {
            NativeQuery nativeQuery = createCompleteNativeQuery(keyword, pageable);

            SearchHits<CodeStyleTemplateDO> hits = elasticsearchTemplate.search(
                    nativeQuery,
                    CodeStyleTemplateDO.class,
                    IndexCoordinates.of("template_index")
            );

            List<HomePageSearchResultVO> voList = hits.getSearchHits()
                    .stream()
                    .map(hit -> convertToVO(hit.getContent()))
                    .collect(Collectors.toList());

            return new PageImpl<>(voList, pageable, hits.getTotalHits());
        } catch (Exception e) {
            throw new RuntimeException("搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建 NativeQuery，含脚本排序
     */
    private NativeQuery createCompleteNativeQuery(String keyword, Pageable pageable) {

        Query baseQuery = buildBoolQuery(keyword);

        // 权重参数
        double hotScoreWeight = properties.getHotScoreWeight() != null
                ? properties.getHotScoreWeight() : 1.0;
        double likeWeight = properties.getUserActionSort().getTotalLikeCountWeight() != null
                ? properties.getUserActionSort().getTotalLikeCountWeight() : 1.0;
        double favoriteWeight = properties.getUserActionSort().getTotalFavoriteCountWeight() != null
                ? properties.getUserActionSort().getTotalFavoriteCountWeight() : 1.0;

        NativeQueryBuilder builder = NativeQuery.builder()
                .withQuery(baseQuery)
                .withPageable(pageable)
                .withSort(s -> s
                        .script(scr -> scr
                                .type(ScriptSortType.Number)
                                .script(scriptBuilder -> scriptBuilder
                                        .inline(inline -> inline
                                                .source(
                                                        "doc['hotScoreWeight'].value * " + hotScoreWeight +
                                                                " + doc['totalLikeCount'].value * " + likeWeight +
                                                                " + doc['totalFavoriteCount'].value * " + favoriteWeight
                                                )
                                        )
                                )
                                .order(SortOrder.Desc)
                        )
                );

        if (properties.getTimeoutMs() != null) {
            builder.withTimeout(Duration.ofMillis(properties.getTimeoutMs()));
        }

        if (properties.getTrackTotalHits() != null) {
            builder.withTrackTotalHits(properties.getTrackTotalHits());
        }

        return builder.build();
    }

    /**
     * 构建 BoolQuery，包括多字段匹配、短语提升、私有空间过滤
     */
    private Query buildBoolQuery(String keyword) {

        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        // 私有空间过滤
        boolBuilder.filter(TermQuery.of(t -> t.field("isPrivate").value(1))._toQuery());

        // 多字段匹配
        MultiMatchQuery.Builder multiBuilder = new MultiMatchQuery.Builder()
                .query(keyword)
                .fields("nameCh", "nameEn", "description", "searchTags");

        if (properties.getMinimumShouldMatch() != null) {
            multiBuilder.minimumShouldMatch(properties.getMinimumShouldMatch());
        }

        boolBuilder.must(multiBuilder.build()._toQuery());

        // 短语提升
        applyPhraseBoost(boolBuilder, "nameCh",
                properties.getPhraseSlops().getNameChSlop(),
                properties.getBoostFactors().getNameChBoost(),
                keyword);
        applyPhraseBoost(boolBuilder, "nameEn",
                properties.getPhraseSlops().getNameEnSlop(),
                properties.getBoostFactors().getNameEnBoost(),
                keyword);
        applyPhraseBoost(boolBuilder, "description",
                properties.getPhraseSlops().getDescriptionSlop(),
                properties.getBoostFactors().getDescriptionBoost(),
                keyword);
        applyPhraseBoost(boolBuilder, "searchTags",
                properties.getPhraseSlops().getSearchTagsSlop(),
                properties.getBoostFactors().getSearchTagsBoost(),
                keyword);

        return boolBuilder.build()._toQuery();
    }

    private void applyPhraseBoost(BoolQuery.Builder boolBuilder, String field, Integer slop, Double boost, String keyword) {
        if (boost != null && boost > 0) {
            boolBuilder.should(MatchPhraseQuery.of(mp -> {
                mp.field(field)
                        .query(keyword)
                        .slop(slop != null ? slop : 0)
                        .boost(boost.floatValue());
                return mp;
            })._toQuery());
        }
    }

    private HomePageSearchResultVO convertToVO(CodeStyleTemplateDO doObj) {
        HomePageSearchResultVO vo = new HomePageSearchResultVO();
        // TODO: 映射字段
        vo.setId(doObj.getId());
        vo.setNameCh(doObj.getNameCh());
        vo.setNameEn(doObj.getNameEn());
        vo.setMemberName(doObj.getMemberNames());
        vo.setMemberAvatarUrls(doObj.getMemberAvatarUrls());
        vo.setCreatorName(doObj.getCreatorName());
        vo.setCreateTime(doObj.getCreateTime());
        vo.setUpdateTime(doObj.getUpdateTime());
        vo.setEditTime(doObj.getEditTime());
        vo.setDescription(doObj.getDescription());
        vo.setTotalLikeCount(doObj.getTotalLikeCount());
        vo.setTotalFavoriteCount(doObj.getTotalFavoriteCount());
        vo.setTags(doObj.getSearchTags());
        vo.setTemplateAvatar(doObj.getAvatar());
        vo.setVersion(doObj.getVersion());
        return vo;
    }

    @PostConstruct
    public void logNumOfAllDoc() {
        try {
            NativeQuery countQuery = NativeQuery.builder()
                    .withQuery(qb -> qb.matchAll(m -> m))
                    .build();
            long totalCount = elasticsearchTemplate.count(countQuery, CodeStyleTemplateDO.class);
            log.info("索引 {} 中的总文档条目: {}", CodeStyleTemplateDO.class.getSimpleName(), totalCount);
        } catch (Exception e) {
            log.info("ES初始化查询失败 异常原因: {}", e.getMessage());
        }
    }
}
