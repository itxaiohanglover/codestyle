package top.codestyle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 空间权限校验结果DTO
 *
 * @author huxc2020
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpacePermissionCheckResult {

    /**
     * 是否允许
     */
    private Boolean allowed;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 当前使用情况
     */
    private Object currentUsage;

    /**
     * 限制值
     */
    private Object limit;

    /**
     * 创建允许的结果
     *
     * @return 允许的结果
     */
    public static SpacePermissionCheckResult allowed() {
        return SpacePermissionCheckResult.builder()
                .allowed(true)
                .message("校验通过")
                .build();
    }

    /**
     * 创建允许的结果（只带信息）
     *
     * @param message 提示信息
     * @return 允许的结果
     */
    public static  SpacePermissionCheckResult allowed(String message) {
        return SpacePermissionCheckResult.builder()
                .allowed(true)
                .message(message)
                .build();
    }
    /**
     * 创建允许的结果（带详细信息）
     *
     * @param message      提示信息
     * @param currentUsage 当前使用情况
     * @param limit        限制值
     * @return 允许的结果
     */
    public static SpacePermissionCheckResult allowed(String message, Object currentUsage, Object limit) {
        return SpacePermissionCheckResult.builder()
                .allowed(true)
                .message(message)
                .currentUsage(currentUsage)
                .limit(limit)
                .build();
    }

    /**
     * 创建拒绝的结果
     *
     * @param message 提示信息
     * @return 拒绝的结果
     */
    public static SpacePermissionCheckResult denied(String message) {
        return SpacePermissionCheckResult.builder()
                .allowed(false)
                .message(message)
                .build();
    }

    /**
     * 创建拒绝的结果（带详细信息）
     *
     * @param message      提示信息
     * @param currentUsage 当前使用情况
     * @param limit        限制值
     * @return 拒绝的结果
     */
    public static SpacePermissionCheckResult denied(String message, Object currentUsage, Object limit) {
        return SpacePermissionCheckResult.builder()
                .allowed(false)
                .message(message)
                .currentUsage(currentUsage)
                .limit(limit)
                .build();
    }
}

