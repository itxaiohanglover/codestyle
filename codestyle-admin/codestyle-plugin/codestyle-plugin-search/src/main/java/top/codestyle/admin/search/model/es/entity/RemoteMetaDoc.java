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

package top.codestyle.admin.search.model.es.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.List;

@Data
@ToString
@Document(indexName = "meta_info")
@Setting(settingPath = "/es_jsons/_mapping.json")
public class RemoteMetaDoc {
    @Id
    @Field(type = FieldType.Long, index = false, docValues = false)
    private Long id;

    @Field(type = FieldType.Keyword, index = false, docValues = false)
    private String groupId;

    @Field(type = FieldType.Keyword, index = false, docValues = false)
    private String artifactId;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Nested)
    private Config config;

    @Data
    public static class Config {
        @Field(type = FieldType.Keyword)
        private String version;

        @Field(type = FieldType.Nested)
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