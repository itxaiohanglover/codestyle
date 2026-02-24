/*
 * Copyright (c) 2022-present CodeStyle Authors. All Rights Reserved.
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

package top.codestyle.admin.template.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.codestyle.admin.template.model.query.TemplateQuery;
import top.codestyle.admin.template.model.resp.TemplateDetailResp;
import top.codestyle.admin.template.model.resp.TemplateItemResp;
import top.codestyle.admin.template.service.TemplateService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.extension.tenant.annotation.TenantIgnore;

@Slf4j
@Tag(name = "模板管理 API")
@Validated
@RestController
@RequiredArgsConstructor
@TenantIgnore
@RequestMapping("/template/library")
public class TemplateController {

    private final TemplateService templateService;

    @Operation(summary = "分页查询模板列表")
    @GetMapping("/list")
    public PageResp<TemplateItemResp> list(TemplateQuery query, PageQuery pageQuery) {
        return templateService.listTemplates(query, pageQuery);
    }

    @Operation(summary = "获取模板详情")
    @GetMapping("/{id}")
    public TemplateDetailResp detail(@Parameter(description = "模板ID") @PathVariable Long id) {
        return templateService.getTemplateDetail(id);
    }

    @Operation(summary = "切换收藏状态")
    @PutMapping("/{id}/favorite")
    public Boolean toggleFavorite(@Parameter(description = "模板ID") @PathVariable Long id) {
        return templateService.toggleFavorite(id);
    }

    @Operation(summary = "记录下载 (实际下载由 search 模块的 /open-api/template/download 提供)")
    @PostMapping("/{id}/download")
    public void recordDownload(@Parameter(description = "模板ID") @PathVariable Long id) {
        templateService.incrementDownloadCount(id);
    }
}
