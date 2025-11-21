package top.codestyle.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Service;
import top.codestyle.entity.es.pojo.McpSearchDO;
import top.codestyle.entity.es.vo.McpSearchResultVO;
import top.codestyle.repository.McpTemplateRepository;
import top.codestyle.service.McpSearchService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
@AllArgsConstructor
@Service
public class McpSearchServiceImpl implements McpSearchService {

    private final McpTemplateRepository mcpTemplateRepository;
    private final McpTemplateRepository repository;

    @Override
    public McpSearchResultVO searchMcp(String templateKeyword) {
        if (StringUtils.isBlank(templateKeyword)) {
            log.warn("搜索关键词为空");
            return null;
        }
        Optional<McpSearchResultVO> mcpSearchResult = repository.searchTopVersionTemplateByKeyword(templateKeyword);
        return mcpSearchResult.orElse(null);
    }


    /**
     * 根据meta.json文件自动保存到Elasticsearch
     * @param metaJsonFile meta.json文件
     * @return 保存结果
     */
    @Override
    public boolean saveMetaJsonToElasticsearch(File metaJsonFile) throws IOException {
        // 1. 检查并创建索引
        mcpTemplateRepository.createIndex();

        // 2. 解析meta.json文件
        McpSearchDO mcpSearchDO = parseMetaJsonAndBuildMcpSearchDO(metaJsonFile);


        // 4. 保存到Elasticsearch
        mcpTemplateRepository.saveOrUpdateTemplate(mcpSearchDO);
        log.info("Meta.json文件保存到Elasticsearch成功: groupId={}, artifactId={}, version={}",
                mcpSearchDO.getGroupId(), mcpSearchDO.getArtifactId(), mcpSearchDO.getConfigs().get(0).getVersion());
        return true;
    }

    
    /**
     * 解析meta.json并构建McpSearchDO对象
     */
    private McpSearchDO parseMetaJsonAndBuildMcpSearchDO(File metaJsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(metaJsonFile);

        McpSearchDO doc = new McpSearchDO();

        /* ---------- 顶层基本信息 ---------- */
        doc.setGroupId(root.path("groupId").asText());
        doc.setArtifactId(root.path("artifactId").asText());
        doc.setId(doc.getGroupId() + ":" + doc.getArtifactId()); // @Id
        doc.setCreateTime(new Date());
        doc.setUpdateTime(new Date());
        doc.setIsDelete(0);
        // 统计先给默认值，业务层再补充
        doc.setTotalLikeCount(0L);
        doc.setTotalFavoriteCount(0L);
        doc.setHotScoreWeight(0D);

        /* ---------- configs 嵌套数组 ---------- */
        List<McpSearchDO.Config> configList = new ArrayList<>();
        JsonNode configsNode = root.path("configs");
        if (configsNode.isArray()) {
            for (JsonNode cfg : configsNode) {
                McpSearchDO.Config config = new McpSearchDO.Config();
                config.setVersion(cfg.path("version").asText());

                /* ------ files 嵌套数组 ------ */
                List<McpSearchDO.FileInfo> fileInfoList = new ArrayList<>();
                JsonNode filesNode = cfg.path("files");
                if (filesNode.isArray()) {
                    for (JsonNode file : filesNode) {
                        McpSearchDO.FileInfo fileInfo = new McpSearchDO.FileInfo();
                        fileInfo.setFilePath(file.path("filePath").asText());
                        fileInfo.setFilename(file.path("filename").asText());
                        fileInfo.setDescription(file.path("description").asText());
                        fileInfo.setSha256(file.path("sha256").asText());

                        /* -- inputVariables 嵌套数组 -- */
                        List<McpSearchDO.InputVariable> varList = new ArrayList<>();
                        JsonNode varsNode = file.path("inputVariables");
                        if (varsNode.isArray()) {
                            for (JsonNode var : varsNode) {
                                McpSearchDO.InputVariable iv = new McpSearchDO.InputVariable();
                                iv.setVariableName(var.path("variableName").asText());
                                iv.setVariableType(var.path("variableType").asText());
                                iv.setVariableComment(var.path("variableComment").asText());
                                iv.setExample(var.path("example").asText());
                                varList.add(iv);
                            }
                        }
                        fileInfo.setInputVariables(varList);
                        fileInfoList.add(fileInfo);
                    }
                }
                config.setFiles(fileInfoList);
                configList.add(config);
            }
        }
        doc.setConfigs(configList);


        long total = configList.stream()
                .mapToLong(c -> c.getFiles() == null ? 0 : c.getFiles().size())
                .sum();
        doc.setTotalFileCount(total);

        return doc;
    }

}
