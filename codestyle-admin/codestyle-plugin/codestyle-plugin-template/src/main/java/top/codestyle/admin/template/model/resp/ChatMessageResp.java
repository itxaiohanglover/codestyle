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
import top.codestyle.admin.common.base.model.resp.BaseResp;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "对话消息")
public class ChatMessageResp extends BaseResp {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "所属会话ID")
    private Long sessionId;

    @Schema(description = "消息角色: user/assistant/system")
    private String role;

    @Schema(description = "消息内容")
    private String content;
}
