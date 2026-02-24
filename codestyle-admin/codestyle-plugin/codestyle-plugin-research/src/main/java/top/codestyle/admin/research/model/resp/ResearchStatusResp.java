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
import top.codestyle.admin.research.model.enums.ResearchStatus;
import top.codestyle.admin.search.model.resp.TemplateUploadResp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 研究任务状态响应
 *
 * @author CodeStyle
 * @since 2026-02-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "研究任务状态响应")
public class ResearchStatusResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务 ID
     */
    @Schema(description = "任务 ID", example = "123456")
    private String taskId;

    /**
     * 任务状态
     */
    @Schema(description = "任务状态", example = "RUNNING")
    private ResearchStatus status;

    /**
     * 当前节点
     */
    @Schema(description = "当前节点", example = "PlannerNode")
    private String currentNode;

    /**
     * 节点进度列表
     */
    @Schema(description = "节点进度列表")
    private List<NodeProgressResp> nodeProgress;

    /**
     * 最终结果（模板信息）
     */
    @Schema(description = "最终结果")
    private TemplateUploadResp result;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String error;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 完成时间
     */
    @Schema(description = "完成时间")
    private LocalDateTime completeTime;

    /**
     * 总耗时（毫秒）
     */
    @Schema(description = "总耗时（毫秒）")
    private Long totalDurationMs;
}
