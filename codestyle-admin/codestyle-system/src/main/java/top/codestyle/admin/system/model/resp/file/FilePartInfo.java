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

package top.codestyle.admin.system.model.resp.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件分片信息
 *
 * @author echo
 * @since 2.14.0
 */
@Data
@Schema(description = "文件分片信息")
public class FilePartInfo implements Serializable {
    /**
     * 文件ID
     */
    @Schema(description = "文件ID")
    private String fileId;

    /**
     * 分片编号（从1开始）
     */
    @Schema(description = "分片编号（从1开始）")
    private Integer partNumber;

    /**
     * 分片大小
     */
    @Schema(description = "分片大小")
    private Long partSize;

    /**
     * 分片MD5
     */
    @Schema(description = "分片MD5")
    private String partMd5;

    /**
     * 分片ETag（S3返回的标识）
     */
    @Schema(description = "分片ETag")
    private String partETag;

    /**
     * 上传ID（S3分片上传标识）
     */
    @Schema(description = "上传ID")
    private String uploadId;

    /**
     * 上传时间
     */
    @Schema(description = "上传时间")
    private LocalDateTime uploadTime;

    /**
     * 状态：UPLOADING, SUCCESS, FAILED
     */
    @Schema(description = "状态")
    private String status;

    /**
     * 存储桶
     */
    @Schema(description = "存储桶")
    private String bucket;

    /**
     * 文件路径
     */
    @Schema(description = "文件路径")
    private String path;

}