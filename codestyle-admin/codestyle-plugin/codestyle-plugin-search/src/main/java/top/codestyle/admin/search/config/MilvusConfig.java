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

package top.codestyle.admin.search.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus 配置
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.milvus", name = "enabled", havingValue = "true")
public class MilvusConfig {

    private final SearchProperties searchProperties;

    @Bean
    public MilvusServiceClient milvusClient() {
        SearchProperties.MilvusProperties milvus = searchProperties.getMilvus();

        log.info("初始化 Milvus 客户端，地址: {}:{}", milvus.getHost(), milvus.getPort());

        ConnectParam connectParam = ConnectParam.newBuilder()
            .withHost(milvus.getHost())
            .withPort(milvus.getPort())
            .build();

        return new MilvusServiceClient(connectParam);
    }
}
