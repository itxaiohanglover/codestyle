package top.codestyle.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.codestyle.model.enums.FileTypeEnum;
import top.continew.starter.data.core.annotation.Query;
import top.continew.starter.data.core.enums.QueryType;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 文件查询条件
 *
 * @author Charles7c
 * @since 2023/12/23 10:38
 */
@Data
@Schema(description = "文件查询条件")
public class FileQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "图片")
    @Query(type = QueryType.LIKE)
    private String name;

    /**
     * 类型
     */
    @Schema(description = "类型", example = "2")
    private FileTypeEnum type;

    /**
     * 存储路径数组
     */
    @Schema(description = "存储路径数组" , example = "/2023/12/,/2024/01/")
    private List<String> paths;
}