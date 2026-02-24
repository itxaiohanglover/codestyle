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

package top.codestyle.admin.search.model;

import lombok.Data;

import java.util.List;

/**
 * meta.json 模型
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Data
public class MetaJson {

    /**
     * groupId
     */
    private String groupId;

    /**
     * artifactId
     */
    private String artifactId;

    /**
     * 版本号
     */
    private String version;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 文件列表
     */
    private List<FileInfo> files;

    /**
     * 文件信息
     */
    @Data
    public static class FileInfo {

        /**
         * 文件名
         */
        private String filename;

        /**
         * 文件路径
         */
        private String filePath;

        /**
         * 文件描述
         */
        private String description;

        /**
         * 输入变量列表
         */
        private List<InputVariable> inputVariables;

        /**
         * SHA256 校验和
         */
        private String sha256;
    }

    /**
     * 输入变量
     */
    @Data
    public static class InputVariable {

        /**
         * 变量名
         */
        private String name;

        /**
         * 变量类型
         */
        private String type;

        /**
         * 变量注释
         */
        private String comment;

        /**
         * 示例值
         */
        private String example;
    }
}

