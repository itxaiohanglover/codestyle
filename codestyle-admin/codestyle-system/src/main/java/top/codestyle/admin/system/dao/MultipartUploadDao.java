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

package top.codestyle.admin.system.dao;

import top.codestyle.admin.system.model.resp.file.FilePartInfo;
import top.codestyle.admin.system.model.resp.file.MultipartUploadInitResp;

import java.util.List;
import java.util.Map;

/**
 * 分片上传持久化接口
 * <p>
 * 纯粹的缓存操作，不包含业务逻辑：
 * 1. MD5到uploadId的映射管理
 * 2. 分片信息缓存
 * 3. 上传状态缓存
 * </p>
 *
 * @author KAI
 * @since 2.14.0
 */
public interface MultipartUploadDao {

    /**
     * 根据MD5获取uploadId
     *
     * @param md5 文件MD5值
     * @return uploadId，如果不存在则返回null
     */
    String getUploadIdByMd5(String md5);

    /**
     * 缓存MD5到uploadId的映射
     *
     * @param md5      文件MD5值
     * @param uploadId 上传ID
     */
    void setMd5Mapping(String md5, String uploadId);

    /**
     * 删除MD5映射
     *
     * @param md5 文件MD5值
     */
    void deleteMd5Mapping(String md5);

    /**
     * 设置缓存分片上传信息
     *
     * @param uploadId 上传ID
     * @param initResp 初始化响应
     * @param metadata 元数据
     */
    void setMultipartUpload(String uploadId, MultipartUploadInitResp initResp, Map<String, String> metadata);

    /**
     * 获取分片上传信息
     *
     * @param uploadId 上传ID
     * @return 分片上传信息，如果不存在则返回null
     */
    MultipartUploadInitResp getMultipartUpload(String uploadId);

    /**
     * 删除分片上传信息
     *
     * @param uploadId 上传ID
     */
    void deleteMultipartUpload(String uploadId);

    void deleteMultipartUploadAll(String uploadId);

    /**
     * 设置缓存分片信息
     *
     * @param uploadId     上传ID
     * @param filePartInfo 分片信息
     */
    void setFilePart(String uploadId, FilePartInfo filePartInfo);

    /**
     * 获取所有分片信息
     *
     * @param uploadId 上传ID
     * @return 分片信息列表
     */
    List<FilePartInfo> getFileParts(String uploadId);

    /**
     * 删除所有分片信息
     *
     * @param uploadId 上传ID
     */
    void deleteFileParts(String uploadId);

    /**
     * 检查分片是否存在
     *
     * @param uploadId   上传ID
     * @param partNumber 分片编号
     * @return 是否存在
     */
    boolean existsFilePart(String uploadId, int partNumber);

    /**
     * 清理过期的缓存数据
     */
    void cleanupExpiredData();
}
