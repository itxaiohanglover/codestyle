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

package top.codestyle.admin.search.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.codestyle.admin.search.model.resp.TemplateUploadResp;
import top.codestyle.admin.search.service.TemplateFileService;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.log.annotation.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 模板文件 API
 * <p>
 * 提供模板压缩包上传解压、文件树查询、文件内容预览及打包下载功能。
 * 所有模板文件通过系统 FileService 统一存储管理。
 * </p>
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "模板文件 API")
@Validated
@RestController
@RequiredArgsConstructor
public class TemplateFileController {

    private final TemplateFileService templateFileService;

    /**
     * 上传模板
     * <p>
     * 接收 ZIP/TAR.GZ/7Z 格式压缩包，自动解压并将模板文件逐个存入文件管理系统。
     * 支持从 meta.json 自动解析 groupId/artifactId/version 参数。
     * </p>
     */
    @Log(ignore = true)
    @Operation(summary = "上传模板", description = "上传模板压缩包，自动解压并存入文件管理系统")
    @Parameter(name = "file", description = "模板压缩包（ZIP/TAR.GZ/7Z）", required = true)
    @Parameter(name = "groupId", description = "组ID（可选，优先于 meta.json）", example = "top.codestyle", in = ParameterIn.QUERY)
    @Parameter(name = "artifactId", description = "项目ID（可选，优先于 meta.json）", example = "crud-template", in = ParameterIn.QUERY)
    @Parameter(name = "version", description = "版本号（可选，优先于 meta.json）", example = "1.0.0", in = ParameterIn.QUERY)
    @PostMapping("/open-api/template/upload")
    public TemplateUploadResp upload(@RequestPart MultipartFile file,
                                     @RequestParam(required = false) String groupId,
                                     @RequestParam(required = false) String artifactId,
                                     @RequestParam(required = false) String version,
                                     @RequestParam(required = false, defaultValue = "false") Boolean overwrite) throws IOException {
        // 参数校验
        ValidationUtils.throwIf(file::isEmpty, "文件不能为空");
        String filename = file.getOriginalFilename();
        CheckUtils.throwIf(filename == null, "文件名不能为空");
        String lowerName = filename.toLowerCase();
        CheckUtils.throwIf(!(lowerName.endsWith(".zip") || lowerName.endsWith(".tar.gz") || lowerName
            .endsWith(".tgz") || lowerName.endsWith(".7z")), "只支持 ZIP、TAR.GZ、7Z 格式的压缩包");

        return templateFileService.uploadTemplate(file, groupId, artifactId, version, overwrite);
    }

    /**
     * 获取模板文件树
     * <p>
     * 从文件管理系统中查询指定模板版本下的所有文件，返回树形结构。
     * </p>
     */
    @Log(ignore = true)
    @Operation(summary = "获取模板文件树", description = "返回模板目录下的文件树结构")
    @Parameter(name = "groupId", description = "组ID", example = "top.codestyle", in = ParameterIn.QUERY)
    @Parameter(name = "artifactId", description = "项目ID", example = "crud-template", in = ParameterIn.QUERY)
    @Parameter(name = "version", description = "版本号", example = "1.0.0", in = ParameterIn.QUERY)
    @GetMapping("/open-api/template/files")
    public List<Map<String, Object>> listFiles(@RequestParam String groupId,
                                               @RequestParam String artifactId,
                                               @RequestParam String version) {
        return templateFileService.listFiles(groupId, artifactId, version);
    }

    /**
     * 预览模板文件内容
     * <p>
     * 从存储系统中读取指定模板文件的文本内容，文件大小不超过 512KB。
     * </p>
     */
    @Log(ignore = true)
    @Operation(summary = "预览模板文件内容", description = "读取模板目录下指定文件的文本内容")
    @Parameter(name = "groupId", description = "组ID", example = "top.codestyle", in = ParameterIn.QUERY)
    @Parameter(name = "artifactId", description = "项目ID", example = "crud-template", in = ParameterIn.QUERY)
    @Parameter(name = "version", description = "版本号", example = "1.0.0", in = ParameterIn.QUERY)
    @Parameter(name = "filePath", description = "模板内相对路径", example = "src/main/java/Entity.java.ftl", in = ParameterIn.QUERY)
    @GetMapping("/open-api/template/file-content")
    public String readFileContent(@RequestParam String groupId,
                                  @RequestParam String artifactId,
                                  @RequestParam String version,
                                  @RequestParam String filePath) throws IOException {
        return templateFileService.readFileContent(groupId, artifactId, version, filePath);
    }

    /**
     * 下载模板
     * <p>
     * 从存储系统中读取指定模板版本的所有文件，按原始目录结构打包为 ZIP 并返回。
     * </p>
     */
    @Log(ignore = true)
    @Operation(summary = "下载模板", description = "将模板文件打包为 ZIP 下载")
    @Parameter(name = "groupId", description = "组ID", example = "top.codestyle", in = ParameterIn.QUERY)
    @Parameter(name = "artifactId", description = "项目ID", example = "crud-template", in = ParameterIn.QUERY)
    @Parameter(name = "version", description = "版本号", example = "1.0.0", in = ParameterIn.QUERY)
    @GetMapping("/open-api/template/download")
    public void download(@RequestParam String groupId,
                         @RequestParam String artifactId,
                         @RequestParam String version,
                         HttpServletResponse response) throws IOException {
        File zipFile = null;
        try {
            // 从存储系统中打包模板文件为 ZIP
            zipFile = templateFileService.createTemplateZip(groupId, artifactId, version);

            // 设置响应头
            String fileName = String.format("%s-%s-%s.zip", groupId, artifactId, version);
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setContentLengthLong(zipFile.length());

            // 写入响应流
            try (FileInputStream fis = new FileInputStream(zipFile); OutputStream os = response.getOutputStream()) {
                IoUtil.copy(fis, os);
                os.flush();
            }
        } finally {
            // 清理临时 ZIP 文件
            if (zipFile != null && zipFile.exists()) {
                FileUtil.del(zipFile);
            }
        }
    }

    @Log(ignore = true)
    @Operation(summary = "删除模板", description = "删除指定版本的模板文件")
    @PostMapping("/open-api/template/delete")
    public void delete(@RequestParam String groupId, @RequestParam String artifactId, @RequestParam String version) {
        templateFileService.deleteTemplateFiles(groupId, artifactId, version);
    }
}
