package top.codestyle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.codestyle.config.SpacePermissionConfig;
import top.codestyle.constant.ProgrammingFileTypeConstant;
import top.codestyle.dto.SpacePermissionCheckResult;
import top.codestyle.dto.UserSpacePermissionInfo;
import top.codestyle.entity.LibraryInfo;
import top.codestyle.entity.LibraryMember;
import top.codestyle.enums.UserLevelEnum;
import top.codestyle.mapper.LibraryInfoMapper;
import top.codestyle.mapper.LibraryMemberMapper;
import top.codestyle.mapper.UserMapper;
import top.codestyle.service.SpacePermissionService;

/**
 * 空间权限服务实现类
 *
 * @author huxc2020
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpacePermissionServiceImpl implements SpacePermissionService {

    private final SpacePermissionConfig permissionConfig;
    private final LibraryInfoMapper libraryInfoMapper;
    private final LibraryMemberMapper libraryMemberMapper;
    private final UserMapper userMapper;

    @Override
    public SpacePermissionCheckResult checkLibraryCountLimit(Long userId) {
        if (userId == null) {
            return SpacePermissionCheckResult.denied("用户ID不能为空");
        }

        try {
            // 获取用户等级
            Integer userLevel = userMapper.getUserLevelByUserId(userId);
            SpacePermissionConfig.UserLimitConfig limitConfig = permissionConfig.getLimitByUserLevel(userLevel);

            // 查询用户创建的模板库数量（作为owner的库）
            QueryWrapper<LibraryMember> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .eq("role", 1); // role=1表示owner
            long currentCount = libraryMemberMapper.selectCount(queryWrapper);

            // 校验是否超限
            if (currentCount >= limitConfig.getMaxLibraryCount()) {
                return SpacePermissionCheckResult.denied(
                        String.format("已达到模板库数量上限，当前：%d/%d", currentCount, limitConfig.getMaxLibraryCount()),
                        currentCount,
                        limitConfig.getMaxLibraryCount()
                );
            }

            return SpacePermissionCheckResult.allowed(
                    "可以创建新模板库",
                    currentCount,
                    limitConfig.getMaxLibraryCount()
            );
        } catch (Exception e) {
            log.error("校验模板库数量限制失败，userId: {}", userId, e);
            return SpacePermissionCheckResult.denied("校验失败：" + e.getMessage());
        }
    }

    @Override
    public SpacePermissionCheckResult checkRepositorySizeLimit(Long libraryId, Long additionalSize) {
        if (libraryId == null) {
            return SpacePermissionCheckResult.denied("模板库ID不能为空");
        }
        if (additionalSize == null || additionalSize < 0) {
            return SpacePermissionCheckResult.denied("文件大小参数无效");
        }

        try {
            // 查询模板库信息
            LibraryInfo libraryInfo = libraryInfoMapper.selectById(libraryId);
            if (libraryInfo == null) {
                return SpacePermissionCheckResult.denied("模板库不存在");
            }

            // 获取库的owner用户ID
            QueryWrapper<LibraryMember> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("library_id", libraryId)
                    .eq("role", 1); // role=1表示owner
            LibraryMember owner = libraryMemberMapper.selectOne(queryWrapper);

            if (owner == null) {
                return SpacePermissionCheckResult.denied("模板库所有者不存在");
            }

            // 获取owner的用户等级
            Integer userLevel = userMapper.getUserLevelByUserId(owner.getUserId());
            SpacePermissionConfig.UserLimitConfig limitConfig = permissionConfig.getLimitByUserLevel(userLevel);

            // 计算添加后的总大小
            Long currentSize = libraryInfo.getTotalFileSize() != null ? libraryInfo.getTotalFileSize() : 0L;
            Long afterSize = currentSize + additionalSize;

            // 校验是否超限
            if (afterSize > limitConfig.getMaxRepositorySize()) {
                return SpacePermissionCheckResult.denied(
                        String.format("超过仓库大小限制，当前：%d字节，增加：%d字节，限制：%d字节",
                                currentSize, additionalSize, limitConfig.getMaxRepositorySize()),
                        afterSize,
                        limitConfig.getMaxRepositorySize()
                );
            }

            return SpacePermissionCheckResult.allowed(
                    "仓库大小未超限",
                    afterSize,
                    limitConfig.getMaxRepositorySize()
            );
        } catch (Exception e) {
            log.error("校验仓库大小限制失败，libraryId: {}, additionalSize: {}", libraryId, additionalSize, e);
            return SpacePermissionCheckResult.denied("校验失败：" + e.getMessage());
        }
    }

    @Override
    public SpacePermissionCheckResult checkSingleFileSizeLimit(Long userId, Long fileSize) {
        if (userId == null) {
            return SpacePermissionCheckResult.denied("用户ID不能为空");
        }
        if (fileSize == null || fileSize < 0) {
            return SpacePermissionCheckResult.denied("文件大小参数无效");
        }

        try {
            // 获取用户等级
            Integer userLevel = userMapper.getUserLevelByUserId(userId);
            SpacePermissionConfig.UserLimitConfig limitConfig = permissionConfig.getLimitByUserLevel(userLevel);

            // 校验是否超限
            if (fileSize > limitConfig.getMaxSingleFileSize()) {
                return SpacePermissionCheckResult.denied(
                        String.format("文件大小超过限制，当前：%d字节，限制：%d字节",
                                fileSize, limitConfig.getMaxSingleFileSize()),
                        fileSize,
                        limitConfig.getMaxSingleFileSize()
                );
            }

            return SpacePermissionCheckResult.allowed(
                    "文件大小未超限",
                    fileSize,
                    limitConfig.getMaxSingleFileSize()
            );
        } catch (Exception e) {
            log.error("校验单次文件大小限制失败，userId: {}, fileSize: {}", userId, fileSize, e);
            return SpacePermissionCheckResult.denied("校验失败：" + e.getMessage());
        }
    }

    @Override
    public SpacePermissionCheckResult checkMemberCountLimit(Long libraryId) {
        if (libraryId == null) {
            return SpacePermissionCheckResult.denied("模板库ID不能为空");
        }

        try {
            // 查询模板库信息
            LibraryInfo libraryInfo = libraryInfoMapper.selectById(libraryId);
            if (libraryInfo == null) {
                return SpacePermissionCheckResult.denied("模板库不存在");
            }

            // 获取库的owner用户ID
            QueryWrapper<LibraryMember> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("library_id", libraryId)
                    .eq("role", 1); // role=1表示owner
            LibraryMember owner = libraryMemberMapper.selectOne(queryWrapper);

            if (owner == null) {
                return SpacePermissionCheckResult.denied("模板库所有者不存在");
            }

            // 获取owner的用户等级
            Integer userLevel = userMapper.getUserLevelByUserId(owner.getUserId());
            SpacePermissionConfig.UserLimitConfig limitConfig = permissionConfig.getLimitByUserLevel(userLevel);

            // 查询当前成员数量
            QueryWrapper<LibraryMember> memberQuery = new QueryWrapper<>();
            memberQuery.eq("library_id", libraryId);
            long currentCount = libraryMemberMapper.selectCount(memberQuery);

            // 校验是否超限
            if (currentCount >= limitConfig.getMaxMemberCount()) {
                return SpacePermissionCheckResult.denied(
                        String.format("已达到成员数量上限，当前：%d/%d",
                                currentCount, limitConfig.getMaxMemberCount()),
                        currentCount,
                        limitConfig.getMaxMemberCount()
                );
            }

            return SpacePermissionCheckResult.allowed(
                    "可以添加新成员",
                    currentCount,
                    limitConfig.getMaxMemberCount()
            );
        } catch (Exception e) {
            log.error("校验成员数量限制失败，libraryId: {}", libraryId, e);
            return SpacePermissionCheckResult.denied("校验失败：" + e.getMessage());
        }
    }

    @Override
    public SpacePermissionCheckResult checkFileTypeAllowed(Long userId, String fileName) {
        if (userId == null) {
            return SpacePermissionCheckResult.denied("用户ID不能为空");
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            return SpacePermissionCheckResult.denied("文件名不能为空");
        }

        try {
            // 获取用户等级
            Integer userLevel = userMapper.getUserLevelByUserId(userId);
            SpacePermissionConfig.UserLimitConfig limitConfig = permissionConfig.getLimitByUserLevel(userLevel);

            // 如果不限制文件类型，直接通过
            if (!limitConfig.getRestrictFileType()) {
                return SpacePermissionCheckResult.allowed("VIP用户无文件类型限制");
            }

            // 检查是否为编程文件
            boolean isProgrammingFile = ProgrammingFileTypeConstant.isProgrammingFile(fileName);
            if (!isProgrammingFile) {
                return SpacePermissionCheckResult.denied(
                        String.format("文件类型不允许上传，普通用户仅允许上传编程文件，文件名：%s", fileName)
                );
            }

            return SpacePermissionCheckResult.allowed("文件类型允许上传");
        } catch (Exception e) {
            log.error("校验文件类型失败，userId: {}, fileName: {}", userId, fileName, e);
            return SpacePermissionCheckResult.denied("校验失败：" + e.getMessage());
        }
    }

    @Override
    public UserSpacePermissionInfo getUserPermissionInfo(Long userId) {
        if (userId == null) {
            return null;
        }

        try {
            // 获取用户等级
            Integer userLevel = userMapper.getUserLevelByUserId(userId);
            if (userLevel == null) {
                userLevel = 0; // 默认为普通用户
            }

            UserLevelEnum levelEnum = UserLevelEnum.fromValue(userLevel);
            SpacePermissionConfig.UserLimitConfig limitConfig = permissionConfig.getLimitByUserLevel(userLevel);

            // 查询用户创建的模板库数量
            QueryWrapper<LibraryMember> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .eq("role", 1); // role=1表示owner
            long currentLibraryCount = libraryMemberMapper.selectCount(queryWrapper);

            // 构建返回信息
            String allowedFileTypesDesc = limitConfig.getRestrictFileType()
                    ? "仅允许编程文件（.java, .vue, .js等）"
                    : "无限制";

            return UserSpacePermissionInfo.builder()
                    .userId(userId)
                    .userLevel(userLevel)
                    .userLevelName(levelEnum.getName())
                    .currentLibraryCount((int) currentLibraryCount)
                    .maxLibraryCount(limitConfig.getMaxLibraryCount())
                    .maxRepositorySize(limitConfig.getMaxRepositorySize())
                    .maxSingleFileSize(limitConfig.getMaxSingleFileSize())
                    .maxMemberCount(limitConfig.getMaxMemberCount())
                    .restrictFileType(limitConfig.getRestrictFileType())
                    .allowedFileTypesDesc(allowedFileTypesDesc)
                    .canCreateLibrary(currentLibraryCount < limitConfig.getMaxLibraryCount())
                    .build();
        } catch (Exception e) {
            log.error("获取用户权限信息失败，userId: {}", userId, e);
            return null;
        }
    }
}

