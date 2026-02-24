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

package top.codestyle.admin.template.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.codestyle.admin.research.model.req.ResearchStartReq;
import top.codestyle.admin.research.model.resp.ResearchStatusResp;
import top.codestyle.admin.research.service.ResearchService;
import top.codestyle.admin.template.model.req.*;
import top.codestyle.admin.template.model.resp.*;
import top.codestyle.admin.template.service.ChatSessionService;
import top.codestyle.admin.template.service.TemplateService;
import top.continew.starter.extension.tenant.annotation.TenantIgnore;

import java.util.List;

@Slf4j
@Tag(name = "AI 代码生成 API")
@Validated
@RestController
@RequiredArgsConstructor
@TenantIgnore
@ConditionalOnClass(name = "org.springframework.ai.chat.client.ChatClient")
@RequestMapping("/template/generate")
public class GenerateController {

    private final ChatSessionService chatSessionService;
    private final TemplateService templateService;
    private final ResearchService researchService;

    @Operation(summary = "发送对话消息")
    @PostMapping("/chat")
    public ChatResponseResp chat(@Valid @RequestBody ChatMessageReq req) {
        return chatSessionService.chat(req);
    }

    @Operation(summary = "创建新会话")
    @PostMapping("/session")
    public ChatSessionResp createSession() {
        return chatSessionService.createSession();
    }

    @Operation(summary = "获取会话列表")
    @GetMapping("/session/list")
    public List<ChatSessionResp> listSessions() {
        return chatSessionService.listSessions();
    }

    @Operation(summary = "获取会话消息历史")
    @GetMapping("/session/{sessionId}/messages")
    public List<ChatMessageResp> getSessionMessages(@Parameter(description = "会话ID") @PathVariable Long sessionId) {
        return chatSessionService.getSessionMessages(sessionId);
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/session/{sessionId}")
    public void deleteSession(@Parameter(description = "会话ID") @PathVariable Long sessionId) {
        chatSessionService.deleteSession(sessionId);
    }

    @Operation(summary = "获取会话代码片段列表")
    @GetMapping("/session/{sessionId}/snippets")
    public List<CodeSnippetResp> getSessionSnippets(@Parameter(description = "会话ID") @PathVariable Long sessionId) {
        return chatSessionService.getSessionSnippets(sessionId);
    }

    @Operation(summary = "更新代码片段")
    @PutMapping("/snippet/{snippetId}")
    public CodeSnippetResp updateSnippet(@Parameter(description = "片段ID") @PathVariable Long snippetId,
                                         @Valid @RequestBody UpdateSnippetReq req) {
        return chatSessionService.updateSnippet(snippetId, req.getCode());
    }

    @Operation(summary = "保存代码片段到模板库")
    @PostMapping("/snippet/{snippetId}/save-to-library")
    public Long saveSnippetToLibrary(@Parameter(description = "片段ID") @PathVariable Long snippetId,
                                     @RequestBody(required = false) SaveToLibraryReq req) {
        return templateService.saveSnippetToLibrary(snippetId, req != null ? req.getName() : null, req != null
            ? req.getDescription()
            : null, req != null ? req.getTags() : null);
    }

    @Operation(summary = "启动深度研究任务 (委托 research 模块)")
    @PostMapping("/research/start")
    public ResearchStatusResp startResearch(@Valid @RequestBody ResearchStartReq req) {
        return researchService.startResearch(req);
    }

    @Operation(summary = "订阅研究任务进度 (SSE)")
    @GetMapping("/research/{taskId}/progress")
    public SseEmitter getResearchProgress(@Parameter(description = "任务ID") @PathVariable String taskId) {
        return researchService.subscribeProgress(taskId);
    }

    @Operation(summary = "取消研究任务")
    @DeleteMapping("/research/{taskId}")
    public void cancelResearch(@Parameter(description = "任务ID") @PathVariable String taskId) {
        researchService.cancelTask(taskId);
    }
}
