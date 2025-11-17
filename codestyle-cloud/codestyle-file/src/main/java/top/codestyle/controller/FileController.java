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

package top.codestyle.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.codestyle.base.controller.BaseController;
import top.codestyle.model.dto.file.FileReq;
import top.codestyle.model.query.FileQuery;
import top.codestyle.model.vo.file.FileDirCalcSizeResp;
import top.codestyle.model.vo.file.FileResp;
import top.codestyle.model.vo.file.FileStatisticsResp;
import top.codestyle.model.vo.file.FileUploadResp;
import top.codestyle.service.FileService;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;
import top.continew.starter.extension.crud.model.resp.IdResp;
import top.continew.starter.log.annotation.Log;

import java.io.IOException;

/**
 * 文件管理 API
 *
 * @author Charles7c
 * @since 2023/12/23 10:38
 */
@Tag(name = "文件管理 API")
@Validated
@RestController
@RequiredArgsConstructor
@CrudRequestMapping(value = "/file", api = {Api.PAGE, Api.UPDATE, Api.BATCH_DELETE})
public class FileController extends BaseController<FileService, FileResp, FileResp, FileQuery, FileReq> {

    /**
     * 上传文件
     * <p>
     * 公共上传文件请使用 {@link CommonController#upload}
     * </p>
     *
     * @param file       文件
     * @param parentPath 上级目录
     * @return 文件上传响应参数
     * @throws IOException /
     */
    @Operation(summary = "上传文件", description = "上传文件")
    @Parameter(name = "parentPath", description = "上级目录（默认：/yyyy/MM/dd）", example = "/", in = ParameterIn.QUERY)
    @SaCheckPermission("system:file:upload")
    @PostMapping("/upload")
    public FileUploadResp upload(@NotNull(message = "文件不能为空") @RequestPart MultipartFile file,
                                 @RequestParam(required = false) String parentPath) throws IOException {
        ValidationUtils.throwIf(file::isEmpty, "文件不能为空");
        FileInfo fileInfo = baseService.upload(file, parentPath);
        return FileUploadResp.builder()
            .id(fileInfo.getId())
            .url(fileInfo.getUrl())
            .thUrl(fileInfo.getThUrl())
            .metadata(fileInfo.getMetadata())
            .build();
    }

    @Operation(summary = "创建文件夹", description = "创建文件夹")
    @SaCheckPermission("system:file:createDir")
    @PostMapping("/dir")
    public IdResp<Long> createDir(@RequestBody @Valid FileReq req) {
        ValidationUtils.throwIfBlank(req.getParentPath(), "上级目录不能为空");
        return new IdResp<>(baseService.createDir(req));
    }

    @Operation(summary = "计算文件夹大小", description = "计算文件夹大小")
    @SaCheckPermission("system:file:calcDirSize")
    @GetMapping("/dir/{id}/size")
    public FileDirCalcSizeResp calcDirSize(@PathVariable Long id) {
        return new FileDirCalcSizeResp(baseService.calcDirSize(id));
    }

    @Log(ignore = true)
    @Operation(summary = "查询文件资源统计", description = "查询文件资源统计")
    @SaCheckPermission("system:file:list")
    @GetMapping("/statistics")
    public FileStatisticsResp statistics() {
        return baseService.statistics();
    }

    @Log(ignore = true)
    @Operation(summary = "检测文件是否存在", description = "检测文件是否存在")
    @SaCheckPermission("system:file:check")
    @GetMapping("/check")
    public FileResp checkFile(String fileHash) {
        return baseService.check(fileHash);
    }

    @Operation(summary = "下载本地文件并打包", description = "下载本地文件并打包")
    @SaCheckPermission("file:load")
    @PostMapping("/load")
    public  byte[] load(FileQuery query, HttpServletResponse response) {;
        if (query.getPaths() == null){
            return null;
        }
        byte[] zipdata = baseService.load(query);
        // 设置响应头
        response.setHeader("attachment", "files.zip");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return zipdata;

    }
}