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

package top.codestyle.admin.schedule.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.codestyle.admin.schedule.api.JobBatchApi;
import top.codestyle.admin.schedule.api.JobClient;
import top.codestyle.admin.schedule.model.query.JobLogQuery;
import top.codestyle.admin.schedule.model.resp.JobLogResp;
import top.codestyle.admin.schedule.service.JobLogService;
import top.continew.starter.extension.crud.model.resp.PageResp;

/**
 * 任务日志业务实现
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/6/27 22:54
 */
@Service
@RequiredArgsConstructor
public class JobLogServiceImpl implements JobLogService {

    private final JobClient jobClient;
    private final JobBatchApi jobBatchApi;

    @Override
    public PageResp<JobLogResp> page(JobLogQuery query) {
        return jobClient.requestPage(() -> jobBatchApi.page(query));
    }

    @Override
    public boolean stop(Long id) {
        return Boolean.TRUE.equals(jobClient.request(() -> jobBatchApi.stop(id)));
    }

    @Override
    public boolean retry(Long id) {
        return Boolean.TRUE.equals(jobClient.request(() -> jobBatchApi.retry(id)));
    }
}
