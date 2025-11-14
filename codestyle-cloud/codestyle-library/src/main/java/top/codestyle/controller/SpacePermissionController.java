package top.codestyle.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.codestyle.dto.SpacePermissionCheckRequest;
import top.codestyle.dto.SpacePermissionCheckResult;
import top.codestyle.dto.UserSpacePermissionInfo;
import top.codestyle.service.SpacePermissionService;

/**
 * 空间权限控制器
 *
 * @author huxc2020
 */
@Slf4j
@RestController
@RequestMapping("/api/space-permission")
@RequiredArgsConstructor
public class SpacePermissionController {

    private final SpacePermissionService spacePermissionService;

    /**
     * 校验模板库数量
     *
     * @param request 校验请求
     * @return 校验结果
     */
    @PostMapping("/check/library-count")
    public SpacePermissionCheckResult checkLibraryCount(@RequestBody SpacePermissionCheckRequest request) {
        log.info("校验模板库数量，userId: {}", request.getUserId());
        return spacePermissionService.checkLibraryCountLimit(request.getUserId());
    }

    /**
     * 校验仓库大小
     *
     * @param request 校验请求
     * @return 校验结果
     */
    @PostMapping("/check/repository-size")
    public SpacePermissionCheckResult checkRepositorySize(@RequestBody SpacePermissionCheckRequest request) {
        log.info("校验仓库大小，libraryId: {}, additionalSize: {}",
                request.getLibraryId(), request.getAdditionalSize());
        return spacePermissionService.checkRepositorySizeLimit(
                request.getLibraryId(),
                request.getAdditionalSize()
        );
    }

    /**
     * 校验单次文件大小
     *
     * @param request 校验请求
     * @return 校验结果
     */
    @PostMapping("/check/file-size")
    public SpacePermissionCheckResult checkFileSize(@RequestBody SpacePermissionCheckRequest request) {
        log.info("校验单次文件大小，userId: {}, fileSize: {}",
                request.getUserId(), request.getFileSize());
        return spacePermissionService.checkSingleFileSizeLimit(
                request.getUserId(),
                request.getFileSize()
        );
    }

    /**
     * 校验成员数量
     *
     * @param request 校验请求
     * @return 校验结果
     */
    @PostMapping("/check/member-count")
    public SpacePermissionCheckResult checkMemberCount(@RequestBody SpacePermissionCheckRequest request) {
        log.info("校验成员数量，libraryId: {}", request.getLibraryId());
        return spacePermissionService.checkMemberCountLimit(request.getLibraryId());
    }

    /**
     * 校验文件类型
     *
     * @param request 校验请求
     * @return 校验结果
     */
    @PostMapping("/check/file-type")
    public SpacePermissionCheckResult checkFileType(@RequestBody SpacePermissionCheckRequest request) {
        log.info("校验文件类型，userId: {}, fileName: {}",
                request.getUserId(), request.getFileName());
        return spacePermissionService.checkFileTypeAllowed(
                request.getUserId(),
                request.getFileName()
        );
    }

    /**
     * 获取用户权限信息
     *
     * @param userId 用户ID
     * @return 用户权限信息
     */
    @GetMapping("/info/{userId}")
    public UserSpacePermissionInfo getUserPermissionInfo(@PathVariable Long userId) {
        log.info("获取用户权限信息，userId: {}", userId);
        return spacePermissionService.getUserPermissionInfo(userId);
    }

    /**
     * 综合校验（用于文件上传前的完整校验）
     *
     * @param request 校验请求
     * @return 校验结果
     */
    @PostMapping("/check/comprehensive")
    public SpacePermissionCheckResult comprehensiveCheck(@RequestBody SpacePermissionCheckRequest request) {
        log.info("综合校验，userId: {}, libraryId: {}, fileSize: {}, fileName: {}",
                request.getUserId(), request.getLibraryId(),
                request.getFileSize(), request.getFileName());

        // 1. 校验文件类型
        SpacePermissionCheckResult fileTypeResult = spacePermissionService.checkFileTypeAllowed(
                request.getUserId(),
                request.getFileName()
        );
        if (!fileTypeResult.getAllowed()) {
            return fileTypeResult;
        }

        // 2. 校验单次文件大小
        SpacePermissionCheckResult fileSizeResult = spacePermissionService.checkSingleFileSizeLimit(
                request.getUserId(),
                request.getFileSize()
        );
        if (!fileSizeResult.getAllowed()) {
            return fileSizeResult;
        }

        // 3. 校验仓库大小（如果提供了libraryId）
        if (request.getLibraryId() != null) {
            SpacePermissionCheckResult repoSizeResult = spacePermissionService.checkRepositorySizeLimit(
                    request.getLibraryId(),
                    request.getFileSize()
            );
            if (!repoSizeResult.getAllowed()) {
                return repoSizeResult;
            }
        }

        return SpacePermissionCheckResult.allowed("综合校验通过");
    }
}

