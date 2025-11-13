package top.codestyle.entity.es;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


/**
 * Code Style Template 的查询结果模板类
 *
 */
@Data
@Document(indexName = "template_index")
public class CodeStyleTemplate {
    // groupId uses keyword type for exact matching and aggregation, better performance
    @Field(type = FieldType.Keyword, store = true)
    private String groupId;
    
    // artifactId also uses keyword type
    @Field(type = FieldType.Keyword, store = true)
    private String artifactId;

    @Field(type = FieldType.Keyword)
    private String filePath;

    @Field(type = FieldType.Keyword)
    private String version;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String fileName;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String projectDescription;
    
    // description uses text type for full-text search
    @Field(type = FieldType.Text, analyzer = "ik_max_word") // Chinese word segmenter
    private String description;

    @Id
    private String fileId;
}