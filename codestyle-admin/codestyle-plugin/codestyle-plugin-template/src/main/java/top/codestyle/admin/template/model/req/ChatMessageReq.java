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
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "发送聊天消息请求")
public class ChatMessageReq implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "会话ID")
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    @Schema(description = "消息内容")
    @NotBlank(message = "消息内容不能为空")
    private String message;

    @Schema(description = "是否启用深度研究模式")
    private Boolean deepResearch = false;
}
