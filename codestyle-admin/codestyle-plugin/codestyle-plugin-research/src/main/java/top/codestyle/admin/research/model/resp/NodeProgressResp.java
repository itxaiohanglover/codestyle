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

package top.codestyle.admin.research.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.codestyle.admin.research.model.enums.NodeStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 节点进度响应
 *
 * @author CodeStyle
 * @since 2026-02-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "节点进度响应")
public class NodeProgressResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 节点名称
     */
    @Schema(description = "节点名称", example = "SourceNode")
    private String nodeName;

    /**
     * 节点状态
     */
    @Schema(description = "节点状态", example = "COMPLETED")
    private NodeStatus status;

    /**
     * 进度消息
     */
    @Schema(description = "进度消息", example = "正在解析输入源...")
    private String message;

    /**
     * 节点结果（JSON 格式）
     */
    @Schema(description = "节点结果")
    private Object result;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String error;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    /**
     * 耗时（毫秒）
     */
    @Schema(description = "耗时（毫秒）")
    private Long durationMs;
}
