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
}

