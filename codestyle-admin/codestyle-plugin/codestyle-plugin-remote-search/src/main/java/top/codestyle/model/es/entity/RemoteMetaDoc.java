
package top.codestyle.model.es.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;
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
        private List<File> files;

        @Data
        public static class File {
            @Field(type = FieldType.Keyword)
            private String filename;
            
            @Field(type = FieldType.Keyword)
            private String sha256;
            
            @Field(type = FieldType.Keyword)
            private String filePath;
            
            @Field(type = FieldType.Text)
            private String description;
            
            @Field(type = FieldType.Nested)
            private List<InputVariable> inputVariables;

            @Data
            public static class InputVariable {
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