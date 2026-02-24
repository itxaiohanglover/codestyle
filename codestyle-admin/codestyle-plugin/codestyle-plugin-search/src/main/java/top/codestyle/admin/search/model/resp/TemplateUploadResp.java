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

package top.codestyle.admin.search.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 模板上传响应
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模板上传响应")
public class TemplateUploadResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板 ID
     */
    @Schema(description = "模板 ID", example = "123456")
    private String templateId;

    /**
     * groupId
     */
    @Schema(description = "groupId", example = "top.codestyle")
    private String groupId;

    /**
     * artifactId
     */
    @Schema(description = "artifactId", example = "crud-template")
    private String artifactId;

    /**
     * 版本号
     */
    @Schema(description = "版本号", example = "1.0.0")
    private String version;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称", example = "CRUD 模板")
    private String name;

    /**
     * 模板描述
     */
    @Schema(description = "模板描述", example = "标准的 CRUD 操作模板")
    private String description;

    /**
     * 下载 URL
     */
    @Schema(description = "下载 URL")
    private String downloadUrl;

    /**
     * 模板文件列表
     */
    @Schema(description = "模板文件列表")
    private List<TemplateFileInfo> files;

    /**
     * 上传时间
     */
    @Schema(description = "上传时间")
    private LocalDateTime uploadTime;

    /**
     * 模板文件信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "模板文件信息")
    public static class TemplateFileInfo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 文件名
         */
        @Schema(description = "文件名", example = "Controller.java.ftl")
        private String filename;

        /**
         * 文件路径
         */
        @Schema(description = "文件路径", example = "/src/main/java/controller")
        private String filePath;

        /**
         * 文件描述
         */
        @Schema(description = "文件描述", example = "CRUD 控制器模板")
        private String description;

        /**
         * 文件 URL
         */
        @Schema(description = "文件 URL")
        private String fileUrl;
    }
}

