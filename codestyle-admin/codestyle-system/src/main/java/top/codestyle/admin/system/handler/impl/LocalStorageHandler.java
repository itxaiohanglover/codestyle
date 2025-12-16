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

package top.codestyle.admin.system.handler.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import top.codestyle.admin.system.constant.MultipartUploadConstants;
import top.codestyle.admin.system.enums.StorageTypeEnum;
import top.codestyle.admin.system.handler.StorageHandler;
import top.codestyle.admin.system.model.entity.StorageDO;
import top.codestyle.admin.system.model.req.MultipartUploadInitReq;
import top.codestyle.admin.system.model.resp.file.MultipartUploadInitResp;
import top.codestyle.admin.system.model.resp.file.MultipartUploadResp;
import top.codestyle.admin.system.service.FileService;
import top.continew.starter.core.exception.BaseException;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 本地存储处理器
 * <p>实现分片上传、合并、取消等操作。</p>
 *
 * @author KAI
 * @since 2023/7/30 22:58
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalStorageHandler implements StorageHandler {

    private final FileService fileService;

    @Override
    public MultipartUploadInitResp initMultipartUpload(StorageDO storageDO, MultipartUploadInitReq req) {
        String uploadId = UUID.randomUUID().toString();
        String bucket = storageDO.getBucketName(); // 本地存储中，bucket是存储根路径
        String parentPath = req.getParentPath();
        String fileName = req.getFileName();
        StrUtil.blankToDefault(parentPath, StrUtil.SLASH);
        String relativePath = StrUtil.endWith(parentPath, StrUtil.SLASH)
            ? parentPath + fileName
            : parentPath + StrUtil.SLASH + fileName;
        try {
            // 创建临时目录用于存储分片
            String tempDirPath = buildTempDirPath(bucket, uploadId);
            FileUtil.mkdir(tempDirPath);
            fileService.createParentDir(parentPath, storageDO);
            // 构建返回结果
            MultipartUploadInitResp result = new MultipartUploadInitResp();
            result.setBucket(bucket);
            result.setFileId(UUID.randomUUID().toString());
            result.setUploadId(uploadId);
            result.setPlatform(storageDO.getCode());
            result.setFileName(fileName);
            result.setFileMd5(req.getFileMd5());
            result.setFileSize(req.getFileSize());
            result.setExtension(FileUtil.extName(fileName));
            result.setContentType(req.getContentType());
            result.setPath(relativePath);
            result.setParentPath(parentPath);
            result.setPartSize(MultipartUploadConstants.MULTIPART_UPLOAD_PART_SIZE);
            log.info("本地存储初始化分片上传成功: uploadId={}, path={}", uploadId, parentPath);
            return result;
        } catch (Exception e) {
            log.error("本地存储初始化分片上传失败: {}", e.getMessage(), e);
            throw new BaseException("本地存储初始化分片上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public MultipartUploadResp uploadPart(StorageDO storageDO,
                                          String path,
                                          String uploadId,
                                          Integer partNumber,
                                          MultipartFile file) {
        try {
            long size = file.getSize();
            String bucket = storageDO.getBucketName();
            // 获取临时目录路径
            String tempDirPath = buildTempDirPath(bucket, uploadId);

            // 确保临时目录存在
            File tempDir = new File(tempDirPath);
            if (!tempDir.exists()) {
                FileUtil.mkdir(tempDirPath);
            }

            // 保存分片文件
            String partFilePath = tempDirPath + File.separator + String.format("part_%s", partNumber);
            File partFile = new File(partFilePath);
            file.transferTo(partFile);

            // 计算ETag (使用MD5)
            String etag = DigestUtil.md5Hex(partFile);

            // 构建返回结果
            MultipartUploadResp result = new MultipartUploadResp();
            result.setPartNumber(partNumber);
            result.setPartETag(etag);
            result.setPartSize(size);
            result.setSuccess(true);

            log.info("本地存储分片上传成功: uploadId={}, partNumber={}, etag={}", uploadId, partNumber, etag);
            return result;
        } catch (Exception e) {
            log.error("本地存储分片上传失败: uploadId={}, partNumber={}, error={}", uploadId, partNumber, e.getMessage(), e);

            MultipartUploadResp result = new MultipartUploadResp();
            result.setPartNumber(partNumber);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            return result;
        }
    }

    @Override
    public void completeMultipartUpload(StorageDO storageDO,
                                        List<MultipartUploadResp> parts,
                                        String path,
                                        String uploadId,
                                        boolean needVerify) {
        String bucket = storageDO.getBucketName(); // 本地存储中，bucket是存储根路径
        String tempDirPath = buildTempDirPath(bucket, uploadId);

        try {
            // 本地存储不需要验证，直接使用传入的分片信息
            Path targetPath = Paths.get(bucket, path);
            Files.createDirectories(targetPath.getParent());

            // 合并分片
            try (OutputStream out = Files
                .newOutputStream(targetPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

                // 按分片编号排序
                List<MultipartUploadResp> sortedParts = parts.stream()
                    .filter(MultipartUploadResp::isSuccess)
                    .sorted(Comparator.comparingInt(MultipartUploadResp::getPartNumber))
                    .toList();

                // 逐个读取并写入
                for (MultipartUploadResp part : sortedParts) {
                    Path partPath = Paths.get(tempDirPath, String.format("part_%s", part.getPartNumber()));

                    if (!Files.exists(partPath)) {
                        throw new BaseException("分片文件不存在: partNumber=" + part.getPartNumber());
                    }

                    Files.copy(partPath, out);
                }
            }
            // 清理临时文件
            cleanupTempFiles(tempDirPath);

            log.info("本地存储分片合并成功: uploadId={}, targetPath={}", uploadId, targetPath);

        } catch (Exception e) {
            log.error("本地存储分片合并失败: uploadId={}, path={}, error={}", uploadId, path, e.getMessage(), e);
            throw new BaseException("完成分片上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void cleanPart(StorageDO storageDO, String uploadId) {
        try {
            String bucket = storageDO.getBucketName();
            // 获取临时目录路径
            String tempDirPath = buildTempDirPath(bucket, uploadId);

            // 清理临时文件
            cleanupTempFiles(tempDirPath);

            log.info("本地存储分片清理成功: uploadId={}", uploadId);
        } catch (Exception e) {
            log.error("本地存储分片清理失败: uploadId={}, error={}", uploadId, e.getMessage(), e);
            throw new BaseException("本地存储分片清理失败: " + e.getMessage(), e);
        }
    }

    @Override
    public StorageTypeEnum getType() {
        return StorageTypeEnum.LOCAL;
    }

    /**
     * 构建临时目录路径
     *
     * @param bucket   存储桶（本地存储根路径）
     * @param uploadId 上传ID
     * @return 临时目录路径
     */
    private String buildTempDirPath(String bucket, String uploadId) {
        return StrUtil
            .appendIfMissing(bucket, File.separator) + MultipartUploadConstants.TEMP_DIR_NAME + File.separator + uploadId;
    }

    /**
     * 构建目标文件路径
     *
     * @param bucket 存储桶（本地存储根路径）
     * @param path   文件路径
     * @return 目标文件路径
     */
    private String buildTargetDirPath(String bucket, String path) {
        return StrUtil.appendIfMissing(bucket, File.separator) + path;
    }

    /**
     * 清理临时文件
     *
     * @param tempDirPath 临时目录路径
     */
    private void cleanupTempFiles(String tempDirPath) {
        try {
            FileUtil.del(tempDirPath);
        } catch (Exception e) {
            log.warn("清理临时文件失败: {}, {}", tempDirPath, e.getMessage());
        }
    }
}