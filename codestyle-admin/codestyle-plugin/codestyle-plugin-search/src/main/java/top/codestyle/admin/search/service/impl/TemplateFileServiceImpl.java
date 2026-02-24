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

package top.codestyle.admin.search.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.codestyle.admin.common.context.UserContext;
import top.codestyle.admin.common.context.UserContextHolder;
import top.codestyle.admin.search.model.MetaJson;
import top.codestyle.admin.search.model.resp.TemplateUploadResp;
import top.codestyle.admin.search.service.TemplateFileService;
import top.codestyle.admin.system.service.FileService;
import top.continew.starter.core.util.validation.CheckUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 模板文件服务实现
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateFileServiceImpl implements TemplateFileService {

    private final FileService fileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateUploadResp uploadTemplate(
            MultipartFile file,
            String groupId,
            String artifactId,
            String version) throws IOException {

        Path tempDir = null;
        
        // ✅ 在无登录状态下，手动设置系统用户上下文
        // 必须在调用 getContext() 之前设置，否则会抛出 NotLoginException
        UserContext originalContext = null;
        try {
            originalContext = UserContextHolder.getContext();
        } catch (Exception e) {
            // 无登录状态，设置系统用户上下文
            log.info("无登录状态，使用系统用户 ID: 1");
            UserContext systemContext = new UserContext();
            systemContext.setId(1L);
            systemContext.setUsername("system");
            UserContextHolder.setContext(systemContext, false);
        }

        try {

            // 1. 创建临时目录
            tempDir = Files.createTempDirectory("template-upload-");
            log.info("创建临时目录: {}", tempDir);

            // 2. 保存上传的 ZIP 文件到临时文件
            File tempZipFile = Files.createTempFile("upload-", ".zip").toFile();
            file.transferTo(tempZipFile);
            log.info("ZIP 文件已保存: {}, 大小: {} bytes", tempZipFile.getAbsolutePath(), tempZipFile.length());

            // 3. 解压 ZIP 文件
            File tempDirFile = tempDir.toFile();
            try {
                unzipFile(tempZipFile, tempDirFile);
                log.info("ZIP 文件解压完成，目标目录: {}", tempDirFile.getAbsolutePath());
            } catch (Exception e) {
                log.error("ZIP 文件解压失败: {}", e.getMessage(), e);
                throw new IOException("ZIP 文件解压失败: " + e.getMessage(), e);
            }

            // 4. 删除临时 ZIP 文件
            FileUtil.del(tempZipFile);

            // 5. 验证模板结构
            MetaJson metaJson = validateTemplateStructure(
                tempDirFile, groupId, artifactId, version
            );

            // 6. 上传文件到存储系统（复用 FileService）
            List<FileInfo> uploadedFiles = uploadFilesToStorage(
                tempDirFile, groupId, artifactId, version
            );
            log.info("文件上传完成，共 {} 个文件", uploadedFiles.size());

            // 7. 构建响应
            return buildUploadResponse(
                groupId, artifactId, version, metaJson, uploadedFiles
            );

        } finally {
            // 8. 清理临时文件
            if (tempDir != null && Files.exists(tempDir)) {
                FileUtil.del(tempDir.toFile());
                log.debug("清理临时目录: {}", tempDir);
            }
            
            // ✅ 恢复原始用户上下文
            if (originalContext == null) {
                UserContextHolder.clearContext();
                log.debug("清除系统用户上下文");
            }
        }
    }

    /**
     * 解压 ZIP 文件（使用 Java 原生方式，避免 Hutool 在 Windows 上的路径问题）
     */
    private void unzipFile(File zipFile, File destDir) throws IOException {
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                // 规范化路径，移除可能的换行符和特殊字符
                String entryName = zipEntry.getName().replace("\r", "").replace("\n", "");
                File newFile = newFile(destDir, entryName);
                
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("创建目录失败: " + newFile);
                    }
                } else {
                    // 确保父目录存在
                    File parent = newFile.getParentFile();
                    if (!parent.exists() && !parent.mkdirs()) {
                        throw new IOException("创建父目录失败: " + parent);
                    }
                    // 写入文件
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    /**
     * 创建新文件（防止 Zip Slip 漏洞）
     */
    private File newFile(File destinationDir, String entryName) throws IOException {
        File destFile = new File(destinationDir, entryName);
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + entryName);
        }
        return destFile;
    }

    /**
     * 验证模板结构
     */
    private MetaJson validateTemplateStructure(
            File tempDir,
            String groupId,
            String artifactId,
            String version) {

        // 检查 meta.json
        File metaJsonFile = new File(tempDir, "meta.json");
        CheckUtils.throwIf(!metaJsonFile.exists(), "模板缺少 meta.json 文件");

        // 解析 meta.json
        String metaJsonContent = FileUtil.readUtf8String(metaJsonFile);
        MetaJson metaJson = JSONUtil.toBean(metaJsonContent, MetaJson.class);

        // 验证参数匹配
        CheckUtils.throwIf(!groupId.equals(metaJson.getGroupId()),
            "meta.json 中的 groupId 与参数不一致");
        CheckUtils.throwIf(!artifactId.equals(metaJson.getArtifactId()),
            "meta.json 中的 artifactId 与参数不一致");
        CheckUtils.throwIf(!version.equals(metaJson.getVersion()),
            "meta.json 中的 version 与参数不一致");

        // 验证模板文件存在
        for (MetaJson.FileInfo fileInfo : metaJson.getFiles()) {
            String filePath = fileInfo.getFilePath() + "/" + fileInfo.getFilename();
            File templateFile = new File(tempDir, filePath);
            CheckUtils.throwIf(!templateFile.exists(),
                "模板文件不存在: {}", filePath);
        }

        return metaJson;
    }

    /**
     * 上传文件到存储系统（完全复用 FileService）
     * 
     * ✅ 正确的实现：逐个上传文件，复用 FileService
     * - 自动创建父目录记录
     * - 自动上传文件到存储系统
     * - 自动计算 SHA256 哈希
     * - 自动生成 CDN URL
     * - 自动记录到数据库
     */
    private List<FileInfo> uploadFilesToStorage(
            File tempDir,
            String groupId,
            String artifactId,
            String version) throws IOException {

        // 1. 构建父路径
        String parentPath = String.format("/templates/%s/%s/%s",
            groupId, artifactId, version);

        // 2. 遍历解压的文件，逐个上传
        List<FileInfo> uploadedFiles = new ArrayList<>();
        List<File> extractedFiles = FileUtil.loopFiles(tempDir);

        for (File extractedFile : extractedFiles) {
            // 计算相对路径
            String relativePath = tempDir.toPath()
                .relativize(extractedFile.toPath())
                .toString()
                .replace("\\", "/");

            // 构建完整的父路径
            String fullParentPath = parentPath;
            if (relativePath.contains("/")) {
                String parentDir = relativePath.substring(0, relativePath.lastIndexOf("/"));
                fullParentPath = parentPath + "/" + parentDir;
            }

            // ✅ 调用 FileService.upload() 上传文件
            // 这会自动：
            // - 创建父目录记录
            // - 上传文件到存储系统
            // - 计算 SHA256 哈希
            // - 生成 CDN URL
            // - 记录到数据库
            FileInfo fileInfo = fileService.upload(extractedFile, fullParentPath);
            uploadedFiles.add(fileInfo);

            log.info("文件已上传: {} -> {}", relativePath, fileInfo.getUrl());
        }

        return uploadedFiles;
    }

    /**
     * 构建响应
     */
    private TemplateUploadResp buildUploadResponse(
            String groupId,
            String artifactId,
            String version,
            MetaJson metaJson,
            List<FileInfo> uploadedFiles) {

        // 生成模板 ID
        String templateId = UUID.randomUUID().toString();

        // 构建下载 URL
        String downloadUrl = String.format(
            "/open-api/template/download?groupId=%s&artifactId=%s&version=%s",
            groupId, artifactId, version
        );

        // 构建文件信息列表
        List<TemplateUploadResp.TemplateFileInfo> fileInfoList = new ArrayList<>();
        for (MetaJson.FileInfo fileInfo : metaJson.getFiles()) {
            // 查找对应的上传文件 URL
            String fileUrl = findFileUrl(uploadedFiles, fileInfo.getFilePath(), fileInfo.getFilename());

            TemplateUploadResp.TemplateFileInfo templateFileInfo = TemplateUploadResp.TemplateFileInfo.builder()
                .filename(fileInfo.getFilename())
                .filePath(fileInfo.getFilePath())
                .description(fileInfo.getDescription())
                .fileUrl(fileUrl)
                .build();

            fileInfoList.add(templateFileInfo);
        }

        return TemplateUploadResp.builder()
            .templateId(templateId)
            .groupId(groupId)
            .artifactId(artifactId)
            .version(version)
            .name(metaJson.getName())
            .description(metaJson.getDescription())
            .downloadUrl(downloadUrl)
            .files(fileInfoList)
            .uploadTime(LocalDateTime.now())
            .build();
    }

    /**
     * 查找文件 URL
     */
    private String findFileUrl(List<FileInfo> uploadedFiles, String filePath, String filename) {
        String targetPath = filePath + "/" + filename;
        for (FileInfo fileInfo : uploadedFiles) {
            if (fileInfo.getPath().endsWith(targetPath)) {
                return fileInfo.getUrl();
            }
        }
        return null;
    }
}

