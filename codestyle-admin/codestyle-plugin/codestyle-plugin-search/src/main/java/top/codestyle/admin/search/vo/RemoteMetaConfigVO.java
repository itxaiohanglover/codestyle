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

package top.codestyle.admin.search.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoteMetaConfigVO {
    /**
     * 组织名(用户名)
     */
    private String groupId;

    /**
     * 模板组名
     */
    private String artifactId;

    /**
     * 模板组总体描述
     */
    private String description;

    /**
     * 单个版本配置对象
     */
    private Config config;

    /**
     * topK个groupId的Map - key为groupId，value为count，按count大小降序排列
     */
    private Map<String, Long> aggGroupIds;

    /**
     * topK个artifactId的Map - key为artifactId，value为count，按count大小降序排列
     */
    private Map<String, Long> aggArtifactIds;

    /**
     * 配置内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Config {
        private String version;
        private List<FileInfo> files;
    }

    /**
     * 文件信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileInfo {
        private String filePath;
        private String description;
        private String filename;
        private List<InputVariable> inputVariables;
        private String sha256;
    }

    /**
     * 输入变量内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InputVariable {
        private String variableName;
        private String variableType;
        private String variableComment;
        private String example;
    }
}