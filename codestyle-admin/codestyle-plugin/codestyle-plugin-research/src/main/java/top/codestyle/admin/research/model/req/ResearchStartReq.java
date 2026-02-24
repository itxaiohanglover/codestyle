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

package top.codestyle.admin.research.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import top.codestyle.admin.research.model.enums.SourceType;

import java.io.Serial;
import java.io.Serializable;

/**
 * 启动研究任务请求
 *
 * @author CodeStyle
 * @since 2026-02-22
 */
@Data
@Schema(description = "启动研究任务请求")
public class ResearchStartReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 输入源类型
     */
    @Schema(description = "输入源类型", example = "GITHUB")
    @NotBlank(message = "输入源类型不能为空")
    private SourceType sourceType;

    /**
     * 输入内容（根据类型不同，可能是代码片段、文件路径、GitHub URL 等）
     */
    @Schema(description = "输入内容", example = "https://github.com/username/repo")
    @NotBlank(message = "输入内容不能为空")
    private String sourceContent;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称", example = "CRUD 模板")
    private String templateName;

    /**
     * 模板描述
     */
    @Schema(description = "模板描述", example = "基于 Spring Boot 的 CRUD 操作模板")
    private String templateDescription;

    /**
     * 是否自动确认（跳过用户确认节点）
     */
    @Schema(description = "是否自动确认", example = "false")
    private Boolean autoConfirm = false;
}
