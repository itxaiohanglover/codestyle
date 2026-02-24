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

package top.codestyle.admin.system.service;

import org.springframework.web.multipart.MultipartFile;
import top.codestyle.admin.system.model.entity.FileDO;
import top.codestyle.admin.system.model.req.MultipartUploadInitReq;
import top.codestyle.admin.system.model.resp.file.MultipartUploadInitResp;
import top.codestyle.admin.system.model.resp.file.MultipartUploadResp;

/**
 * 分片上传业务接口
 *
 * @author KAI
 * @since 2025/7/3 8:42
 */
public interface MultipartUploadService {

    MultipartUploadInitResp initMultipartUpload(MultipartUploadInitReq multiPartUploadInitReq);

    MultipartUploadResp uploadPart(MultipartFile file, String uploadId, Integer partNumber, String path);

    FileDO completeMultipartUpload(String uploadId);

    void cancelMultipartUpload(String uploadId);
}
