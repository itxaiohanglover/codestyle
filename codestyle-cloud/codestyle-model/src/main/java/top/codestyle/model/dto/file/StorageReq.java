package top.codestyle.model.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.codestyle.constant.RegexConstants;
import top.codestyle.enums.DisEnableStatusEnum;
import top.codestyle.model.enums.StorageTypeEnum;
import top.codestyle.validation.ValidationGroup;
import top.continew.starter.extension.crud.model.req.BaseReq;


import java.io.Serial;

/**
 * 存储请求参数
 *
 * @author Charles7c
 * @since 2023/12/26 22:09
 */
@Data
@Schema(description = "存储请求参数")
public class StorageReq extends BaseReq {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "存储1")
    @NotBlank(message = "名称不能为空")
    @Length(max = 100, message = "名称长度不能超过 {max} 个字符")
    private String name;

    /**
     * 编码
     */
    @Schema(description = "编码", example = "local")
    @NotBlank(message = "编码不能为空")
    @Pattern(regexp = RegexConstants.GENERAL_CODE, message = "编码长度为 2-30 个字符，支持大小写字母、数字、下划线，以字母开头")
    private String code;

    /**
     * 类型
     */
    @Schema(description = "类型", example = "2")
    @NotNull(message = "类型非法")
    private StorageTypeEnum type;

    /**
     * 访问密钥
     */
    @Schema(description = "访问密钥", example = "")
    @Length(max = 255, message = "访问密钥长度不能超过 {max} 个字符")
    @NotBlank(message = "访问密钥不能为空", groups = ValidationGroup.Storage.S3.class)
    private String accessKey;

    /**
     * 私有密钥
     */
    @Schema(description = "私有密钥", example = "")
    @NotBlank(message = "私有密钥不能为空", groups = ValidationGroup.Storage.S3.class)
    private String secretKey;

    /**
     * 终端节点
     */
    @Schema(description = "终端节点", example = "")
    @Length(max = 255, message = "终端节点长度不能超过 {max} 个字符")
    @NotBlank(message = "终端节点不能为空", groups = ValidationGroup.Storage.S3.class)
    private String endpoint;

    /**
     * 桶名称
     */
    @Schema(description = "桶名称", example = "C:/continew-admin/data/file/")
    @Length(max = 255, message = "桶名称长度不能超过 {max} 个字符")
    @NotBlank(message = "桶名称不能为空", groups = ValidationGroup.Storage.S3.class)
    @NotBlank(message = "存储路径不能为空", groups = ValidationGroup.Storage.Local.class)
    private String bucketName;

    /**
     * 域名
     */
    @Schema(description = "域名", example = "http://localhost:8000/file")
    @Length(max = 255, message = "域名长度不能超过 {max} 个字符")
    @NotBlank(message = "域名不能为空")
    private String domain;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sort;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "存储描述")
    @Length(max = 200, message = "描述长度不能超过 {max} 个字符")
    private String description;

    /**
     * 是否为默认存储
     */
    @Schema(description = "是否为默认存储", example = "true")
    @NotNull(message = "是否为默认存储不能为空")
    private Boolean isDefault;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private DisEnableStatusEnum status;
}