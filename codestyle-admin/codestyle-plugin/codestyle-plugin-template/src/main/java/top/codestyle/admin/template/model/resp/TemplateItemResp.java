/*
 * Copyright (c) 2022-present CodeStyle Authors. All Rights Reserved.
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

package top.codestyle.admin.template.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.codestyle.admin.common.base.model.resp.BaseDetailResp;

import java.io.Serial;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "模板列表项")
public class TemplateItemResp extends BaseDetailResp {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Maven groupId")
    private String groupId;

    @Schema(description = "Maven artifactId")
    private String artifactId;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "图标文字")
    private String icon;

    @Schema(description = "作者")
    private String author;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "标签列表")
    private List<TagItemResp> tags;

    @Schema(description = "下载次数")
    private Integer downloadCount;

    @Schema(description = "评分")
    private Double rating;

    @Schema(description = "是否已收藏")
    private Boolean isFavorite;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "可见性（0：私有；1：公开）")
    private Integer visibility;
}
