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

package top.codestyle.admin.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 批量处理器配置属性
 * 
 * @author AI Assistant
 * @date 2025/12/23
 */
@Component
@Data
@ConfigurationProperties(prefix = "codestyle.search.bulk")
public class BulkProcessorProperties {

    /**
     * 批量操作数量阈值（每达到此数量执行一次批量处理）
     */
    private int bulkActions = 1000;

    /**
     * 批量刷新间隔（毫秒）
     */
    private long flushIntervalMs = 5000;

    /**
     * 并发请求数
     */
    private int concurrentRequests = 1;
}
