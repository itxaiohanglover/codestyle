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

package top.codestyle.admin.search.runner;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import top.codestyle.admin.search.service.SyncService;

/**
 * 
 * 全量同步执行器在应用启动时自动执行全量同步
 * 
 * @author ChonghaoGao
 * @date 2025/12/22
 */
@Slf4j
@Component
@AllArgsConstructor
public class FullSyncRunner implements ApplicationRunner {

    private final SyncService syncService;

    /**
     * 应用启动时执行全量同步
     *
     * @param args 应用启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("应用启动，开始执行全量同步");
            int count = syncService.fullSync();
            log.info("全量同步完成，成功同步 {} 条数据到ES", count);
        } catch (Exception e) {
            log.error("全量同步失败: {}", e.getMessage(), e);
        }
    }
}
