package top.codestyle.entity.es.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.Date;
import java.util.List;

@Data
@Document(indexName = "template_mcp")
@Setting(settingPath = "/elasticsearch/template-settings.json")
public class McpSearchDO {

    @Id
    private String id;

    // 模板组信息
    @Field(type = FieldType.Keyword)
    private String groupId;

    @Field(type = FieldType.Keyword)
    private String artifactId;


    // 配置列表，包含多个版本
    @Field(type = FieldType.Nested, includeInParent = true)
    private List<Config> configs;

    // 统计信息
    @Field(type = FieldType.Long)
    private Long totalFileCount;

    @Field(type = FieldType.Long)
    private Long totalLikeCount;

    @Field(type = FieldType.Long)
    private Long totalFavoriteCount;

    @Field(type = FieldType.Double)
    private Double hotScoreWeight;

    // 时间字段
    @Field(type = FieldType.Date)
    private Date createTime;

    @Field(type = FieldType.Date)
    private Date updateTime;

    @Field(type = FieldType.Integer)
    private Integer isDelete;

    // 配置类
    @Data
    public static class Config {
        @Field(type = FieldType.Keyword)
        private String version;

        @Field(type = FieldType.Nested, includeInParent = true)
        private List<FileInfo> files;
    }

    // 文件信息类
    @Data
    public static class FileInfo {
        @Field(type = FieldType.Keyword)
        private String filePath;

        @Field(type = FieldType.Text, analyzer = "ik_max_word_pinyin", searchAnalyzer = "ik_smart_pinyin")
        private String description;

        @Field(type = FieldType.Keyword)
        private String filename;

        @Field(type = FieldType.Nested, includeInParent = true)
        private List<InputVariable> inputVariables;

        @Field(type = FieldType.Keyword)
        private String sha256;
    }

    // 输入变量类
    @Data
    public static class InputVariable {
        @Field(type = FieldType.Keyword)
        private String variableName;

        @Field(type = FieldType.Keyword)
        private String variableType;

        @Field(type = FieldType.Text, analyzer = "ik_max_word_pinyin")
        private String variableComment;

        @Field(type = FieldType.Keyword)
        private String example;
    }
}