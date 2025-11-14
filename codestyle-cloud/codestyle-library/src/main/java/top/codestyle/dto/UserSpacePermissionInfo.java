package top.codestyle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户空间权限信息DTO
 *
 * @author huxc2020
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSpacePermissionInfo {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户等级（0-普通用户，1-VIP用户）
     */
    private Integer userLevel;

    /**
     * 用户等级名称
     */
    private String userLevelName;

    /**
     * 当前模板库数量
     */
    private Integer currentLibraryCount;

    /**
     * 最大模板库数量
     */
    private Integer maxLibraryCount;

    /**
     * 最大仓库大小（字节）
     */
    private Long maxRepositorySize;

    /**
     * 单次上传文件最大大小（字节）
     */
    private Long maxSingleFileSize;

    /**
     * 最大成员数量
     */
    private Integer maxMemberCount;

    /**
     * 是否限制文件类型
     */
    private Boolean restrictFileType;

    /**
     * 允许的文件类型描述
     */
    private String allowedFileTypesDesc;

    /**
     * 是否可以创建新模板库
     */
    private Boolean canCreateLibrary;
}

