/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
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

package top.codestyle.admin.search.service;

import top.codestyle.admin.search.model.dto.DataChangeMessage;

/**
 * 消息幂等性服务接口
 * 使用Redis记录已处理的消息，确保消息不会被重复处理
 *
 * @author chonghaoGao
 * @date 2025/12/23
 */
public interface MessageIdempotencyService {

    /**
     * 检查消息是否已处理
     * 
     * @param messageId 消息ID
     * @return true表示已处理，false表示未处理
     */
    boolean isProcessed(String messageId);

    /**
     * 标记消息为已处理
     * 
     * @param messageId 消息ID
     */
    void markAsProcessed(String messageId);

    /**
     * 检查消息是否已处理（使用DataChangeMessage对象）
     * 
     * @param message 数据变更消息
     * @return true表示已处理，false表示未处理
     */
    boolean isProcessed(DataChangeMessage message);

    /**
     * 标记消息为已处理（使用DataChangeMessage对象）
     * 
     * @param message 数据变更消息
     */
    void markAsProcessed(DataChangeMessage message);
}
