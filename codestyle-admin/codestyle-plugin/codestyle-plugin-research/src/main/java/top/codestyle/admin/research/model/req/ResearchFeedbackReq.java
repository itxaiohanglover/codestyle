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

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户反馈请求
 *
 * @author CodeStyle
 * @since 2026-02-22
 */
@Data
@Schema(description = "用户反馈请求")
public class ResearchFeedbackReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 反馈内容
     */
    @Schema(description = "反馈内容", example = "请调整模板变量名称")
    @NotBlank(message = "反馈内容不能为空")
    private String feedback;

    /**
     * 是否继续执行
     */
    @Schema(description = "是否继续执行", example = "true")
    private Boolean continueExecution = true;
}
