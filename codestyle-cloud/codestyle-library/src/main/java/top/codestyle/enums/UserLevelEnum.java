package top.codestyle.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户等级枚举
 *
 * @author huxc2020
 */
@Getter
@AllArgsConstructor
public enum UserLevelEnum {

    /**
     * 普通用户
     */
    NORMAL(0, "普通用户"),

    /**
     * VIP用户
     */
    VIP(1, "VIP用户");

    /**
     * 等级值
     */
    private final Integer value;

    /**
     * 等级名称
     */
    private final String name;

    /**
     * 根据值获取枚举
     *
     * @param value 等级值
     * @return 用户等级枚举
     */
    public static UserLevelEnum fromValue(Integer value) {
        if (value == null) {
            return NORMAL;
        }
        for (UserLevelEnum level : values()) {
            if (level.getValue().equals(value)) {
                return level;
            }
        }
        return NORMAL;
    }
}
