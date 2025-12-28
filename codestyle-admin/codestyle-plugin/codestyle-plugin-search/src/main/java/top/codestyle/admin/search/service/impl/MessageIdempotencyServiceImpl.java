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

package top.codestyle.admin.search.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import top.codestyle.admin.search.model.dto.DataChangeMessage;
import top.codestyle.admin.search.service.MessageIdempotencyService;

import java.time.Duration;

/**
 * 消息幂等性服务实现类
 * 使用Redis记录已处理的消息，确保消息不会被重复处理
 *
 * @author chonghaoGao
 * @date 2025/12/23
 */
@Slf4j
@Service("messageIdempotencyService")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class MessageIdempotencyServiceImpl implements MessageIdempotencyService {

    private static final String PROCESSED_KEY_PREFIX = "msg:processed:";
    private static final int DEFAULT_TTL_SECONDS = 86400; // 24小时

    private final StringRedisTemplate redisTemplate;

    /**
     * 检查消息是否已处理
     * 
     * @param messageId 消息ID
     * @return true表示已处理，false表示未处理
     */
    @Override
    public boolean isProcessed(String messageId) {
        if (messageId == null || messageId.isEmpty()) {
            return false;
        }

        try {
            String key = PROCESSED_KEY_PREFIX + messageId;
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("检查消息是否已处理失败: messageId={}", messageId, e);
            // Redis异常时返回false，允许处理（避免Redis故障导致消息无法处理）
            return false;
        }
    }

    /**
     * 标记消息为已处理
     * 
     * @param messageId 消息ID
     */
    @Override
    public void markAsProcessed(String messageId) {
        if (messageId == null || messageId.isEmpty()) {
            return;
        }

        try {
            String key = PROCESSED_KEY_PREFIX + messageId;
            redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(DEFAULT_TTL_SECONDS));
            log.debug("标记消息为已处理: messageId={}", messageId);
        } catch (Exception e) {
            log.error("标记消息为已处理失败: messageId={}", messageId, e);
            // Redis异常时只记录日志，不抛出异常（避免影响主流程）
        }
    }

    /**
     * 检查消息是否已处理（使用DataChangeMessage对象）
     * 
     * @param message 数据变更消息
     * @return true表示已处理，false表示未处理
     */
    @Override
    public boolean isProcessed(DataChangeMessage message) {
        if (message == null) {
            return false;
        }
        return isProcessed(message.getMessageId());
    }

    /**
     * 标记消息为已处理（使用DataChangeMessage对象）
     * 
     * @param message 数据变更消息
     */
    @Override
    public void markAsProcessed(DataChangeMessage message) {
        if (message == null) {
            return;
        }
        markAsProcessed(message.getMessageId());
    }
}
