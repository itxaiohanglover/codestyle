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

package top.codestyle.admin.search.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.List;

/**
 * 
 * 搜索实体类，对应Elasticsearch索引结构
 * 
 * @author ChonghaoGao
 * @date 2025/12/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "meta_info", createIndex = false)
@Setting(settingPath = "/es_jsons/es-settings.json")
public class SearchEntity {

    @Id
    @Field(type = FieldType.Long, index = false)
    private Long id;

    // 使用标准分析器，避免依赖 IK 插件
    // 如果需要中文分词，请确保 Elasticsearch 已安装 IK 插件
    @Field(type = FieldType.Text)
    private String groupId;

    @Field(type = FieldType.Text)
    private String artifactId;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Object, dynamic = Dynamic.FALSE)
    private Config config;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Config {
        @Field(type = FieldType.Keyword)
        private String version;

        @Field(type = FieldType.Keyword)
        private List<FileInfo> files;

        @Data
        public static class FileInfo {
            @Field(type = FieldType.Keyword)
            private String filename;

            @Field(type = FieldType.Keyword)
            private String sha256;

            @Field(type = FieldType.Keyword)
            private String filePath;

            @Field(type = FieldType.Text)
            private String description;

            @Field(type = FieldType.Nested)
            private List<MetaVariable> inputVariables;

            @Data
            public static class MetaVariable {
                @Field(type = FieldType.Keyword)
                private String variableType;

                @Field(type = FieldType.Keyword)
                private String variableName;

                @Field(type = FieldType.Keyword)
                private String variableComment;

                @Field(type = FieldType.Keyword)
                private String example;
            }
        }
    }
}