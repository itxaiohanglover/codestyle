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

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ZipUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.codestyle.admin.search.model.resp.TemplateUploadResp;
import top.codestyle.admin.search.service.TemplateFileService;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.log.annotation.Log;
import top.continew.starter.web.model.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 模板文件 API
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

    /**
     * 模板存储根目录
     * 从配置文件读取，默认为 ./templates
     */
    @Value("${template.storage.path:./templates}")
    private String templateBasePath;

    private final TemplateFileService templateFileService;

    /**
     * 下载模板文件
     * <p>
     * 根据 groupId/artifactId/version 定位模板目录，打包为 ZIP 并返回
     * </p>
     *
     * @param groupId    组ID（如：top.codestyle）
     * @param artifactId 项目ID（如：crud-template）
     * @param version    版本号（如：1.0.0）
     * @param response   HTTP 响应
     * @throws IOException IO 异常
     */
    @Log(ignore = true)
    @SaIgnore
    @Operation(summary = "下载模板", description = "根据 groupId/artifactId/version 下载模板 ZIP 包")
    @Parameter(name = "groupId", description = "组ID", example = "top.codestyle", in = ParameterIn.QUERY)
    @Parameter(name = "artifactId", description = "项目ID", example = "crud-template", in = ParameterIn.QUERY)
    @Parameter(name = "version", description = "版本号", example = "1.0.0", in = ParameterIn.QUERY)
    @GetMapping("/open-api/template/download")
    public void download(
            @RequestParam String groupId,
            @RequestParam String artifactId,
            @RequestParam String version,
            HttpServletResponse response) throws IOException {

        log.info("开始下载模板: groupId={}, artifactId={}, version={}", groupId, artifactId, version);

        // 1. 构建模板目录路径
        Path templatePath = Paths.get(templateBasePath, groupId, artifactId, version);
        File templateDir = templatePath.toFile();

        // 2. 验证目录是否存在
        CheckUtils.throwIf(!templateDir.exists(), "模板不存在: {}/{}/{}", groupId, artifactId, version);
        CheckUtils.throwIf(!templateDir.isDirectory(), "路径不是目录: {}", templatePath);

        // 3. 创建临时 ZIP 文件
        File zipFile = null;
        try {
            zipFile = Files.createTempFile("template-", ".zip").toFile();
            log.info("创建临时 ZIP 文件: {}", zipFile.getAbsolutePath());

            // 4. 打包目录为 ZIP
            ZipUtil.zip(zipFile, false, templateDir);
            log.info("模板打包完成，ZIP 大小: {} bytes", zipFile.length());

            // 5. 设置响应头
            String fileName = String.format("%s-%s-%s.zip", groupId, artifactId, version);
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setContentLengthLong(zipFile.length());

            // 6. 写入响应流
            try (FileInputStream fis = new FileInputStream(zipFile);
                 OutputStream os = response.getOutputStream()) {
                IoUtil.copy(fis, os);
                os.flush();
            }

            log.info("模板下载完成: {}", fileName);

        } catch (Exception e) {
            log.error("模板下载失败: groupId={}, artifactId={}, version={}", groupId, artifactId, version, e);
            throw new IOException("模板下载失败: " + e.getMessage(), e);
        } finally {
            // 7. 清理临时文件
            if (zipFile != null && zipFile.exists()) {
                FileUtil.del(zipFile);
                log.debug("清理临时 ZIP 文件: {}", zipFile.getAbsolutePath());
            }
        }
    }

    /**
     * 上传模板
     * <p>
     * 接收 ZIP 文件，自动解压并上传到存储系统
     * </p>
     *
     * @param file       模板 ZIP 文件
     * @param groupId    组ID（如：top.codestyle）
     * @param artifactId 项目ID（如：crud-template）
     * @param version    版本号（如：1.0.0）
     * @return 模板上传响应
     * @throws IOException IO 异常
     */
    @Log(ignore = true)
    @SaIgnore
    @Operation(summary = "上传模板", description = "上传模板 ZIP 包，自动解压并索引")
    @Parameter(name = "file", description = "模板 ZIP 文件", required = true)
    @Parameter(name = "groupId", description = "组ID", example = "top.codestyle", in = ParameterIn.QUERY)
    @Parameter(name = "artifactId", description = "项目ID", example = "crud-template", in = ParameterIn.QUERY)
    @Parameter(name = "version", description = "版本号", example = "1.0.0", in = ParameterIn.QUERY)
    @PostMapping("/open-api/template/upload")
    public R<TemplateUploadResp> upload(
            @RequestPart MultipartFile file,
            @RequestParam String groupId,
            @RequestParam String artifactId,
            @RequestParam String version) throws IOException {

        log.info("开始上传模板: groupId={}, artifactId={}, version={}", groupId, artifactId, version);

        // 1. 参数校验
        ValidationUtils.throwIf(file::isEmpty, "文件不能为空");
        ValidationUtils.throwIfBlank(groupId, "groupId 不能为空");
        ValidationUtils.throwIfBlank(artifactId, "artifactId 不能为空");
        ValidationUtils.throwIfBlank(version, "version 不能为空");

        // 2. 验证文件类型
        String filename = file.getOriginalFilename();
        CheckUtils.throwIf(filename == null || !filename.endsWith(".zip"), "只支持 ZIP 文件");

        // 3. 上传并解压（复用 FileService）
        TemplateUploadResp resp = templateFileService.uploadTemplate(
            file, groupId, artifactId, version
        );

        log.info("模板上传成功: templateId={}", resp.getTemplateId());
        return R.ok(resp);
    }
}

