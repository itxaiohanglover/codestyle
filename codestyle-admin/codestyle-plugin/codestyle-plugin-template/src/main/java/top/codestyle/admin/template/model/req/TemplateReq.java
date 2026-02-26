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

package top.codestyle.admin.template.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import top.codestyle.admin.template.model.resp.TagItemResp;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "模板请求参数")
public class TemplateReq implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Size(max = 100)
    @Schema(description = "Maven groupId")
    private String groupId;

    @Size(max = 100)
    @Schema(description = "Maven artifactId")
    private String artifactId;

    @NotBlank(message = "模板名称不能为空")
    @Size(max = 200)
    @Schema(description = "模板名称")
    private String name;

    @NotBlank(message = "图标不能为空")
    @Size(max = 10)
    @Schema(description = "图标文字")
    private String icon;

    @NotBlank(message = "作者不能为空")
    @Size(max = 100)
    @Schema(description = "作者")
    private String author;

    @NotBlank(message = "描述不能为空")
    @Schema(description = "描述")
    private String description;

    @NotBlank(message = "版本号不能为空")
    @Size(max = 20)
    @Schema(description = "版本号")
    private String version;

    @Size(max = 500)
    @Schema(description = "下载地址")
    private String downloadUrl;

    @Size(max = 100)
    @Schema(description = "search 模块模板引用 ID")
    private String searchRefId;

    @Schema(description = "标签列表")
    private List<TagItemResp> tags;

    @Schema(description = "可见性（0：私有；1：公开，默认公开）")
    private Integer visibility;
}
