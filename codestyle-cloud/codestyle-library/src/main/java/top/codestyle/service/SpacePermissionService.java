package top.codestyle.service;

import top.codestyle.dto.SpacePermissionCheckResult;
import top.codestyle.dto.UserSpacePermissionInfo;

/**
 * 空间权限服务接口
 *
 * @author huxc2020
 */
public interface SpacePermissionService {

    /**
     * 校验用户是否可以创建新的模板库
     *
     * @param userId 用户ID
     * @return 校验结果
     */
    SpacePermissionCheckResult checkLibraryCountLimit(Long userId);

    /**
     * 校验添加文件后是否超过仓库大小限制
     *
     * @param libraryId      模板库ID
     * @param additionalSize 额外增加的文件大小（字节）
     * @return 校验结果
     */
    SpacePermissionCheckResult checkRepositorySizeLimit(Long libraryId, Long additionalSize);

    /**
     * 校验单次上传文件大小是否超限
     *
     * @param userId   用户ID
     * @param fileSize 文件大小（字节）
     * @return 校验结果
     */
    SpacePermissionCheckResult checkSingleFileSizeLimit(Long userId, Long fileSize);

    /**
     * 校验模板库是否可以添加新成员
     *
     * @param libraryId 模板库ID
     * @return 校验结果
     */
    SpacePermissionCheckResult checkMemberCountLimit(Long libraryId);

    /**
     * 校验文件类型是否允许上传
     *
     * @param userId   用户ID
     * @param fileName 文件名
     * @return 校验结果
     */
    SpacePermissionCheckResult checkFileTypeAllowed(Long userId, String fileName);

    /**
     * 获取用户的权限信息和使用情况
     *
     * @param userId 用户ID
     * @return 用户权限信息
     */
    UserSpacePermissionInfo getUserPermissionInfo(Long userId);
}

