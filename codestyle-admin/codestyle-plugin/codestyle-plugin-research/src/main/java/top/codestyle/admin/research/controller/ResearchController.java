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

package top.codestyle.admin.research.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.codestyle.admin.research.model.req.ResearchFeedbackReq;
import top.codestyle.admin.research.model.req.ResearchStartReq;
import top.codestyle.admin.research.model.resp.ResearchStatusResp;
import top.codestyle.admin.research.service.ResearchService;

/**
 * 深度研究 API
 *
 * @author CodeStyle
 * @since 2026-02-22
 */
@Slf4j
@Tag(name = "深度研究 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/research/task")
public class ResearchController {

    private final ResearchService researchService;

    @Operation(summary = "启动研究任务", description = "启动一个新的深度研究任务")
    @PostMapping("/start")
    public ResearchStatusResp start(@Valid @RequestBody ResearchStartReq request) {
        log.info("启动研究任务: sourceType={}, sourceContent={}", request.getSourceType(), request.getSourceContent());
        return researchService.startResearch(request);
    }

    @Operation(summary = "获取任务状态", description = "通过 SSE 流式获取任务进度")
    @GetMapping(value = "/{taskId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getStatus(@Parameter(description = "任务 ID", required = true) @PathVariable String taskId) {
        log.info("订阅任务状态: taskId={}", taskId);
        return researchService.subscribeProgress(taskId);
    }

    @Operation(summary = "提交用户反馈", description = "在等待确认节点提交用户反馈")
    @PostMapping("/{taskId}/feedback")
    public void submitFeedback(@Parameter(description = "任务 ID", required = true) @PathVariable String taskId,
                               @Valid @RequestBody ResearchFeedbackReq request) {
        log.info("提交用户反馈: taskId={}, feedback={}", taskId, request.getFeedback());
        researchService.submitFeedback(taskId, request);
    }

    @Operation(summary = "确认继续执行", description = "确认中断节点继续执行")
    @PostMapping("/{taskId}/confirm")
    public void confirm(@Parameter(description = "任务 ID", required = true) @PathVariable String taskId) {
        log.info("确认继续执行: taskId={}", taskId);
        researchService.confirmContinue(taskId);
    }

    @Operation(summary = "取消任务", description = "取消正在执行的研究任务")
    @PostMapping("/{taskId}/cancel")
    public void cancel(@Parameter(description = "任务 ID", required = true) @PathVariable String taskId) {
        log.info("取消任务: taskId={}", taskId);
        researchService.cancelTask(taskId);
    }
}
