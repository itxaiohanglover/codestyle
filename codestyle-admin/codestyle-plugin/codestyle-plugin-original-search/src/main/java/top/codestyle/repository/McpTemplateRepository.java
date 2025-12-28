package top.codestyle.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;
import top.codestyle.entity.es.pojo.McpSearchDO;
import top.codestyle.entity.es.vo.McpSearchResultVO;
import top.codestyle.properties.ElasticsearchSearchProperties;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class McpTemplateRepository {

    private final ElasticsearchClient client;
    private final ElasticsearchTemplate elasticsearchTemplate;
    private final ElasticsearchSearchProperties properties;

    private final String INDEX_NAME = "template_mcp";

    /**
     * 根据关键词搜索模板，并返回版本最高的匹配结果
     */
    public Optional<McpSearchResultVO> searchTopVersionTemplateByKeyword(String keyword) {
        try {
            NativeQuery nativeQuery = createMcpNativeQuery(keyword);

            SearchHits<McpSearchDO> hits = elasticsearchTemplate.search(
                    nativeQuery,
                    McpSearchDO.class,
                    IndexCoordinates.of(INDEX_NAME)
            );

            List<McpSearchDO> templates = hits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            if (templates.isEmpty()) {
                log.info("未找到匹配关键词: {} 的模板", keyword);
                return Optional.empty();
            }

            // 选取版本最高的模板
            McpSearchResultVO highestVersionTemplate = getHighestVersionTemplate(templates);
            log.debug("找到最高版本模板: {}, 版本号: {}", highestVersionTemplate.getGroupId(), highestVersionTemplate.getConfig().getVersion());

            return Optional.of(highestVersionTemplate);
        } catch (Exception e) {
            log.error("MCP模板搜索失败: {}", e.getMessage(), e);
            throw new RuntimeException("MCP模板搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建MCP专用的NativeQuery
     */
    private NativeQuery createMcpNativeQuery(String keyword) {
        Query baseQuery = buildMcpBoolQuery(keyword);

        int maxResults = properties.getVersionSortConfig() != null && properties.getVersionSortConfig().getMaxCandidateResults() != null
                ? properties.getVersionSortConfig().getMaxCandidateResults()
                : 50;

        NativeQueryBuilder builder = NativeQuery.builder()
                .withQuery(baseQuery)
                .withPageable(PageRequest.of(0, maxResults))
                .withSort(s -> s
                        .field(f -> f
                                .field("hotScoreWeight")
                                .order(SortOrder.Desc)
                        )
                );

        return builder.build();
    }

    /**
     * 构建MCP专用的布尔查询
     */
    private Query buildMcpBoolQuery(String keyword) {
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        // 多字段匹配查询
        MultiMatchQuery.Builder multiBuilder = new MultiMatchQuery.Builder()
                .query(keyword)
                .fields("artifactId", "groupId", "description", "configs.files.filename", "configs.files.description");

        if (properties.getMinimumShouldMatch() != null) {
            multiBuilder.minimumShouldMatch(properties.getMinimumShouldMatch());
        }

        boolBuilder.must(multiBuilder.build()._toQuery());

        // 默认过滤：未删除
        boolBuilder.filter(TermQuery.of(t -> t.field("isDelete").value(0))._toQuery());

        return boolBuilder.build()._toQuery();
    }

    /**
     * 选取版本号最高的模板
     */
    private McpSearchResultVO getHighestVersionTemplate(List<McpSearchDO> templates) {
        McpSearchResultVO highestVersionResult = null;
        String highestVersion = "0.0.0";

        for (McpSearchDO template : templates) {
            if (template.getConfigs() != null) {
                for (McpSearchDO.Config config : template.getConfigs()) {
                    if (compareVersions(config.getVersion(), highestVersion) > 0) {
                        highestVersion = config.getVersion();
                        highestVersionResult = convertToVO(template, config);
                    }
                }
            }
        }

        // 如果没有找到合适的结果，返回第一个
        if (highestVersionResult == null && !templates.isEmpty()) {
            McpSearchDO template = templates.get(0);
            if (template.getConfigs() != null && !template.getConfigs().isEmpty()) {
                highestVersionResult = convertToVO(template, template.getConfigs().get(0));
            }
        }

        return highestVersionResult;
    }

    /**
     * 版本号比较
     */
    private int compareVersions(String version1, String version2) {
        if (version1 == null || version2 == null) {
            return 0;
        }

        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        int minLength = Math.min(parts1.length, parts2.length);

        for (int i = 0; i < minLength; i++) {
            try {
                int num1 = Integer.parseInt(parts1[i]);
                int num2 = Integer.parseInt(parts2[i]);
                if (num1 != num2) {
                    return num1 - num2;
                }
            } catch (NumberFormatException e) {
                // 如果不是数字，按字符串比较
                return parts1[i].compareTo(parts2[i]);
            }
        }

        return parts1.length - parts2.length;
    }

    /**
     * 转换为VO
     */
    private McpSearchResultVO convertToVO(McpSearchDO template, McpSearchDO.Config config) {
        McpSearchResultVO vo = new McpSearchResultVO();
        vo.setGroupId(template.getGroupId());
        vo.setArtifactId(template.getArtifactId());
        vo.setDescription("待初始化");

        McpSearchResultVO.Config voConfig = new McpSearchResultVO.Config();
        voConfig.setVersion(config.getVersion().replaceFirst("^[vV]", ""));
        
        if (config.getFiles() != null) {
            List<McpSearchResultVO.FileInfo> fileInfos = config.getFiles().stream().map(file -> {
                McpSearchResultVO.FileInfo fileInfo = new McpSearchResultVO.FileInfo();
                fileInfo.setFilePath(file.getFilePath());
                fileInfo.setDescription(file.getDescription());
                fileInfo.setFilename(file.getFilename());
                fileInfo.setSha256(file.getSha256());
                
                if (file.getInputVariables() != null) {
                    fileInfo.setInputVarivales(file.getInputVariables().stream().map(var -> {
                        McpSearchResultVO.InputVarivales inputVar = new McpSearchResultVO.InputVarivales();
                        inputVar.setVariableName(var.getVariableName());
                        inputVar.setVariableType(var.getVariableType());
                        inputVar.setVariableComment(var.getVariableComment());
                        inputVar.setExample(var.getExample());
                        return inputVar;
                    }).collect(Collectors.toList()));
                }
                
                return fileInfo;
            }).collect(Collectors.toList());
            voConfig.setFiles(fileInfos);
        }
        
        vo.setConfig(voConfig);
        return vo;
    }

    
    /**
     * 根据groupId和artifactId查找模板
     */
    public Optional<McpSearchDO> findTemplateByGroupIdAndArtifactId(String groupId, String artifactId) {
        try {
            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(qb -> qb
                            .bool(b -> b
                                    .must(m1 -> m1.term(t -> t.field("groupId").value(groupId)))
                                    .must(m2 -> m2.term(t -> t.field("artifactId").value(artifactId)))
                            )
                    )
                    .build();
    
            SearchHits<McpSearchDO> hits = elasticsearchTemplate.search(
                    nativeQuery,
                    McpSearchDO.class,
                    IndexCoordinates.of("template_mcp")
            );
    
            if (hits.getSearchHits().isEmpty()) {
                return Optional.empty();
            }
    
            return Optional.of(hits.getSearchHits().get(0).getContent());
        } catch (Exception e) {
            log.error("查找模板失败: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * 保存或更新模板
     * 如果模板已存在，则更新版本信息
     */
    public void saveOrUpdateTemplate(McpSearchDO newTemplate) {
        // 查找现有模板
        Optional<McpSearchDO> existingTemplateOpt = findTemplateByGroupIdAndArtifactId(
                newTemplate.getGroupId(), newTemplate.getArtifactId());
    
        if (existingTemplateOpt.isPresent()) {
            // 模板已存在，更新版本信息
            McpSearchDO existingTemplate = existingTemplateOpt.get();
            
            // 检查版本是否已存在
            boolean versionExists = existingTemplate.getConfigs().stream()
                    .anyMatch(config -> config.getVersion().equals(newTemplate.getConfigs().get(0).getVersion()));
    
            if (!versionExists) {
                // 版本不存在，添加新版本
                existingTemplate.getConfigs().add(newTemplate.getConfigs().get(0));
                existingTemplate.setUpdateTime(new Date());
                existingTemplate.setTotalFileCount(newTemplate.getTotalFileCount());
                
                // 保存更新后的模板
                elasticsearchTemplate.save(existingTemplate, IndexCoordinates.of("template_mcp"));
                log.info("更新模板成功: {}, 新增版本: {}, 总版本数: {}", 
                        existingTemplate.getId(), 
                        newTemplate.getConfigs().get(0).getVersion(), 
                        existingTemplate.getConfigs().size());
            } else {
                // 版本已存在，替换该版本
                List<McpSearchDO.Config> updatedConfigs = existingTemplate.getConfigs().stream()
                        .map(config -> config.getVersion().equals(newTemplate.getConfigs().get(0).getVersion()) 
                                ? newTemplate.getConfigs().get(0) 
                                : config)
                        .collect(Collectors.toList());
                
                existingTemplate.setConfigs(updatedConfigs);
                existingTemplate.setUpdateTime(new Date());
                existingTemplate.setTotalFileCount(newTemplate.getTotalFileCount());
                
                // 保存更新后的模板
                elasticsearchTemplate.save(existingTemplate, IndexCoordinates.of("template_mcp"));
                log.info("替换模板版本成功: {}, 版本: {}", 
                        existingTemplate.getId(), 
                        newTemplate.getConfigs().get(0).getVersion());
            }
        } else {
            // 模板不存在，创建新模板
            elasticsearchTemplate.save(newTemplate, IndexCoordinates.of("template_mcp"));
            log.info("创建新模板成功: {}, 版本: {}", 
                    newTemplate.getId(), 
                    newTemplate.getConfigs().get(0).getVersion());
        }
    }

    @PostConstruct
    public void logInit() {
        log.info("MCP模板仓库初始化完成");
    }

    /**
     * 创建索引
     */
    public void createIndex() throws IOException {
        boolean exists = client.indices().exists(new ExistsRequest.Builder()
                .index(INDEX_NAME)
                .build()).value();
    
        if (!exists) {
            try {
                // 使用ClassPathResource从类路径加载配置文件
                ClassPathResource resource = new ClassPathResource("elasticsearch/template-settings.json");
                
                if (resource.exists()) {
                    // 文件存在时使用配置文件创建索引
                    try (InputStream inputStream = resource.getInputStream()) {
                        client.indices().create(new CreateIndexRequest.Builder()
                                .index(INDEX_NAME)
                                .withJson(inputStream)
                                .build());
                        log.info("使用配置文件创建索引 {} 成功", INDEX_NAME);
                    }
                } else {
                    // 文件不存在时，创建默认索引
                    client.indices().create(new CreateIndexRequest.Builder()
                            .index(INDEX_NAME)
                            .build());
                    log.warn("未找到索引配置文件 elasticsearch/template-settings.json，已创建默认索引 {}", INDEX_NAME);
                }
            } catch (Exception e) {
                log.error("创建索引失败: {}", e.getMessage(), e);
                // 向上抛出异常，让调用者知道发生了错误
                throw new IOException("创建Elasticsearch索引失败: " + e.getMessage(), e);
            }
        } else {
            log.info("索引 {} 已存在，无需创建", INDEX_NAME);
        }
    }

}