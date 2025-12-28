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

import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    // Spring Boot 3.x 自动配置
    // 不需要手动配置 RedisTemplate
    // 配置可以通过 application.yml 中的 spring.redis.* 属性进行设置

    // 示例配置：
    // spring:
    //   redis:
    //     host: localhost
    //     port: 6379
    //     password:
    //     database: 0
}