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

package top.codestyle.admin.system.handler.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import top.codestyle.admin.system.constant.MultipartUploadConstants;
import top.codestyle.admin.system.enums.StorageTypeEnum;
import top.codestyle.admin.system.factory.S3ClientFactory;
import top.codestyle.admin.system.handler.StorageHandler;
import top.codestyle.admin.system.model.entity.StorageDO;
import top.codestyle.admin.system.model.req.MultipartUploadInitReq;
import top.codestyle.admin.system.model.resp.file.MultipartUploadInitResp;
import top.codestyle.admin.system.model.resp.file.MultipartUploadResp;
import top.codestyle.admin.system.service.FileService;
import top.continew.starter.core.exception.BaseException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * S3存储处理器
 * <p>使用AWS SDK 2.x版本API。实现分片上传、合并、取消等操作。</p>
 *
 * @author KAI
 * @since 2025/07/30 20:10
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class S3StorageHandler implements StorageHandler {

    private final S3ClientFactory s3ClientFactory;

    private final FileService fileService;

    @Override
    public MultipartUploadInitResp initMultipartUpload(StorageDO storageDO, MultipartUploadInitReq req) {
        String bucket = storageDO.getBucketName();
        String parentPath = req.getParentPath();
        String fileName = req.getFileName();
        String contentType = req.getContentType();
        StrUtil.blankToDefault(parentPath, StrUtil.SLASH);
        String relativePath = StrUtil.endWith(parentPath, StrUtil.SLASH)
            ? parentPath + fileName
            : parentPath + StrUtil.SLASH + fileName;

        fileService.createParentDir(parentPath, storageDO);
        try {
            // 构建请求
            CreateMultipartUploadRequest.Builder requestBuilder = CreateMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(buildS3Key(relativePath))
                .contentType(contentType);

            // 添加元数据 暂时注释掉 mataData传递中文会导致签名校验不通过
            //            if (metaData != null && !metaData.isEmpty()) {
            //                requestBuilder.metadata(metaData);
            //            }

            S3Client s3Client = s3ClientFactory.getClient(storageDO);
            log.info("S3初始化分片上传: bucket={}, key={}, contentType={}", bucket, buildS3Key(relativePath), contentType);

            // 执行请求
            CreateMultipartUploadResponse response = s3Client.createMultipartUpload(requestBuilder.build());
            String uploadId = response.uploadId();
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
            log.info("S3初始化分片上传成功: uploadId={}, path={}", uploadId, relativePath);
            return result;

        } catch (Exception e) {
            throw new BaseException("S3初始化分片上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public MultipartUploadResp uploadPart(StorageDO storageDO,
                                          String path,
                                          String uploadId,
                                          Integer partNumber,
                                          MultipartFile file) {
        try {
            String bucket = storageDO.getBucketName();
            // 读取数据到内存（注意：实际使用时可能需要优化大文件处理）
            byte[] bytes = file.getBytes();

            // 构建请求
            UploadPartRequest request = UploadPartRequest.builder()
                .bucket(bucket)
                .key(buildS3Key(path))
                .uploadId(uploadId)
                .partNumber(partNumber)
                .contentLength((long)bytes.length)
                .build();

            // 执行上传
            S3Client s3Client = s3ClientFactory.getClient(storageDO);
            UploadPartResponse response = s3Client.uploadPart(request, RequestBody.fromBytes(bytes));
            // 构建返回结果
            MultipartUploadResp result = new MultipartUploadResp();
            result.setPartNumber(partNumber);
            result.setPartETag(response.eTag());
            result.setSuccess(true);
            log.info("S3上传分片成功: partNumber={} for key={} with uploadId={}", partNumber, path, uploadId);
            log.info("上传分片ETag: {}", response.eTag());

            return result;

        } catch (Exception e) {
            MultipartUploadResp result = new MultipartUploadResp();
            result.setPartNumber(partNumber);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            log.error("S3上传分片失败: partNumber={} for key={} with uploadId={} errorMessage={}", partNumber, path, uploadId, e
                .getMessage());
            return result;
        }
    }

    @Override
    public void completeMultipartUpload(StorageDO storageDO,
                                        List<MultipartUploadResp> parts,
                                        String path,
                                        String uploadId,
                                        boolean needVerify) {
        if (path == null) {
            throw new BaseException("无效的uploadId: " + uploadId);
        }
        String bucket = storageDO.getBucketName();
        S3Client s3Client = s3ClientFactory.getClient(storageDO);
        // 如果需要验证，比较本地记录和S3的分片信息
        if (needVerify) {
            List<MultipartUploadResp> s3Parts = listParts(bucket, path, uploadId, s3Client);
            validateParts(parts, s3Parts);
        }
        // 构建已完成的分片列表
        List<CompletedPart> completedParts = parts.stream()
            .filter(MultipartUploadResp::isSuccess)
            .map(part -> CompletedPart.builder().partNumber(part.getPartNumber()).eTag(part.getPartETag()).build())
            .sorted(Comparator.comparingInt(CompletedPart::partNumber))
            .collect(Collectors.toList());

        // 构建请求
        CompleteMultipartUploadRequest request = CompleteMultipartUploadRequest.builder()
            .bucket(bucket)
            .key(buildS3Key(path))
            .uploadId(uploadId)
            .multipartUpload(CompletedMultipartUpload.builder().parts(completedParts).build())
            .build();

        // 完成上传
        s3Client.completeMultipartUpload(request);
        log.info("S3完成分片上传: key={}, uploadId={}, parts={}", buildS3Key(path), uploadId, completedParts.size());
    }

    @Override
    public void cleanPart(StorageDO storageDO, String uploadId) {
        try {
            String bucket = storageDO.getBucketName();
            S3Client s3Client = s3ClientFactory.getClient(storageDO);

            // 列出所有未完成的分片上传
            ListMultipartUploadsRequest listRequest = ListMultipartUploadsRequest.builder().bucket(bucket).build();

            ListMultipartUploadsResponse listResponse = s3Client.listMultipartUploads(listRequest);

            // 查找匹配的上传任务
            Optional<MultipartUpload> targetUpload = listResponse.uploads()
                .stream()
                .filter(upload -> upload.uploadId().equals(uploadId))
                .findFirst();

            if (targetUpload.isPresent()) {
                MultipartUpload upload = targetUpload.get();

                // 取消分片上传
                AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(upload.key())
                    .uploadId(uploadId)
                    .build();

                s3Client.abortMultipartUpload(abortRequest);
                log.info("S3清理分片上传成功: bucket={}, key={}, uploadId={}", bucket, upload.key(), uploadId);
            } else {
                log.warn("S3未找到对应的分片上传任务: uploadId={}", uploadId);
            }

        } catch (Exception e) {
            log.error("S3清理分片上传失败: uploadId={}, error={}", uploadId, e.getMessage(), e);
            throw new BaseException("S3清理分片上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public StorageTypeEnum getType() {
        return StorageTypeEnum.OSS;
    }

    /**
     * 列出已上传的分片
     */
    public List<MultipartUploadResp> listParts(String bucket, String path, String uploadId, S3Client s3Client) {
        try {
            // 构建请求
            ListPartsRequest request = ListPartsRequest.builder()
                .bucket(bucket)
                .key(buildS3Key(path))
                .uploadId(uploadId)
                .build();

            // 获取分片列表
            ListPartsResponse response = s3Client.listParts(request);

            // 转换结果
            return response.parts().stream().map(part -> {
                MultipartUploadResp result = new MultipartUploadResp();
                result.setPartNumber(part.partNumber());
                result.setPartETag(part.eTag());
                result.setSuccess(true);
                return result;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            throw new BaseException("S3列出分片失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证分片一致性
     *
     * @param recordParts 记录部件
     * @param s3Parts     s3零件
     */
    private void validateParts(List<MultipartUploadResp> recordParts, List<MultipartUploadResp> s3Parts) {
        Map<Integer, String> recordMap = recordParts.stream()
            .collect(Collectors.toMap(MultipartUploadResp::getPartNumber, MultipartUploadResp::getPartETag));

        Map<Integer, String> s3Map = s3Parts.stream()
            .collect(Collectors.toMap(MultipartUploadResp::getPartNumber, MultipartUploadResp::getPartETag));

        // 检查分片数量
        if (recordMap.size() != s3Map.size()) {
            throw new BaseException(String.format("分片数量不一致: 本地记录=%d, S3=%d", recordMap.size(), s3Map.size()));
        }

        // 检查每个分片
        List<Integer> missingParts = new ArrayList<>();
        List<Integer> mismatchParts = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : recordMap.entrySet()) {
            Integer partNumber = entry.getKey();
            String recordETag = entry.getValue();
            String s3ETag = s3Map.get(partNumber);

            if (s3ETag == null) {
                missingParts.add(partNumber);
            } else if (!recordETag.equals(s3ETag)) {
                mismatchParts.add(partNumber);
            }
        }

        if (!missingParts.isEmpty()) {
            throw new BaseException("S3缺失分片: " + missingParts);
        }

        if (!mismatchParts.isEmpty()) {
            throw new BaseException("分片ETag不匹配: " + mismatchParts);
        }
    }

    /**
     * 规范化 S3 对象 key，去掉前导斜杠，合并多余斜杠。
     *
     * @param rawKey 你传入的完整路径，比如 "/folder//子目录//文件名.png"
     * @return 规范化后的 key，比如 "folder/子目录/文件名.png"
     */
    public static String buildS3Key(String rawKey) {
        if (rawKey == null || rawKey.isEmpty()) {
            throw new IllegalArgumentException("key 不能为空");
        }
        // 去掉前导斜杠
        while (rawKey.startsWith("/")) {
            rawKey = rawKey.substring(1);
        }
        // 替换连续多个斜杠为一个斜杠
        rawKey = rawKey.replaceAll("/+", "/");
        return rawKey;
    }

}