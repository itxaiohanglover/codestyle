package top.codestyle.repository;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.stereotype.Component;
import top.codestyle.entity.es.CodeStyleTemplate;

import java.time.Duration;


import top.codestyle.utils.SearchUtils;

/**
 * @author ChonghaoGao
 * @date 2025/11/9 19:58
 * Code Style Template Repository
 * 搜索的 原始es-sql语句 相当于数据库的DAO层
 */
@Slf4j
@Component
public class CodeStyleTemplateRepository {



    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    public Page<CodeStyleTemplate> searchByKeywordWithParams(
            String keyword,
            String minimumShouldMatch,
            Integer filenameSlop,
            Integer fileDescriptionSlop,
            Integer projectDescriptionSlop,
            Double filenameBoost,
            Double fileDescriptionBoost,
            Double projectDescriptionBoost,
            Long timeoutMs,
            Boolean trackTotalHits,
            String[] sourceIncludes,
            Pageable pageable) {

        try {
            // 构建完整的查询
            NativeQuery nativeQuery = createCompleteNativeQuery(
                    keyword, minimumShouldMatch, filenameSlop,
                    fileDescriptionSlop, projectDescriptionSlop,
                    filenameBoost, fileDescriptionBoost, projectDescriptionBoost,
                    timeoutMs, trackTotalHits, sourceIncludes, pageable
            );

            // 执行查询
            SearchHits<CodeStyleTemplate> searchHits = elasticsearchTemplate.search(
                    nativeQuery,
                    CodeStyleTemplate.class,
                    IndexCoordinates.of("template_index") // 指定索引名
            );

            // 手动转换为普通 Page 对象
            return SearchUtils.convertToPage(searchHits, pageable);

        } catch (Exception e) {
            throw new RuntimeException("搜索失败: " + e.getMessage(), e);
        }
    }



    private NativeQuery createCompleteNativeQuery(
            String keyword,
            String minimumShouldMatch,
            Integer filenameSlop,
            Integer fileDescriptionSlop,
            Integer projectDescriptionSlop,
            Double filenameBoost,
            Double fileDescriptionBoost,
            Double projectDescriptionBoost,
            Long timeoutMs,
            Boolean trackTotalHits,
            String[] sourceIncludes,
            Pageable pageable) {

        // 1. 构建查询条件
        Query query = createQueryBuilder(
                keyword, minimumShouldMatch, filenameSlop,projectDescriptionSlop,
                fileDescriptionSlop,filenameBoost, fileDescriptionBoost, projectDescriptionBoost
        );

        // 2. 构建 NativeQuery
        NativeQueryBuilder nativeQueryBuilder = NativeQuery.builder()
                .withQuery(query)
                .withPageable(pageable)
                .withSort(Sort.by(Sort.Order.desc("_score")));

        // 3. 添加超时设置
        if (timeoutMs != null) {
            nativeQueryBuilder.withTimeout(Duration.ofMillis(timeoutMs));
        }

        // 4. 添加总命中数跟踪
        if (trackTotalHits != null) {
            nativeQueryBuilder.withTrackTotalHits(trackTotalHits);
        }

        // 5. 添加源过滤
        if (sourceIncludes != null && sourceIncludes.length > 0) {
            nativeQueryBuilder.withSourceFilter(new FetchSourceFilter(sourceIncludes, null));
        }

        return nativeQueryBuilder.build();
    }

    private Query createQueryBuilder(
            String keyword,
            String minimumShouldMatch,
            Integer filenameSlop,
            Integer fileDescriptionSlop,
            Integer projectDescriptionSlop,
            Double filenameBoost,
            Double fileDescriptionBoost,
            Double projectDescriptionBoost) {

        // 创建BoolQuery构建器
        BoolQuery.Builder boolQueryBuilder = QueryBuilders.bool();

        // 主要匹配条件 - 多字段匹配
        MultiMatchQuery.Builder multiMatchBuilder = QueryBuilders.multiMatch()
                .query(keyword)
                .fields("projectDescription", "description", "fileName");

        if (minimumShouldMatch != null) {
            multiMatchBuilder.minimumShouldMatch(minimumShouldMatch);
        }

        boolQueryBuilder.must(multiMatchBuilder.build()._toQuery());

        // 短语匹配提升 - 文件名
        if (filenameBoost != null && filenameBoost > 0) {
            MatchPhraseQuery.Builder fileNamePhraseBuilder = QueryBuilders.matchPhrase()
                    .field("fileName")
                    .query(keyword);

            if (filenameSlop != null) {
                fileNamePhraseBuilder.slop(filenameSlop);
            }

            boolQueryBuilder.should(fileNamePhraseBuilder.boost(filenameBoost.floatValue()).build()._toQuery());
        }

        // 短语匹配提升 - 文件描述
        if (fileDescriptionBoost != null && fileDescriptionBoost > 0) {
            MatchPhraseQuery.Builder fileDescPhraseBuilder = QueryBuilders.matchPhrase()
                    .field("description")
                    .query(keyword);

            if (fileDescriptionSlop != null) {
                fileDescPhraseBuilder.slop(fileDescriptionSlop);
            }

            boolQueryBuilder.should(fileDescPhraseBuilder.boost(fileDescriptionBoost.floatValue()).build()._toQuery());
        }

        // 短语匹配提升 - 项目描述
        if (projectDescriptionBoost != null && projectDescriptionBoost > 0) {
            MatchPhraseQuery.Builder projectDescPhraseBuilder = QueryBuilders.matchPhrase()
                    .field("projectDescription")
                    .query(keyword)
                    .boost(projectDescriptionBoost.floatValue());

            if(projectDescriptionSlop != null) {
                projectDescPhraseBuilder.slop(projectDescriptionSlop);
            }

            boolQueryBuilder.should(projectDescPhraseBuilder.build()._toQuery());
        }

        return boolQueryBuilder.build()._toQuery();
    }


    @PostConstruct
    public void logNumOfAllDoc() {
        try {
            // 执行计数查询统计总文档数
            NativeQuery countQuery = new NativeQueryBuilder()
                    .withQuery(qb -> qb.matchAll(m -> m))  // 匹配所有文档
                    .build();

            long totalCount = elasticsearchTemplate.count(countQuery, CodeStyleTemplate.class);
            log.info("索引 {} 中的总文档条目: {}", CodeStyleTemplate.class.getSimpleName(), totalCount);

        } catch (Exception e) {
            // 这里可以记录日志，但不阻止应用启动
            log.info("ES初始化查询失败 异常原因:{}",e.getMessage());
        }
    }

}