package top.codestyle.validation;

import jakarta.validation.groups.Default;

/**
 * 分组校验
 *
 * @author Charles7c
 * @since 2024/7/3 22:01
 */
public interface ValidationGroup extends Default {

    /**
     * 分组校验-增删改查
     */
    interface Storage extends ValidationGroup {
        /**
         * 本地存储
         */
        interface Local extends Storage {
        }

        /**
         * 兼容S3协议存储
         */
        interface S3 extends Storage {
        }
    }
}