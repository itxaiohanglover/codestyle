package top.codestyle.pojo.enums;

/**
 * @author ChonghaoGao
 * @date 2025/11/19 14:57)
 *  和收藏点赞有关的枚举类
 */
public enum TemplateSortField {
    HOT_SCORE,
    LIKE_COUNT,
    FAVORITE_COUNT,
    CREATE_TIME,
    UPDATE_TIME,
    EDIT_TIME;

    public static TemplateSortField from(String str) {
        for (TemplateSortField type : values()) {
            if (type.name().equalsIgnoreCase(str)) {
                return type;
            }
        }
        throw new IllegalArgumentException("TemplateSortField Invalid range param: " + str);
    }
}

