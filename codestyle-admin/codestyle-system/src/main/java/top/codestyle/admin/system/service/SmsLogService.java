/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this fileInfo except in compliance with the License.
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

package top.codestyle.admin.system.service;

import top.codestyle.admin.common.base.service.BaseService;
import top.codestyle.admin.system.model.query.SmsLogQuery;
import top.codestyle.admin.system.model.req.SmsLogReq;
import top.codestyle.admin.system.model.resp.SmsLogResp;

/**
 * 短信日志业务接口
 *
 * @author luoqiz
 * @since 2025/03/15 22:15
 */
public interface SmsLogService extends BaseService<SmsLogResp, SmsLogResp, SmsLogQuery, SmsLogReq> {}