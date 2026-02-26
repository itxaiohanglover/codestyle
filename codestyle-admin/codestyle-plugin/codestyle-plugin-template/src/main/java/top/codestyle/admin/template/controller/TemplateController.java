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

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.codestyle.admin.template.model.query.TemplateQuery;
import top.codestyle.admin.template.model.req.TemplateReq;
import top.codestyle.admin.template.model.resp.TemplateDetailResp;
import top.codestyle.admin.template.model.resp.TemplateItemResp;
import top.codestyle.admin.template.service.TemplateService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.extension.tenant.annotation.TenantIgnore;

import java.util.List;

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
    @SaCheckPermission("template:library:list")
    @GetMapping("/list")
    public PageResp<TemplateItemResp> list(TemplateQuery query, PageQuery pageQuery) {
        return templateService.listTemplates(query, pageQuery);
    }

    @Operation(summary = "我的收藏列表")
    @SaCheckPermission("template:library:list")
    @GetMapping("/favorites")
    public PageResp<TemplateItemResp> favorites(TemplateQuery query, PageQuery pageQuery) {
        return templateService.listFavorites(query, pageQuery);
    }

    @Operation(summary = "获取模板详情")
    @SaCheckPermission("template:library:get")
    @GetMapping("/{id}")
    public TemplateDetailResp detail(@Parameter(description = "模板ID") @PathVariable Long id) {
        return templateService.getTemplateDetail(id);
    }

    @Operation(summary = "切换收藏状态")
    @SaCheckPermission("template:library:favorite")
    @PutMapping("/{id}/favorite")
    public Boolean toggleFavorite(@Parameter(description = "模板ID") @PathVariable Long id) {
        return templateService.toggleFavorite(id);
    }

    @Operation(summary = "记录下载 (实际下载由 search 模块的 /open-api/template/download 提供)")
    @SaCheckPermission("template:library:download")
    @PostMapping("/{id}/download")
    public void recordDownload(@Parameter(description = "模板ID") @PathVariable Long id) {
        templateService.incrementDownloadCount(id);
    }

    @Operation(summary = "新增模板")
    @SaCheckPermission("template:library:create")
    @PostMapping
    public Long create(@Valid @RequestBody TemplateReq req) {
        return templateService.create(req);
    }

    @Operation(summary = "修改模板")
    @SaCheckPermission("template:library:update")
    @PutMapping("/{id}")
    public void update(@Parameter(description = "模板ID") @PathVariable Long id, @Valid @RequestBody TemplateReq req) {
        templateService.update(id, req);
    }

    @Operation(summary = "删除模板")
    @SaCheckPermission("template:library:delete")
    @DeleteMapping
    public void delete(@NotEmpty(message = "ID列表不能为空") @RequestBody List<Long> ids) {
        templateService.delete(ids);
    }

    @Operation(summary = "查询模板版本列表")
    @SaCheckPermission("template:library:list")
    @GetMapping("/versions")
    public List<TemplateItemResp> versions(@NotBlank(message = "groupId不能为空") @RequestParam String groupId,
                                           @NotBlank(message = "artifactId不能为空") @RequestParam String artifactId) {
        return templateService.listVersions(groupId, artifactId);
    }
}
