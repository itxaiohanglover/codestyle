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

package top.codestyle.admin.template.service;

import top.codestyle.admin.template.model.req.ChatMessageReq;
import top.codestyle.admin.template.model.resp.ChatMessageResp;
import top.codestyle.admin.template.model.resp.ChatResponseResp;
import top.codestyle.admin.template.model.resp.ChatSessionResp;
import top.codestyle.admin.template.model.resp.CodeSnippetResp;

import java.util.List;

/**
 * 对话会话服务
 */
public interface ChatSessionService {

    /**
     * 创建新会话
     */
    ChatSessionResp createSession();

    /**
     * 获取当前用户的会话列表
     */
    List<ChatSessionResp> listSessions();

    /**
     * 获取会话消息历史
     */
    List<ChatMessageResp> getSessionMessages(Long sessionId);

    /**
     * 删除会话
     */
    void deleteSession(Long sessionId);

    /**
     * 发送消息并获取AI回复
     */
    ChatResponseResp chat(ChatMessageReq req);

    /**
     * 获取会话的代码片段列表
     */
    List<CodeSnippetResp> getSessionSnippets(Long sessionId);

    /**
     * 更新代码片段
     */
    CodeSnippetResp updateSnippet(Long snippetId, String code);
}
