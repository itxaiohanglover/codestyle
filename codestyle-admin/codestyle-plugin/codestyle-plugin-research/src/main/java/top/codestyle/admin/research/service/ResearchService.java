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

package top.codestyle.admin.research.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.codestyle.admin.research.model.req.ResearchFeedbackReq;
import top.codestyle.admin.research.model.req.ResearchStartReq;
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
    ResearchStatusResp startResearch(ResearchStartReq request);

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
     * @param taskId  任务 ID
     * @param request 反馈请求
     */
    void submitFeedback(String taskId, ResearchFeedbackReq request);

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
