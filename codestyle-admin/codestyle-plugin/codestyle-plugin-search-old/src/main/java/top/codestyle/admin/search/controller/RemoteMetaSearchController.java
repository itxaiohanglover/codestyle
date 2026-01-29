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

package top.codestyle.admin.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.codestyle.admin.search.service.RemoteMetaSearchService;
import top.codestyle.admin.search.vo.RemoteMetaConfigVO;

import java.util.Optional;

/**
 * 搜索控制器
 */
@Tag(name = "应用管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mcp")
public class RemoteMetaSearchController {

    private final RemoteMetaSearchService remoteMetaSearchService;

    @Operation(summary = "根据关键词、语义远程检索模板", description = "检索到的模板json信息，否则为未匹配到关键词响应")
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String query) {
        Optional<RemoteMetaConfigVO> searchMetaVO = remoteMetaSearchService.search(query);
        if (searchMetaVO.isPresent()) {
            return ResponseEntity.ok(searchMetaVO.get());
        } else {
            return ResponseEntity.status(404).body("未找到匹配关键词: \"" + query + "\" 的模板");
        }
    }
}