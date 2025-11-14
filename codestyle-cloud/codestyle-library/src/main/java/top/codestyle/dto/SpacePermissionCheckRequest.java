package top.codestyle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 空间权限校验请求DTO
 *
 * @author huxc2020
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpacePermissionCheckRequest {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 模板库ID（可选，用于仓库大小和成员数量校验）
     */
    private Long libraryId;

    /**
     * 文件大小（可选，用于文件大小校验）
     */
    private Long fileSize;

    /**
     * 文件名（可选，用于文件类型校验）
     */
    private String fileName;

    /**
     * 额外增加的大小（可选，用于仓库大小校验）
     */
    private Long additionalSize;
}

