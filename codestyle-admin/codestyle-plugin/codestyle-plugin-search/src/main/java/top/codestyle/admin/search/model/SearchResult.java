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

package top.codestyle.admin.search.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 检索结果
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "检索结果")
public class SearchResult {

    @Schema(description = "文档 ID")
    private String id;

    @Schema(description = "数据源类型")
    private SearchSourceType sourceType;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容摘要")
    private String snippet;

    @Schema(description = "完整内容")
    private String content;

    @Schema(description = "相关性分数")
    private Double score;

    @Schema(description = "排名")
    private Integer rank;

    @Schema(description = "元数据")
    private Map<String, Object> metadata;

    @Schema(description = "高亮片段")
    private String highlight;

    // ==================== MCP 必要字段（需要索引）====================

    @Schema(description = "组 ID（MCP）")
    private String groupId;

    @Schema(description = "项目 ID（MCP）")
    private String artifactId;

    @Schema(description = "版本号（MCP）")
    private String version;

    @Schema(description = "文件类型（MCP）")
    private String fileType;

    // ==================== 辅助方法：从 metadata 提取非索引字段 ====================

    /**
     * 获取文件路径（从 metadata 中提取）
     */
    public String getFilePath() {
        return metadata != null ? (String) metadata.get("filePath") : null;
    }

    /**
     * 获取文件名（从 metadata 中提取）
     */
    public String getFilename() {
        return metadata != null ? (String) metadata.get("filename") : null;
    }

    /**
     * 获取 SHA256 哈希值（从 metadata 中提取）
     */
    public String getSha256() {
        return metadata != null ? (String) metadata.get("sha256") : null;
    }

    /**
     * 获取下载 URL（动态生成）
     */
    public String getDownloadUrl() {
        if (groupId != null && artifactId != null && version != null) {
            return String.format("/api/file/download?groupId=%s&artifactId=%s&version=%s",
                groupId, artifactId, version);
        }
        return null;
    }
}
