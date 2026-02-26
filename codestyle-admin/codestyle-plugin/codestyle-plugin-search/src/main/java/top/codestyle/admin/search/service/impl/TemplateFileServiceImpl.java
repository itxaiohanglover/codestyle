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

package top.codestyle.admin.search.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.codestyle.admin.common.context.UserContext;
import top.codestyle.admin.common.context.UserContextHolder;
import top.codestyle.admin.search.model.MetaJson;
import top.codestyle.admin.search.model.resp.TemplateUploadResp;
import top.codestyle.admin.search.service.TemplateFileService;
import top.codestyle.admin.system.enums.FileTypeEnum;
import top.codestyle.admin.system.mapper.FileMapper;
import top.codestyle.admin.system.model.entity.FileDO;
import top.codestyle.admin.system.model.entity.StorageDO;
import top.codestyle.admin.system.service.FileService;
import top.codestyle.admin.system.service.StorageService;
import top.continew.starter.core.util.validation.CheckUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 模板文件服务实现
 * <p>
 * 所有模板文件通过系统 FileService 存储，确保在文件管理器中可见。
 * 上传流程：接收压缩包 → 解压到临时目录 → 逐个文件上传到 FileService → 清理临时目录。
 * </p>
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateFileServiceImpl implements TemplateFileService {

    /** 模板文件在文件管理系统中的统一前缀 */
    private static final String TEMPLATE_PATH_PREFIX = "/templates/";

    private final FileService fileService;
    private final FileStorageService fileStorageService;
    private final StorageService storageService;
    private final FileMapper fileMapper;

    // ==================== 公开接口实现 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateUploadResp uploadTemplate(MultipartFile file,
                                             String groupIdParam,
                                             String artifactIdParam,
                                             String versionParam) throws IOException {
        Path tempDir = null;
        File tempArchiveFile = null;

        // 确保有用户上下文（开放 API 可能无登录状态）
        UserContext originalContext = ensureUserContext();

        try {
            // 1. 创建临时目录
            tempDir = Files.createTempDirectory("template-upload-");

            // 2. 保存压缩包到临时文件（保留原始扩展名，用于格式识别）
            String origName = file.getOriginalFilename();
            String suffix = origName != null && origName.contains(".")
                ? origName.substring(origName.indexOf('.'))
                : ".zip";
            tempArchiveFile = Files.createTempFile("upload-", suffix).toFile();
            file.transferTo(tempArchiveFile);

            // 3. 解压到临时目录
            File tempDirFile = tempDir.toFile();
            extractArchive(tempArchiveFile, tempDirFile);

            // 4. 自动定位模板根目录（处理压缩包外层嵌套目录）
            File templateRoot = locateTemplateRoot(tempDirFile);

            // 5. 解析 meta.json，自动补全参数
            MetaJson metaJson = parseMetaJson(templateRoot);
            String groupId = firstNonBlank(groupIdParam, metaJson.getGroupId());
            String artifactId = firstNonBlank(artifactIdParam, metaJson.getArtifactId());
            String version = firstNonBlank(versionParam, metaJson.getVersion());
            CheckUtils.throwIfBlank(groupId, "groupId 不能为空，请在 meta.json 中指定");
            CheckUtils.throwIfBlank(artifactId, "artifactId 不能为空，请在 meta.json 中指定");
            CheckUtils.throwIfBlank(version, "version 不能为空，请在 meta.json 中指定");
            validateTemplateFiles(templateRoot, metaJson);

            // 6. 删除旧版本文件（如有）
            deleteOldVersionFiles(groupId, artifactId, version);

            // 7. 逐个文件上传到 FileService
            String templatePrefix = buildTemplatePrefix(groupId, artifactId, version);
            uploadExtractedFiles(templateRoot, templateRoot, templatePrefix);

            // 8. 若 meta.json 没有 description，尝试从 README 中提取
            if (StrUtil.isBlank(metaJson.getDescription())) {
                metaJson.setDescription(readReadmeContent(templateRoot));
            }

            // 9. 构建响应
            String downloadUrl = String
                .format("/open-api/template/download?groupId=%s&artifactId=%s&version=%s", groupId, artifactId, version);
            return buildUploadResponse(groupId, artifactId, version, metaJson, downloadUrl);

        } finally {
            // 清理临时文件
            if (tempDir != null && Files.exists(tempDir)) {
                FileUtil.del(tempDir.toFile());
            }
            if (tempArchiveFile != null && tempArchiveFile.exists()) {
                FileUtil.del(tempArchiveFile);
            }
            // 恢复用户上下文
            restoreUserContext(originalContext);
        }
    }

    @Override
    public List<Map<String, Object>> listFiles(String groupId, String artifactId, String version) {
        String templatePrefix = buildTemplatePrefix(groupId, artifactId, version);

        // 查询该模板版本下的所有文件记录
        List<FileDO> files = fileMapper.lambdaQuery()
            .likeRight(FileDO::getParentPath, templatePrefix)
            .ne(FileDO::getType, FileTypeEnum.DIR)
            .list();
        CheckUtils.throwIf(files.isEmpty(), "模板不存在: {}/{}/{}", groupId, artifactId, version);

        // 将平铺的文件列表重建为树形结构
        return buildFileTreeFromRecords(files, templatePrefix);
    }

    @Override
    public String readFileContent(String groupId,
                                  String artifactId,
                                  String version,
                                  String filePath) throws IOException {
        // 从 filePath 分离出目录和文件名
        String fileName = filePath.contains("/") ? filePath.substring(filePath.lastIndexOf('/') + 1) : filePath;
        String relativeDir = filePath.contains("/") ? filePath.substring(0, filePath.lastIndexOf('/')) : "";

        // 构建 parentPath 查询条件
        String templatePrefix = buildTemplatePrefix(groupId, artifactId, version);
        String parentPath = relativeDir.isEmpty() ? templatePrefix : templatePrefix + "/" + relativeDir;

        // 查询文件记录
        FileDO fileDO = fileMapper.lambdaQuery()
            .eq(FileDO::getParentPath, parentPath)
            .eq(FileDO::getOriginalName, fileName)
            .ne(FileDO::getType, FileTypeEnum.DIR)
            .one();
        CheckUtils.throwIfNull(fileDO, "文件不存在: {}", filePath);
        CheckUtils.throwIf(fileDO.getSize() != null && fileDO.getSize() > 512 * 1024, "文件过大，不支持在线预览");

        // 通过存储系统下载文件内容
        StorageDO storage = storageService.getById(fileDO.getStorageId());
        FileInfo fileInfo = fileDO.toFileInfo(storage);
        byte[] bytes = fileStorageService.download(fileInfo).bytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public File createTemplateZip(String groupId, String artifactId, String version) throws IOException {
        String templatePrefix = buildTemplatePrefix(groupId, artifactId, version);

        // 查询所有非目录文件
        List<FileDO> files = fileMapper.lambdaQuery()
            .likeRight(FileDO::getParentPath, templatePrefix)
            .ne(FileDO::getType, FileTypeEnum.DIR)
            .list();
        CheckUtils.throwIf(files.isEmpty(), "模板不存在: {}/{}/{}", groupId, artifactId, version);

        // 创建临时 ZIP 文件
        File zipFile = Files.createTempFile("template-download-", ".zip").toFile();
        StorageDO storage = storageService.getDefaultStorage();

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (FileDO fileDO : files) {
                // 还原文件的相对路径
                String relativePath = fileDO.getParentPath().substring(templatePrefix.length());
                if (relativePath.startsWith("/")) {
                    relativePath = relativePath.substring(1);
                }
                String entryPath = relativePath.isEmpty()
                    ? fileDO.getOriginalName()
                    : relativePath + "/" + fileDO.getOriginalName();

                // 从存储系统下载文件内容并写入 ZIP 条目
                StorageDO fileStorage = fileDO.getStorageId().equals(storage.getId())
                    ? storage
                    : storageService.getById(fileDO.getStorageId());
                FileInfo fileInfo = fileDO.toFileInfo(fileStorage);
                byte[] bytes = fileStorageService.download(fileInfo).bytes();

                zos.putNextEntry(new ZipEntry(entryPath));
                zos.write(bytes);
                zos.closeEntry();
            }
        } catch (Exception e) {
            FileUtil.del(zipFile);
            throw new IOException("模板打包失败: " + e.getMessage(), e);
        }
        return zipFile;
    }

    @Override
    public void deleteTemplateFiles(String groupId, String artifactId, String version) {
        deleteOldVersionFiles(groupId, artifactId, version);
    }

    // ==================== 压缩包解压 ====================

    /**
     * 根据文件后缀自动选择解压方式
     *
     * @param archiveFile 压缩包文件
     * @param destDir     解压目标目录
     * @throws IOException 解压失败
     */
    private void extractArchive(File archiveFile, File destDir) throws IOException {
        String name = archiveFile.getName().toLowerCase();
        if (name.endsWith(".tar.gz") || name.endsWith(".tgz")) {
            extractTarGz(archiveFile, destDir);
        } else if (name.endsWith(".7z")) {
            extract7z(archiveFile, destDir);
        } else {
            unzipFile(archiveFile, destDir);
        }
    }

    /** 解压 TAR.GZ 格式 */
    private void extractTarGz(File tarGzFile, File destDir) throws IOException {
        try (FileInputStream fis = new FileInputStream(tarGzFile);
            GzipCompressorInputStream gzIn = new GzipCompressorInputStream(fis);
            TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn)) {
            ArchiveEntry entry;
            while ((entry = tarIn.getNextEntry()) != null) {
                File newFile = newFile(destDir, entry.getName());
                if (entry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("创建目录失败: " + newFile);
                    }
                } else {
                    Files.createDirectories(newFile.getParentFile().toPath());
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        tarIn.transferTo(fos);
                    }
                }
            }
        }
    }

    /** 解压 7Z 格式 */
    private void extract7z(File sevenZFile, File destDir) throws IOException {
        try (SevenZFile szf = SevenZFile.builder().setFile(sevenZFile).get()) {
            SevenZArchiveEntry entry;
            while ((entry = szf.getNextEntry()) != null) {
                File newFile = newFile(destDir, entry.getName());
                if (entry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("创建目录失败: " + newFile);
                    }
                } else {
                    Files.createDirectories(newFile.getParentFile().toPath());
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = szf.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }

    /** 解压 ZIP 格式（Java 原生） */
    private void unzipFile(File zipFile, File destDir) throws IOException {
        byte[] buffer = new byte[4096];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String entryName = zipEntry.getName().replace("\r", "").replace("\n", "");
                File newFile = newFile(destDir, entryName);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("创建目录失败: " + newFile);
                    }
                } else {
                    Files.createDirectories(newFile.getParentFile().toPath());
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
     * 创建目标文件（防止 Zip Slip 目录遍历漏洞）
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

    // ==================== 模板解析与校验 ====================

    /**
     * 自动定位模板根目录
     * <p>
     * 压缩包可能有外层嵌套目录（如 template-v1/meta.json），
     * 递归向下查找直到找到包含 meta.json 的目录。
     * </p>
     */
    private File locateTemplateRoot(File dir) {
        if (new File(dir, "meta.json").exists()) {
            return dir;
        }
        File[] children = dir.listFiles();
        if (children != null && children.length == 1 && children[0].isDirectory()) {
            return locateTemplateRoot(children[0]);
        }
        return dir;
    }

    /** 解析 meta.json 文件 */
    private MetaJson parseMetaJson(File tempDir) {
        File metaJsonFile = new File(tempDir, "meta.json");
        CheckUtils.throwIf(!metaJsonFile.exists(), "模板缺少 meta.json 文件");
        String content = FileUtil.readUtf8String(metaJsonFile);
        return JSONUtil.toBean(content, MetaJson.class);
    }

    /** 校验 meta.json 中声明的文件是否都存在 */
    private void validateTemplateFiles(File tempDir, MetaJson metaJson) {
        for (MetaJson.FileInfo fi : metaJson.getFiles()) {
            String filePath = fi.getFilePath() + "/" + fi.getFilename();
            File templateFile = new File(tempDir, filePath);
            CheckUtils.throwIf(!templateFile.exists(), "模板文件不存在: {}", filePath);
        }
    }

    // ==================== 文件上传与管理 ====================

    /**
     * 将解压后的模板文件逐个上传到 FileService
     *
     * @param currentDir     当前遍历的目录
     * @param templateRoot   模板根目录（用于计算相对路径）
     * @param templatePrefix FileService 中的存储前缀路径
     */
    private void uploadExtractedFiles(File currentDir, File templateRoot, String templatePrefix) throws IOException {
        File[] children = currentDir.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                uploadExtractedFiles(child, templateRoot, templatePrefix);
            } else {
                // 计算文件在 FileService 中的 parentPath
                String relativePath = templateRoot.toPath()
                    .relativize(child.getParentFile().toPath())
                    .toString()
                    .replace("\\", "/");
                String parentPath = relativePath.isEmpty() ? templatePrefix : templatePrefix + "/" + relativePath;

                try {
                    fileService.upload(child, parentPath);
                    log.debug("文件已上传: {}/{}", parentPath, child.getName());
                } catch (Exception e) {
                    log.warn("文件上传失败: {}/{}, 原因: {}", parentPath, child.getName(), e.getMessage());
                }
            }
        }
    }

    /**
     * 删除指定模板版本的所有旧文件
     * <p>
     * 重新上传同版本模板时，先清理旧文件避免残留。
     * </p>
     */
    private void deleteOldVersionFiles(String groupId, String artifactId, String version) {
        String templatePrefix = buildTemplatePrefix(groupId, artifactId, version);
        List<FileDO> oldFiles = fileMapper.lambdaQuery().likeRight(FileDO::getParentPath, templatePrefix).list();
        if (!oldFiles.isEmpty()) {
            List<Long> ids = oldFiles.stream().map(FileDO::getId).collect(Collectors.toList());
            fileService.delete(ids);
            log.info("已删除旧版本模板文件 {} 个", ids.size());
        }
    }

    // ==================== 文件树构建 ====================

    /**
     * 从数据库文件记录重建树形结构
     * <p>
     * 将平铺的 FileDO 列表，按 parentPath 分组，
     * 还原为前端所需的嵌套树形 JSON 结构。
     * </p>
     */
    private List<Map<String, Object>> buildFileTreeFromRecords(List<FileDO> files, String templatePrefix) {
        // 收集所有相对路径
        Set<String> dirPaths = new TreeSet<>();
        Map<String, List<FileDO>> filesByDir = new HashMap<>();

        for (FileDO file : files) {
            String relativePath = file.getParentPath().substring(templatePrefix.length());
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }
            filesByDir.computeIfAbsent(relativePath, k -> new ArrayList<>()).add(file);
            // 收集所有中间目录
            String[] parts = relativePath.split("/");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                if (!part.isEmpty()) {
                    if (!sb.isEmpty()) {
                        sb.append("/");
                    }
                    sb.append(part);
                    dirPaths.add(sb.toString());
                }
            }
        }

        // 递归构建树
        return buildTreeLevel("", dirPaths, filesByDir);
    }

    /** 递归构建某一层级的树节点 */
    private List<Map<String, Object>> buildTreeLevel(String currentPath,
                                                     Set<String> allDirPaths,
                                                     Map<String, List<FileDO>> filesByDir) {
        List<Map<String, Object>> nodes = new ArrayList<>();

        // 添加子目录
        Set<String> directChildDirs = new TreeSet<>();
        String prefix = currentPath.isEmpty() ? "" : currentPath + "/";
        for (String dirPath : allDirPaths) {
            if (dirPath.startsWith(prefix) && !dirPath.equals(currentPath)) {
                String remaining = dirPath.substring(prefix.length());
                if (!remaining.contains("/")) {
                    directChildDirs.add(dirPath);
                }
            }
        }
        for (String dirPath : directChildDirs) {
            String dirName = dirPath.contains("/") ? dirPath.substring(dirPath.lastIndexOf('/') + 1) : dirPath;
            Map<String, Object> dirNode = new LinkedHashMap<>();
            dirNode.put("name", dirName);
            dirNode.put("path", dirPath);
            dirNode.put("isDir", true);
            dirNode.put("children", buildTreeLevel(dirPath, allDirPaths, filesByDir));
            nodes.add(dirNode);
        }

        // 添加当前目录下的文件
        List<FileDO> currentFiles = filesByDir.getOrDefault(currentPath, Collections.emptyList());
        currentFiles.sort(Comparator.comparing(f -> f.getOriginalName().toLowerCase()));
        for (FileDO file : currentFiles) {
            String filePath = currentPath.isEmpty()
                ? file.getOriginalName()
                : currentPath + "/" + file.getOriginalName();
            Map<String, Object> fileNode = new LinkedHashMap<>();
            fileNode.put("name", file.getOriginalName());
            fileNode.put("path", filePath);
            fileNode.put("isDir", false);
            fileNode.put("size", file.getSize());
            nodes.add(fileNode);
        }

        return nodes;
    }

    // ==================== 辅助方法 ====================

    /** 构建模板在 FileService 中的存储前缀路径 */
    private String buildTemplatePrefix(String groupId, String artifactId, String version) {
        return TEMPLATE_PATH_PREFIX + groupId + "/" + artifactId + "/" + version;
    }

    /** 优先取第一个非空字符串 */
    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }

    /**
     * 从模板根目录读取 README 文件内容作为描述
     */
    private String readReadmeContent(File templateRoot) {
        String[] readmeNames = {"README.md", "readme.md", "README.MD", "README", "readme"};
        for (String name : readmeNames) {
            File readme = new File(templateRoot, name);
            if (readme.exists() && readme.isFile()) {
                try {
                    String content = FileUtil.readUtf8String(readme);
                    if (StrUtil.isNotBlank(content)) {
                        return content;
                    }
                } catch (Exception e) {
                    log.warn("读取 README 失败: {}", readme.getAbsolutePath(), e);
                }
            }
        }
        return null;
    }

    /** 构建上传响应 */
    private TemplateUploadResp buildUploadResponse(String groupId,
                                                   String artifactId,
                                                   String version,
                                                   MetaJson metaJson,
                                                   String downloadUrl) {
        List<TemplateUploadResp.TemplateFileInfo> fileInfoList = new ArrayList<>();
        for (MetaJson.FileInfo fi : metaJson.getFiles()) {
            fileInfoList.add(TemplateUploadResp.TemplateFileInfo.builder()
                .filename(fi.getFilename())
                .filePath(fi.getFilePath())
                .description(fi.getDescription())
                .build());
        }

        return TemplateUploadResp.builder()
            .templateId(UUID.randomUUID().toString())
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
     * 确保存在用户上下文（FileService 操作需要）
     *
     * @return 原始上下文（若为 null 说明是新创建的系统上下文，需要在 finally 中清除）
     */
    private UserContext ensureUserContext() {
        try {
            return UserContextHolder.getContext();
        } catch (Exception e) {
            UserContext systemContext = new UserContext();
            systemContext.setId(1L);
            systemContext.setUsername("system");
            UserContextHolder.setContext(systemContext, false);
            return null;
        }
    }

    /** 恢复用户上下文 */
    private void restoreUserContext(UserContext originalContext) {
        if (originalContext == null) {
            UserContextHolder.clearContext();
        }
    }
}
