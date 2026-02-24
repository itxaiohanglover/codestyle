/*
 * Copyright (c) 2022-present CodeStyle (codestyle.top)
 *
 * This project is licensed under the Apache License 2.0.
 */

package top.codestyle.admin.research.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.codestyle.admin.research.model.req.ResearchFeedbackRequest;
import top.codestyle.admin.research.model.req.ResearchStartRequest;
import top.codestyle.admin.research.model.resp.ResearchStatusResp;

/**
 * 深度研究服务接口
 *
 * @author CodeStyle
 * @since 2026-02-22
 */
public interface ResearchService {

    /**
     * 启动研究任务
     *
     * @param request 启动请求
     * @return 任务状态
     */
    ResearchStatusResp startResearch(ResearchStartRequest request);

    /**
     * 订阅任务进度（SSE 流式返回）
     *
     * @param taskId 任务 ID
     * @return SSE Emitter
     */
    SseEmitter subscribeProgress(String taskId);

    /**
     * 提交用户反馈
     *
     * @param taskId 任务 ID
     * @param request 反馈请求
     */
    void submitFeedback(String taskId, ResearchFeedbackRequest request);

    /**
     * 确认继续执行
     *
     * @param taskId 任务 ID
     */
    void confirmContinue(String taskId);

    /**
     * 取消任务
     *
     * @param taskId 任务 ID
     */
    void cancelTask(String taskId);
}

