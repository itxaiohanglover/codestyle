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
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 检索请求
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Data
@Schema(description = "检索请求")
public class SearchRequest {

    @Schema(description = "查询文本", example = "如何配置 MySQL 连接池")
    @NotBlank(message = "查询文本不能为空")
    private String query;

    @Schema(description = "数据源类型")
    private SearchSourceType sourceType = SearchSourceType.HYBRID;

    @Schema(description = "返回结果数量", example = "10")
    private Integer topK = 10;

    @Schema(description = "是否启用重排", example = "true")
    private Boolean enableRerank = false;

    @Schema(description = "过滤条件")
    private Map<String, Object> filters;

    @Schema(description = "超时时间（毫秒）", example = "5000")
    private Long timeout = 5000L;
}

