/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this fileInfo except in compliance with the License.
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

package top.codestyle.admin.search.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import org.springframework.data.elasticsearch.core.AggregationsContainer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import top.codestyle.admin.search.config.SearchProperties;
import top.codestyle.admin.search.config.SearchSyncThreadPoolConfig;
import top.codestyle.admin.search.entity.SearchEntity;
import top.codestyle.admin.search.util.ElasticsearchQueryUtils;
import top.codestyle.admin.search.vo.RemoteMetaConfigVO;

/**
 * Elasticsearch搜索仓库实现类
 * 使用ElasticsearchOperations实现复杂查询，包括multi_match和聚合查询
 * 
 * @author ChonghaoGao
 * @date 2025/12/22
 */
@Slf4j
@Component
@ConditionalOnClass(name = "org.springframework.data.elasticsearch.core.ElasticsearchOperations")
public class RemoteSearchESRepositoryImpl {

    private final RemoteSearchESRepository remoteSearchESRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final SearchProperties searchProperties;
    private final SearchSyncThreadPoolConfig threadPoolConfig;

    public RemoteSearchESRepositoryImpl(@Lazy RemoteSearchESRepository remoteSearchESRepository,
                                        ElasticsearchOperations elasticsearchOperations,
                                        SearchProperties searchProperties,
                                        SearchSyncThreadPoolConfig threadPoolConfig) {
        this.remoteSearchESRepository = remoteSearchESRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.searchProperties = searchProperties;
        this.threadPoolConfig = threadPoolConfig;
    }

