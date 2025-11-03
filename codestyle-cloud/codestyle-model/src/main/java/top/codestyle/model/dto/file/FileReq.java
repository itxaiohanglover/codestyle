package top.codestyle.model.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.continew.starter.extension.crud.model.req.BaseReq;

import java.io.Serial;

/**
 * 修改文件参数
 *
 * @author Charles7c
 * @since 2023/12/23 10:38
 */
@Data
@Schema(description = "修改文件参数")
public class FileReq extends BaseReq {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "test123")
    @NotBlank(message = "文件名称不能为空")
    @Length(max = 255, message = "文件名称长度不能超过 {max} 个字符")
    private String name;
}