package top.codestyle.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 空间权限配置类
 * 支持 Nacos 动态刷新配置
 *
 * @author huxc2020
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "space.permission")
public class SpacePermissionConfig {

    /**
     * 普通用户限制配置
     */
    private UserLimitConfig normal = new UserLimitConfig();

    /**
     * VIP用户限制配置
     */
    private UserLimitConfig vip = new UserLimitConfig();

    /**
     * 用户限制配置内部类
     */
    @Data
    public static class UserLimitConfig {
        /**
         * 最大模板库数量
         */
        private Integer maxLibraryCount = 3;

        /**
         * 最大仓库大小（字节）
         */
        private Long maxRepositorySize = 10485760L; // 10MB

        /**
         * 单次上传文件最大大小（字节）
         */
        private Long maxSingleFileSize = 1048576L; // 1MB

        /**
         * 最大成员数量
         */
        private Integer maxMemberCount = 10;

        /**
         * 是否限制文件类型（true表示仅允许编程文件）
         */
        private Boolean restrictFileType = true;
    }

    /**
     * 根据用户等级获取限制配置
     *
     * @param userLevel 用户等级（0-普通用户，1-VIP用户）
     * @return 限制配置
     */
    public UserLimitConfig getLimitByUserLevel(Integer userLevel) {
        if (userLevel != null && userLevel == 1) {
            return vip;
        }
        return normal;
    }
}

