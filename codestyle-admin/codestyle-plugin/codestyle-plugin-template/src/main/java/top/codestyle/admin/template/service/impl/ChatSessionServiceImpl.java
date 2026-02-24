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

package top.codestyle.admin.template.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.codestyle.admin.common.context.UserContextHolder;
import top.codestyle.admin.research.model.req.ResearchStartReq;
import top.codestyle.admin.research.model.resp.ResearchStatusResp;
import top.codestyle.admin.research.service.ResearchService;
import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.service.SearchService;
import top.codestyle.admin.template.mapper.ChatMessageMapper;
import top.codestyle.admin.template.mapper.ChatSessionMapper;
import top.codestyle.admin.template.mapper.CodeSnippetMapper;
import top.codestyle.admin.template.model.entity.ChatMessageDO;
import top.codestyle.admin.template.model.entity.ChatSessionDO;
import top.codestyle.admin.template.model.entity.CodeSnippetDO;
import top.codestyle.admin.template.model.req.ChatMessageReq;
import top.codestyle.admin.template.model.resp.ChatMessageResp;
import top.codestyle.admin.template.model.resp.ChatResponseResp;
import top.codestyle.admin.template.model.resp.ChatSessionResp;
import top.codestyle.admin.template.model.resp.CodeSnippetResp;
import top.codestyle.admin.template.service.ChatSessionService;
import top.continew.starter.core.util.validation.CheckUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnClass(name = "org.springframework.ai.chat.client.ChatClient")
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final CodeSnippetMapper codeSnippetMapper;
    private final ChatClient.Builder chatClientBuilder;
    private final SearchService searchService;
    private final ResearchService researchService;

    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(\\w*)\\n([\\s\\S]*?)```");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatSessionResp createSession() {
        Long userId = UserContextHolder.getUserId();
        LocalDateTime now = LocalDateTime.now();

        ChatSessionDO session = new ChatSessionDO();
        session.setUserId(userId);
        session.setTitle("新对话");
        session.setPreview("");
        session.setLastTime(now);
        chatSessionMapper.insert(session);

        ChatMessageDO greeting = new ChatMessageDO();
        greeting.setSessionId(session.getId());
        greeting.setContent("你好！我是 AI 助手，已经分析了你的项目。有什么我可以帮助你的吗？");
        greeting.setRole("assistant");
        chatMessageMapper.insert(greeting);

        session.setPreview("你好！我是 AI 助手，已经分析了你的...");
        chatSessionMapper.updateById(session);

        return BeanUtil.copyProperties(session, ChatSessionResp.class);
    }

    @Override
    public List<ChatSessionResp> listSessions() {
        Long userId = UserContextHolder.getUserId();
        List<ChatSessionDO> sessions = chatSessionMapper.selectList(new LambdaQueryWrapper<ChatSessionDO>()
            .eq(ChatSessionDO::getUserId, userId)
            .orderByDesc(ChatSessionDO::getLastTime));
        return sessions.stream()
            .map(s -> BeanUtil.copyProperties(s, ChatSessionResp.class))
            .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageResp> getSessionMessages(Long sessionId) {
        checkSessionOwnership(sessionId);
        List<ChatMessageDO> messages = chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessageDO>()
            .eq(ChatMessageDO::getSessionId, sessionId)
            .orderByAsc(ChatMessageDO::getCreateTime));
        return messages.stream()
            .map(m -> BeanUtil.copyProperties(m, ChatMessageResp.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long sessionId) {
        checkSessionOwnership(sessionId);
        chatSessionMapper.deleteById(sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatResponseResp chat(ChatMessageReq req) {
        checkSessionOwnership(req.getSessionId());

        ChatMessageDO userMsg = new ChatMessageDO();
        userMsg.setSessionId(req.getSessionId());
        userMsg.setContent(req.getMessage());
        userMsg.setRole("user");
        chatMessageMapper.insert(userMsg);

        if (Boolean.TRUE.equals(req.getDeepResearch())) {
            return handleDeepResearch(req, userMsg);
        }

        return handleNormalChat(req, userMsg);
    }

    private ChatResponseResp handleNormalChat(ChatMessageReq req, ChatMessageDO userMsg) {
        String aiReplyText;
        try {
            List<ChatMessageDO> history = chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessageDO>()
                .eq(ChatMessageDO::getSessionId, req.getSessionId())
                .orderByAsc(ChatMessageDO::getCreateTime));

            StringBuilder systemPrompt = new StringBuilder();
            systemPrompt.append("你是 CodeStyle 平台的代码助手，专注于代码模板生成、技术问答和代码优化。\n");
            systemPrompt.append("请基于用户问题和检索到的相关模板上下文，提供专业、准确的回答。\n\n");

            String ragContext = retrieveSearchContext(req.getMessage());
            if (StrUtil.isNotBlank(ragContext)) {
                systemPrompt.append("以下是与用户问题相关的模板参考资料：\n");
                systemPrompt.append(ragContext).append("\n\n");
            }

            int startIdx = Math.max(0, history.size() - 20);
            for (int i = startIdx; i < history.size(); i++) {
                ChatMessageDO msg = history.get(i);
                systemPrompt.append("user".equals(msg.getRole()) ? "User: " : "Assistant: ");
                systemPrompt.append(msg.getContent()).append("\n");
            }

            ChatClient chatClient = chatClientBuilder.build();
            aiReplyText = chatClient.prompt().system(systemPrompt.toString()).user(req.getMessage()).call().content();

        } catch (Exception e) {
            log.error("AI调用失败", e);
            aiReplyText = "抱歉，AI服务暂时不可用，请稍后重试。";
        }

        ChatMessageDO aiMsg = new ChatMessageDO();
        aiMsg.setSessionId(req.getSessionId());
        aiMsg.setContent(aiReplyText);
        aiMsg.setRole("assistant");
        chatMessageMapper.insert(aiMsg);

        List<CodeSnippetResp> snippets = extractAndSaveCodeSnippets(req.getSessionId(), aiMsg.getId(), aiReplyText, req
            .getMessage());

        updateSessionMeta(req.getSessionId(), req.getMessage(), aiReplyText);

        ChatResponseResp response = new ChatResponseResp();
        response.setReply(BeanUtil.copyProperties(aiMsg, ChatMessageResp.class));
        response.setCodeSnippets(snippets);
        return response;
    }

    private ChatResponseResp handleDeepResearch(ChatMessageReq req, ChatMessageDO userMsg) {
        String aiReplyText;
        try {
            ResearchStartReq researchReq = new ResearchStartReq();
            researchReq.setSourceContent(req.getMessage());
            researchReq.setTemplateName("对话研究 - " + req.getMessage()
                .substring(0, Math.min(20, req.getMessage().length())));
            researchReq.setAutoConfirm(true);

            ResearchStatusResp status = researchService.startResearch(researchReq);

            ChatSessionDO session = chatSessionMapper.selectById(req.getSessionId());
            if (session != null) {
                session.setResearchTaskId(status.getTaskId());
                chatSessionMapper.updateById(session);
            }

            aiReplyText = String.format("已启动深度研究任务 (ID: %s)，正在分析中...\n当前状态: %s\n\n请使用研究进度查询接口跟踪进展。", status
                .getTaskId(), status.getStatus());

        } catch (UnsupportedOperationException e) {
            aiReplyText = "深度研究功能正在开发中，暂时使用普通对话模式回答。";
            return handleNormalChat(req, userMsg);
        } catch (Exception e) {
            log.error("深度研究启动失败", e);
            aiReplyText = "深度研究启动失败: " + e.getMessage();
        }

        ChatMessageDO aiMsg = new ChatMessageDO();
        aiMsg.setSessionId(req.getSessionId());
        aiMsg.setContent(aiReplyText);
        aiMsg.setRole("assistant");
        chatMessageMapper.insert(aiMsg);

        updateSessionMeta(req.getSessionId(), req.getMessage(), aiReplyText);

        ChatResponseResp response = new ChatResponseResp();
        response.setReply(BeanUtil.copyProperties(aiMsg, ChatMessageResp.class));
        response.setCodeSnippets(List.of());
        return response;
    }

    private String retrieveSearchContext(String query) {
        try {
            SearchRequest searchReq = new SearchRequest();
            searchReq.setQuery(query);
            searchReq.setTopK(3);
            searchReq.setEnableRerank(true);

            List<SearchResult> results = searchService.search(searchReq);
            if (results == null || results.isEmpty()) {
                return null;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < results.size(); i++) {
                SearchResult r = results.get(i);
                sb.append(String.format("[%d] %s (score: %.2f)\n", i + 1, r.getTitle(), r.getScore()));
                if (StrUtil.isNotBlank(r.getSnippet())) {
                    sb.append(r.getSnippet()).append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            log.debug("搜索检索失败，跳过 RAG 增强", e);
            return null;
        }
    }

    @Override
    public List<CodeSnippetResp> getSessionSnippets(Long sessionId) {
        checkSessionOwnership(sessionId);
        List<CodeSnippetDO> snippets = codeSnippetMapper.selectList(new LambdaQueryWrapper<CodeSnippetDO>()
            .eq(CodeSnippetDO::getSessionId, sessionId)
            .orderByAsc(CodeSnippetDO::getCreateTime));
        return snippets.stream()
            .map(s -> BeanUtil.copyProperties(s, CodeSnippetResp.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CodeSnippetResp updateSnippet(Long snippetId, String code) {
        CodeSnippetDO snippet = codeSnippetMapper.selectById(snippetId);
        CheckUtils.throwIfNull(snippet, "代码片段不存在: {}", snippetId);
        snippet.setCode(code);
        snippet.setLanguage(code.contains("<template>") ? "vue" : snippet.getLanguage());
        codeSnippetMapper.updateById(snippet);
        return BeanUtil.copyProperties(snippet, CodeSnippetResp.class);
    }

    private void checkSessionOwnership(Long sessionId) {
        Long userId = UserContextHolder.getUserId();
        ChatSessionDO session = chatSessionMapper.selectById(sessionId);
        CheckUtils.throwIf(session == null || !session.getUserId().equals(userId), "会话不存在或无权限访问");
    }

    private List<CodeSnippetResp> extractAndSaveCodeSnippets(Long sessionId,
                                                             Long messageId,
                                                             String aiReply,
                                                             String context) {
        List<CodeSnippetResp> snippets = new ArrayList<>();
        Matcher matcher = CODE_BLOCK_PATTERN.matcher(aiReply);

        while (matcher.find()) {
            String lang = matcher.group(1);
            String code = matcher.group(2).trim();
            if (code.isEmpty())
                continue;

            String language;
            if (code.contains("<template>")) {
                language = "vue";
            } else if (StrUtil.isNotBlank(lang)) {
                language = lang.toLowerCase();
            } else {
                language = "javascript";
            }

            CodeSnippetDO snippet = new CodeSnippetDO();
            snippet.setSessionId(sessionId);
            snippet.setMessageId(messageId);
            snippet.setCode(code);
            snippet.setLanguage(language);
            snippet.setContext(context.length() > 500 ? context.substring(0, 500) : context);
            codeSnippetMapper.insert(snippet);

            snippets.add(BeanUtil.copyProperties(snippet, CodeSnippetResp.class));
        }

        return snippets;
    }

    private void updateSessionMeta(Long sessionId, String userMessage, String aiReply) {
        ChatSessionDO session = chatSessionMapper.selectById(sessionId);
        if (session == null)
            return;

        if ("新对话".equals(session.getTitle())) {
            String title = userMessage.length() > 20 ? userMessage.substring(0, 20) + "..." : userMessage;
            session.setTitle(title);
        }

        String plain = aiReply.replaceAll("<[^>]*>", "").replaceAll("```[\\s\\S]*?```", "[代码]");
        session.setPreview(plain.length() > 30 ? plain.substring(0, 30) + "..." : plain);
        session.setLastTime(LocalDateTime.now());
        chatSessionMapper.updateById(session);
    }
}
