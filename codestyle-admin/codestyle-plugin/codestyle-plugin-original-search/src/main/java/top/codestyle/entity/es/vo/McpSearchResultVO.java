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

package top.codestyle.entity.es.vo;

import lombok.Data;

import java.util.List;

/**
 * MCP搜索结果VO实体类
 */
@Data
public class McpSearchResultVO {

    private String groupId;
    private String artifactId;
    private Config config;
    private String description;

    /**
     * 配置类
     */
    @Data
    public static class Config {
        private String version;
        private List<FileInfo> files;
    }

    /**
     * 文件信息类
     */
    @Data
    public static class FileInfo {
        private String filePath;
        private String description;
        private String filename;
        private List<InputVarivales> inputVarivales;
        private String sha256;
    }

    /**
     * 输入变量类
     */
    @Data
    public static class InputVarivales {
        private String variableName;
        private String variableType;
        private String variableComment;
        private String example;
    }

}