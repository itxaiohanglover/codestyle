package top.codestyle.pojo.enums;

/**
 * @author ChonghaoGao
 * @date 2025/11/19 14:57)
 * 时间戳查询枚举类
 */
public enum TemplateDateRangeFilter {
    NONE,       // 不过滤
    LAST_1_DAY,
    LAST_7_DAYS,
    LAST_30_DAYS,
    LAST_180_DAYS,
    CUSTOM ;     // 用户自传时间范围（比如指定 startTime/endTime）
    public static TemplateDateRangeFilter from(String str) {
        for (TemplateDateRangeFilter type : values()) {
            if (type.name().equalsIgnoreCase(str)) {
                return type;
            }
        }
        throw new IllegalArgumentException("TemplateDateRangeFileter Invalid range param: " + str);
    }
}
