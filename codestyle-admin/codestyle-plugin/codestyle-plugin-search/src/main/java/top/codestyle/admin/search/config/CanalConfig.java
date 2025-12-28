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

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

/**
 * Canal配置类
 * 负责Canal连接池的创建和管理
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(CanalConfigProperties.class)
@ConditionalOnClass(name = "com.alibaba.otter.canal.client.CanalConnector")
@ConditionalOnProperty(name = "canal.enabled", havingValue = "true", matchIfMissing = false)
public class CanalConfig {

    @Resource
    private CanalConfigProperties canalConfigProperties;

    /**
     * 创建Canal连接器
     * 
     * @return CanalConnector实例
     */
    @Bean(destroyMethod = "disconnect")
    public CanalConnector canalConnector() {
        if (!canalConfigProperties.isEnabled()) {
            log.info("Canal已禁用，跳过初始化");
            return null;
        }

        log.info("初始化Canal连接器，地址: {}:{}, 实例: {}", canalConfigProperties.getHostname(), canalConfigProperties
            .getPort(), canalConfigProperties.getDestination());

        // 创建连接器
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(canalConfigProperties
            .getHostname(), canalConfigProperties.getPort()), canalConfigProperties
                .getDestination(), canalConfigProperties.getUsername(), canalConfigProperties.getPassword());

        try {
            // 连接
            connector.connect();
            log.info("Canal连接器连接成功");

            // 构建订阅表达式
            String subscribeExpr = canalConfigProperties.getSubscribe();
            // 订阅表
            connector.subscribe(subscribeExpr);
            log.info("Canal连接器订阅表成功: {}", subscribeExpr);

            // 回滚到上一条确认的位置
            connector.rollback();
            log.info("Canal连接器初始化完成");
        } catch (Exception e) {
            log.error("Canal连接器初始化失败: {}", e.getMessage(), e);
            throw e;
        }

        return connector;
    }
}