    /**
     * 执行带聚合的搜索查询
     * 根据索引信息.md的要求实现：
     * - multi_match查询，字段权重：groupId^3, artifactId^2, description（可配置）
     * - 聚合查询：获取全局groupId和artifactId分组（可配置）
     * - 只返回1个最匹配的结果（可配置）
     * 
     * @param query 搜索关键词
     * @return 搜索结果VO，包含聚合信息
     */
    public Optional<RemoteMetaConfigVO> searchWithAggregations(String query) {
        try {
            // 从配置中获取参数
            SearchProperties.FieldWeights fieldWeights = searchProperties.getQuery().getFieldWeights();
            float groupIdWeight = fieldWeights.getGroupId();
            float artifactIdWeight = fieldWeights.getArtifactId();
            float descriptionWeight = fieldWeights.getDescription();
            String multiMatchType = searchProperties.getQuery().getMultiMatchType();
            int maxResults = searchProperties.getQuery().getMaxResults();

            // 获取聚合配置
            String groupIdsField = searchProperties.getAggregation().getGroupIds().getField();
            if (groupIdsField == null || groupIdsField.isEmpty()) {
                groupIdsField = "groupId.keyword";
            }
            int groupIdsSize = searchProperties.getAggregation().getGroupIds().getSize();

            String artifactIdsField = searchProperties.getAggregation().getArtifactIds().getField();
            if (artifactIdsField == null || artifactIdsField.isEmpty()) {
                artifactIdsField = "artifactId.keyword";
            }
            int artifactIdsSize = searchProperties.getAggregation().getArtifactIds().getSize();

            // 使用 NativeQuery Builder 构建查询
            NativeQuery searchQuery = buildNativeQuery(query, groupIdWeight, artifactIdWeight, descriptionWeight, multiMatchType, maxResults, groupIdsField, groupIdsSize, artifactIdsField, artifactIdsSize);

            // 执行查询
            SearchHits<SearchEntity> searchHits = elasticsearchOperations.search(searchQuery, SearchEntity.class);

            if (searchHits.isEmpty()) {
                log.debug("未找到匹配关键词: {}", query);
                return Optional.empty();
            }

            // 获取第一个结果
            SearchHit<SearchEntity> firstHit = searchHits.getSearchHit(0);
            SearchEntity firstEntity = firstHit.getContent();

            // 构建VO对象
            RemoteMetaConfigVO vo = new RemoteMetaConfigVO();
            vo.setGroupId(firstEntity.getGroupId());
            vo.setArtifactId(firstEntity.getArtifactId());
            vo.setDescription(firstEntity.getDescription());
            vo.setConfig(convertToConfigVO(firstEntity.getConfig()));

            // 处理聚合结果
            try {
                AggregationsContainer<?> aggregations = searchHits.getAggregations();
                if (aggregations != null) {
                    // 处理groupId聚合
                    Map<String, Long> groupIdsMap = extractStringTermsAggregation(aggregations, "agg_groupIds");
                    if (groupIdsMap != null && !groupIdsMap.isEmpty()) {
                        vo.setAggGroupIds(groupIdsMap);
                    }

                    // 处理artifactId聚合
                    Map<String, Long> artifactIdsMap = extractStringTermsAggregation(aggregations, "agg_artifactIds");
                    if (artifactIdsMap != null && !artifactIdsMap.isEmpty()) {
                        vo.setAggArtifactIds(artifactIdsMap);
                    }
                }
            } catch (Exception e) {
                log.warn("解析聚合结果失败，继续返回搜索结果: {}", e.getMessage(), e);
                // 聚合解析失败不影响主搜索结果的返回
            }

            return Optional.of(vo);
        } catch (Exception e) {
            log.error("执行ES搜索查询失败: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 使用 NativeQuery Builder 构建查询
     * 
     * @param query             搜索关键词
     * @param groupIdWeight     groupId字段权重
     * @param artifactIdWeight  artifactId字段权重
     * @param descriptionWeight description字段权重
     * @param multiMatchType    multi_match查询类型
     * @param maxResults        最大返回结果数
     * @param groupIdsField     groupId聚合字段
     * @param groupIdsSize      groupId聚合桶数量
     * @param artifactIdsField  artifactId聚合字段
     * @param artifactIdsSize   artifactId聚合桶数量
     * @return NativeQuery
     */
    private NativeQuery buildNativeQuery(String query,
                                         float groupIdWeight,
                                         float artifactIdWeight,
                                         float descriptionWeight,
                                         String multiMatchType,
                                         int maxResults,
                                         String groupIdsField,
                                         int groupIdsSize,
                                         String artifactIdsField,
                                         int artifactIdsSize) {
        // 构建字段列表（带权重）
        List<String> fields = List.of(String.format("groupId^%.1f", groupIdWeight), String
            .format("artifactId^%.1f", artifactIdWeight), String.format("description^%.1f", descriptionWeight));

        // 构建 MultiMatchQuery
        MultiMatchQuery multiMatchQuery = MultiMatchQuery.of(m -> m.query(query)
            .fields(fields)
            .type(ElasticsearchQueryUtils.getMultiMatchQueryType(multiMatchType)));

        // 构建 Elasticsearch Query
        var esQuery = ElasticsearchQueryUtils.buildElasticsearchQuery(multiMatchQuery);

        // 构建聚合
        // groupId 聚合
        TermsAggregation groupIdsTerms = TermsAggregation.of(t -> t.field(groupIdsField).size(groupIdsSize));
        Aggregation aggGroupIds = Aggregation.of(a -> a.terms(groupIdsTerms));

        // artifactId 聚合
        TermsAggregation artifactIdsTerms = TermsAggregation.of(t -> t.field(artifactIdsField).size(artifactIdsSize));
        Aggregation aggArtifactIds = Aggregation.of(a -> a.terms(artifactIdsTerms));

        // 构建分页
        Pageable pageable = PageRequest.of(0, maxResults);

        // 使用 NativeQuery.builder() 构建查询，分别添加每个聚合
        return NativeQuery.builder()
            .withQuery(esQuery)
            .withAggregation("agg_groupIds", aggGroupIds)
            .withAggregation("agg_artifactIds", aggArtifactIds)
            .withPageable(pageable)
            .build();
    }

    /**
     * 从聚合结果中提取StringTerms聚合数据
     * <p>
     * 将StringTerms聚合的buckets转换为Map，key为聚合的键值，value为文档计数
     * </p>
     *
     * @param aggregations    聚合容器（使用通配符类型，方法内部进行类型安全检查和转换）
     * @param aggregationName 聚合名称
     * @return Map<String, Long> 键值对映射，如果聚合不存在或不是StringTerms类型则返回null
     */
    private Map<String, Long> extractStringTermsAggregation(AggregationsContainer<?> aggregations,
                                                            String aggregationName) {
        if (aggregations == null || aggregationName == null || aggregationName.isEmpty()) {
            return null;
        }

        // 从聚合容器中获取聚合Map，使用模式匹配的instanceof来避免类型转换
        // 通过instanceof模式匹配，Java会自动进行类型检查和变量绑定，无需显式类型转换
        Map<String, Long> resultMap = new LinkedHashMap<>();

        if (aggregations instanceof ElasticsearchAggregations elasticAggs) {

            ElasticsearchAggregation tagsAgg = elasticAggs.get(aggregationName);
            if (tagsAgg != null) {
                tagsAgg.aggregation();
                if (tagsAgg.aggregation().getAggregate().sterms() != null) {
                    tagsAgg.aggregation().getAggregate().sterms().buckets().array().forEach(bucket -> {
                        resultMap.put(bucket.key().stringValue(), bucket.docCount());
                    });
                }
            }
        }

        return resultMap;
    }

    /**
     * 将SearchEntity.Config转换为RemoteMetaConfigVO.Config
     */
    private RemoteMetaConfigVO.Config convertToConfigVO(SearchEntity.Config config) {
        if (config == null) {
            return null;
        }

        RemoteMetaConfigVO.Config configVO = new RemoteMetaConfigVO.Config();
        configVO.setVersion(config.getVersion());

        // 转换所有FileInfo文件列表
        if (config.getFiles() != null && !config.getFiles().isEmpty()) {
            List<RemoteMetaConfigVO.FileInfo> fileInfoList = config.getFiles().stream().map(searchFileInfo -> {
                RemoteMetaConfigVO.FileInfo fileInfoVO = new RemoteMetaConfigVO.FileInfo();
                fileInfoVO.setFilePath(searchFileInfo.getFilePath());
                fileInfoVO.setDescription(searchFileInfo.getDescription());
                fileInfoVO.setFilename(searchFileInfo.getFilename());
                fileInfoVO.setSha256(searchFileInfo.getSha256());

                // 转换InputVariables
                if (searchFileInfo.getInputVariables() != null) {
                    fileInfoVO.setInputVariables(searchFileInfo.getInputVariables().stream().map(metaVar -> {
                        RemoteMetaConfigVO.InputVariable inputVar = new RemoteMetaConfigVO.InputVariable();
                        inputVar.setVariableName(metaVar.getVariableName());
                        inputVar.setVariableType(metaVar.getVariableType());
                        inputVar.setVariableComment(metaVar.getVariableComment());
                        inputVar.setExample(metaVar.getExample());
                        return inputVar;
                    }).toList());
                }

                return fileInfoVO;
            }).toList();

            configVO.setFiles(fileInfoList);
        }

        return configVO;
    }

    /**
     * 批量保存ES中的所有数据
     * 注意：此方法只用于全量同步，使用批量处理器进行优化
     */
    public boolean saveMetaDocsByList(List<SearchEntity> metaDocList) {
        if (metaDocList == null || metaDocList.isEmpty()) {
            log.info("文档列表为空，无需保存");
            return true;
        }

        log.info("开始批量保存 {} 条文档到ES", metaDocList.size());
        long startTime = System.currentTimeMillis();

        try {
            // 对于大批量数据，使用多线程并行处理
            if (metaDocList.size() > 1000) {
                processBatchInParallel(metaDocList);
            } else {
                // 小批量数据直接使用批量处理器
                remoteSearchESRepository.saveAll(metaDocList);
            }

            long endTime = System.currentTimeMillis();
            log.info("批量保存完成，共 {} 条文档，耗时: {}ms", metaDocList.size(), (endTime - startTime));
            return true;
        } catch (Exception e) {
            log.error("批量保存文档到ES失败: {}", e.getMessage(), e);
            // 不抛出异常，允许继续执行后续同步步骤
            log.info("插入操作异常，继续执行后续同步步骤");
        }
        return false;
    }

    /**
     * 并行处理大批量数据
     *
     * @param metaDocList 文档列表
     */
    private void processBatchInParallel(List<SearchEntity> metaDocList) {
        SearchProperties.ThreadPoolConfig config = threadPoolConfig.getThreadPoolConfig();
        int threadCount = config.getMaximumPoolSize();
        int batchSize = metaDocList.size() / threadCount;

        // 使用线程池配置工厂创建线程池
        ThreadPoolExecutor executorService = threadPoolConfig.createThreadPool();

        log.info("使用配置化线程池并行处理大批量数据，核心线程数: {}, 最大线程数: {}, 队列容量: {}, 每批 {} 条", config.getCorePoolSize(), config
            .getMaximumPoolSize(), config.getQueueCapacity(), batchSize);

        try {
            for (int i = 0; i < threadCount; i++) {
                final int start = i * batchSize;
                final int end = (i == threadCount - 1) ? metaDocList.size() : (i + 1) * batchSize;
                final List<SearchEntity> batch = metaDocList.subList(start, end);

                executorService.submit(() -> {
                    try {
                        remoteSearchESRepository.saveAll(batch);
                        log.debug("批次处理完成，范围: {}-{}, 数量: {}", start, end, batch.size());
                    } catch (Exception e) {
                        log.error("批量处理子任务失败: {}", e.getMessage(), e);
                        throw new RuntimeException("批量处理子任务失败", e);
                    }
                });
            }

            executorService.shutdown();
            boolean completed = executorService.awaitTermination(config.getAwaitTerminationMinutes(), TimeUnit.MINUTES);
            if (!completed) {
                log.warn("批量处理超时（{}分钟），部分任务可能未完成", config.getAwaitTerminationMinutes());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("批量处理被中断: {}", e.getMessage(), e);
            throw new RuntimeException("批量处理被中断", e);
        } finally {
            if (!executorService.isTerminated()) {
                log.warn("强制关闭线程池，未完成的任务将被中断");
                executorService.shutdownNow();
            }
        }
    }

    /**
     * 删除ES中的所有数据
     * 注意：此方法只用于全量同步，不应频繁调用
     * 如果索引不存在，则只记录信息日志，不报错
     */
    public void deleteAllMetaDocs() {
        try {
            // 先检查索引是否存在
            String indexName = "meta_info"; // 与 SearchEntity 中的 @Document(indexName = "meta_info") 保持一致
            if (elasticsearchOperations.indexOps(SearchEntity.class).exists()) {
                remoteSearchESRepository.deleteAll();
                log.info("成功删除ES中所有数据");
            } else {
                log.info("ES索引 {} 不存在，跳过删除操作", indexName);
            }
        } catch (org.springframework.data.elasticsearch.NoSuchIndexException e) {
            // 索引不存在的情况，只记录信息日志，不报错
            log.info("ES索引不存在，跳过删除操作（这是正常的，特别是在首次同步时）");
        } catch (Exception e) {
            // 其他异常，记录警告日志，但不抛出异常
            log.warn("删除ES中所有数据时出现异常，继续执行后续同步步骤: {}", e.getMessage());
        }
    }
}
