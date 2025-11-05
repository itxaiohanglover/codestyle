
package top.codestyle.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.continew.starter.core.enums.BaseEnum;

/**
 * 存储类型枚举
 *
 * @author Charles7c
 * @since 2023/12/27 21:45
 */
@Getter
@RequiredArgsConstructor
public enum StorageTypeEnum implements BaseEnum<Integer> {

    /**
     * 兼容S3协议存储
     */
    S3(1, "兼容S3协议存储"),

    /**
     * 本地存储
     */
    LOCAL(2, "本地存储"),;



    @EnumValue
    private final Integer value;

    private final String description;

    @Override
    public Integer getValue() {
        return value;
    }


}