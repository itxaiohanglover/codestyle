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

package top.codestyle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {
    // 排除租户相关自动配置
    top.continew.starter.extension.tenant.autoconfigure.TenantAutoConfiguration.class,
    top.continew.starter.extension.tenant.autoconfigure.TenantWebMvcAutoConfiguration.class,
    // 排除数据源相关自动配置
    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
    org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class,
    org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration.class
})
@EnableAsync
public class CodestyleSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodestyleSearchApplication.class, args);
    }

}
