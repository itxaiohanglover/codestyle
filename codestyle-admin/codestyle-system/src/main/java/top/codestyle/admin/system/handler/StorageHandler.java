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

package top.codestyle.admin.system.handler;

import org.springframework.web.multipart.MultipartFile;
import top.codestyle.admin.system.enums.StorageTypeEnum;
import top.codestyle.admin.system.model.entity.StorageDO;
import top.codestyle.admin.system.model.req.MultipartUploadInitReq;
import top.codestyle.admin.system.model.resp.file.MultipartUploadInitResp;
import top.codestyle.admin.system.model.resp.file.MultipartUploadResp;

import java.util.List;

/**
 * 存储类型处理器
 * <p>
 * 专注于文件操作，不包含业务逻辑
 *
 * @author KAI
 * @since 2025/7/30 17:15
 */
public interface StorageHandler {

    MultipartUploadInitResp initMultipartUpload(StorageDO storageDO, MultipartUploadInitReq req);

    /**
     * 分片上传
     *
     * @param storageDO 存储实体
     * @param path      存储路径
     * @param uploadId  文件名
     * @param file      文件对象
     * @return {@link MultipartUploadResp} 分片上传结果
     */
    MultipartUploadResp uploadPart(StorageDO storageDO,
                                   String path,
                                   String uploadId,
                                   Integer partNumber,
                                   MultipartFile file);

    /**
     * 合并分片
     *
     * @param storageDO 存储实体
     * @param uploadId  上传Id
     */
    void completeMultipartUpload(StorageDO storageDO,
                                 List<MultipartUploadResp> parts,
                                 String path,
                                 String uploadId,
                                 boolean needVerify);

    /**
     * 清楚分片
     *
     * @param storageDO 存储实体
     * @param uploadId  上传Id
     */
    void cleanPart(StorageDO storageDO, String uploadId);

    /**
     * 获取存储类型
     *
     * @return 存储类型
     */
    StorageTypeEnum getType();
}
