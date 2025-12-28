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

package top.codestyle.admin.search.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.codestyle.admin.search.entity.RemoteMetaDO;
import top.codestyle.admin.search.entity.SearchEntity;
import top.codestyle.admin.search.mapper.RemoteMetaInfoMapper;
import top.codestyle.admin.search.repository.RemoteSearchESRepositoryImpl;
import top.codestyle.admin.search.service.SyncService;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * ES同步服务实现类
 * 
 * <p><b>架构说明：</b></p>
 * <ul>
 * <li><b>全量同步</b>：应用启动时执行一次，将MySQL中所有数据同步到ES</li>
 * <li><b>增量同步</b>：通过Canal+Kafka实现，实时同步MySQL变更到ES</li>
 * </ul>
 * 
 * <p><b>增量同步流程：</b>MySQL Binlog → Canal Server → Kafka → Kafka消费者 → ES</p>
 * 
 * @author ChonghaoGao
 * @date 2025/12/22
 */
@Slf4j
@Service
@AllArgsConstructor
public class SyncServiceImpl implements SyncService {

    private final RemoteMetaInfoMapper remoteMetaInfoMapper;
    private final RemoteSearchESRepositoryImpl remoteSearchESRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 全量同步：将MySQL中所有数据同步到ES
     * 
     * <p>注意：此方法会先删除ES中的所有数据，然后重新载入，不应频繁调用。</p>
     * <p>通常在应用启动时执行一次，后续增量同步通过Canal+Kafka实现。</p>
     * 
     * @return 同步成功的数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int fullSync() {
        log.info("开始全量同步MySQL数据到ES");

        try {
            // 1. 先删除ES中的所有数据
            remoteSearchESRepository.deleteAllMetaDocs();

            // 2. 查询MySQL中所有数据（未被逻辑删除的）
            List<RemoteMetaDO> allMetaInfos = remoteMetaInfoMapper.selectList(null);
            log.info("从MySQL查询到 {} 条数据", allMetaInfos.size());

            if (allMetaInfos.isEmpty()) {
                log.info("MySQL中没有数据，无需同步");
                return 0;
            }

            // 3. 转换数据并批量处理
            int totalCount = allMetaInfos.size();
            List<SearchEntity> allMetaDocs = allMetaInfos.parallelStream().map(this::convertToSearchEntity).toList();
            remoteSearchESRepository.saveMetaDocsByList(allMetaDocs);

            log.info("全量同步完成，成功同步 {} 条数据", totalCount);
            return totalCount;

        } catch (Exception e) {
            log.error("全量同步失败: {}", e.getMessage(), e);
            throw new RuntimeException("全量同步失败", e);
        }
    }

    /**
     * 将RemoteMetaDO转换为SearchEntity
     * <p>
     * 从metaJson中解析出groupId、artifactId、description和config等字段
     * </p>
     *
     * @param remoteMetaDO MySQL数据对象
     * @return ES文档对象
     */
    private SearchEntity convertToSearchEntity(RemoteMetaDO remoteMetaDO) {
        try {
            SearchEntity searchEntity = new SearchEntity();
            searchEntity.setId(remoteMetaDO.getId());

            // 从metaJson中解析数据
            if (remoteMetaDO.getMetaJson() != null && !remoteMetaDO.getMetaJson().trim().isEmpty()) {
                JsonNode metaJsonNode = objectMapper.readTree(remoteMetaDO.getMetaJson());

                // 解析基本字段
                searchEntity.setGroupId(getJsonStringValue(metaJsonNode, "groupId"));
                searchEntity.setArtifactId(getJsonStringValue(metaJsonNode, "artifactId"));
                searchEntity.setDescription(getJsonStringValue(metaJsonNode, "description"));

                // 解析config字段
                JsonNode configNode = metaJsonNode.get("config");
                if (configNode != null && !configNode.isNull()) {
                    SearchEntity.Config config = parseConfig(configNode);
                    searchEntity.setConfig(config);
                }
            } else {
                // 如果metaJson为空，尝试使用虚拟字段（可能在某些查询中被填充）
                searchEntity.setGroupId(remoteMetaDO.getGroupId());
                searchEntity.setArtifactId(remoteMetaDO.getArtifactId());
                searchEntity.setDescription(remoteMetaDO.getDescription());
            }

            return searchEntity;
        } catch (Exception e) {
            log.error("转换RemoteMetaDO到SearchEntity失败，id: {}, error: {}", remoteMetaDO.getId(), e.getMessage(), e);
            // 返回一个只有id的SearchEntity，避免整个同步失败
            SearchEntity searchEntity = new SearchEntity();
            searchEntity.setId(remoteMetaDO.getId());
            return searchEntity;
        }
    }

    /**
     * 从JsonNode中获取字符串值
     */
    private String getJsonStringValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode != null && !fieldNode.isNull()) {
            return fieldNode.asText();
        }
        return null;
    }

    /**
     * 解析Config对象
     */
    private SearchEntity.Config parseConfig(JsonNode configNode) {
        SearchEntity.Config config = new SearchEntity.Config();
        config.setVersion(getJsonStringValue(configNode, "version"));

        // 解析files数组
        JsonNode filesNode = configNode.get("files");
        if (filesNode != null && filesNode.isArray()) {
            List<SearchEntity.Config.FileInfo> files = new ArrayList<>();
            for (JsonNode fileNode : filesNode) {
                SearchEntity.Config.FileInfo fileInfo = parseFileInfo(fileNode);
                files.add(fileInfo);
            }
            config.setFiles(files);
        }

        return config;
    }

    /**
     * 解析FileInfo对象
     */
    private SearchEntity.Config.FileInfo parseFileInfo(JsonNode fileNode) {
        SearchEntity.Config.FileInfo fileInfo = new SearchEntity.Config.FileInfo();
        fileInfo.setFilename(getJsonStringValue(fileNode, "filename"));
        fileInfo.setSha256(getJsonStringValue(fileNode, "sha256"));
        fileInfo.setFilePath(getJsonStringValue(fileNode, "filePath"));
        fileInfo.setDescription(getJsonStringValue(fileNode, "description"));

        // 解析inputVariables数组
        JsonNode inputVariablesNode = fileNode.get("inputVariables");
        if (inputVariablesNode != null && inputVariablesNode.isArray()) {
            List<SearchEntity.Config.FileInfo.MetaVariable> inputVariables = new ArrayList<>();
            for (JsonNode varNode : inputVariablesNode) {
                SearchEntity.Config.FileInfo.MetaVariable variable = new SearchEntity.Config.FileInfo.MetaVariable();
                variable.setVariableName(getJsonStringValue(varNode, "variableName"));
                variable.setVariableType(getJsonStringValue(varNode, "variableType"));
                variable.setVariableComment(getJsonStringValue(varNode, "variableComment"));
                variable.setExample(getJsonStringValue(varNode, "example"));
                inputVariables.add(variable);
            }
            fileInfo.setInputVariables(inputVariables);
        }

        return fileInfo;
    }
}
