package top.codestyle.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import top.codestyle.enums.DisEnableStatusEnum;
import top.codestyle.model.enums.StorageTypeEnum;
import top.continew.starter.extension.crud.model.entity.BaseDO;
import top.continew.starter.security.crypto.annotation.FieldEncrypt;


import java.io.Serial;

/**
 * 存储实体
 *
 * @author Charles7c
 * @since 2023/12/26 22:09
 */
@Data
@TableName("sys_storage")
public class StorageDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    private String name;

    /**
     * 编码
     */
    private String code;

    /**
     * 类型
     */
    @TableField(typeHandler = EnumOrdinalTypeHandler.class)
    private StorageTypeEnum type;

    /**
     * Access Key（访问密钥）
     */
    @FieldEncrypt
    private String accessKey;

    /**
     * Secret Key（私有密钥）
     */
    @FieldEncrypt
    private String secretKey;

    /**
     * Endpoint（终端节点）
     */
    private String endpoint;

    /**
     * 桶名称
     */
    private String bucketName;

    /**
     * 域名
     */
    private String domain;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否为默认存储
     */
    private Boolean isDefault;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态
     */
    private DisEnableStatusEnum status;
}