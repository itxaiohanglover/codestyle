
package top.codestyle.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.codestyle.constant.UiConstants;
import top.continew.starter.core.enums.BaseEnum;

/**
 * 启用/禁用状态枚举
 *
 * @author GALAwang
 * @since 2022/12/29 22:38
 */
@Getter
@RequiredArgsConstructor
public enum DisEnableStatusEnum implements BaseEnum<Integer> {

    /**
     * 启用
     */
    ENABLE(1, "启用", UiConstants.COLOR_SUCCESS),

    /**
     * 禁用
     */
    DISABLE(2, "禁用", UiConstants.COLOR_ERROR),;

    @EnumValue
    private final Integer value;

    private final String description;
    private final String color;

    @Override
    public Integer getValue() {
        return value;
    }

}