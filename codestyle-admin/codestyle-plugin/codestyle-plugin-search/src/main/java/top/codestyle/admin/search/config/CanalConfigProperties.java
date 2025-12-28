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

import java.util.List;

/**
 * 
 * Canal配置属性类
 * 封装Canal的所有配置项
 * 
 * @author ChonghaoGao
 * @date 2025/12/22
 */
@Data
@Component
@ConfigurationProperties(prefix = "canal")
public class CanalConfigProperties {

    /**
     * 是否启用Canal
     */
    private boolean enabled = true;

    /**
     * Canal服务器主机地址
     */
    private String hostname = "localhost";

    /**
     * Canal服务器端口
     */
    private int port = 11111;

    /**
     * 用户名
     */
    private String username = "canal";

    /**
     * 密码
     */
    private String password = "canal";

    /**
     * 实例名称
     */
    private String destination = "example";

    /**
     * 订阅表达式
     */
    private String subscribe = "codestyle.*";

    /**
     * 批处理大小
     */
    private int batchSize = 1000;

    /**
     * 超时时间(毫秒)
     */
    private long timeout = 1000;

    /**
     * 需要同步的数据库+表配置，格式为dbName.tableName，多个配置用逗号分隔
     */
    private List<String> subscribeTables;

    /**
     * 数据库配置
     */
    private Db db = new Db();

    /**
     * 数据库配置内部类
     */
    @Data
    public static class Db {
        /**
         * 数据库用户名
         */
        private String username = "root";

        /**
         * 数据库密码
         */
        private String password = "root";
    }
}