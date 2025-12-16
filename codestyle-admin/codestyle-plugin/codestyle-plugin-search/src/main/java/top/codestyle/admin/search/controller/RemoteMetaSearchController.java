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

package top.codestyle.admin.search.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.codestyle.admin.search.services.RemoteMetaSearchService;
import top.codestyle.admin.search.vo.RemoteMetaConfigVO;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;
import top.continew.starter.extension.tenant.annotation.TenantIgnore;

import java.util.Optional;

/**
 * @author ChonghaoGao
 * @date 2025/12/1 19:21)
 */
@Slf4j
@Tag(name = "应用管理 API")
@RestController
@RequiredArgsConstructor
@CrudRequestMapping(value = "/api/mcp", api = {Api.PAGE, Api.GET, Api.CREATE, Api.UPDATE, Api.BATCH_DELETE,
        Api.EXPORT})
public class RemoteMetaSearchController {

    private final RemoteMetaSearchService remoteMetaSearchService;

    @SaIgnore
    @TenantIgnore
    @Operation(summary = "根据关键词、语义远程检索模板", description = "检索到的模板json信息,否则为:未匹配到关键词响应")
    @GetMapping("/search")
    public ResponseEntity<?> metaSearch(@RequestParam String query) {

        Optional<RemoteMetaConfigVO> searchMetaVO = remoteMetaSearchService.search(query);
        log.info("query:{} result:{}", query, searchMetaVO);
        if (searchMetaVO.isPresent()) {
            return ResponseEntity.ok(searchMetaVO.get());
        } else {
            // 查询失败时返回带有错误信息的响应

            return ResponseEntity.status(404).body("未找到匹配关键词: \"" + query + "\" 的模板");
        }
    }
}
