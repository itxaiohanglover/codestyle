package top.codestyle.pojo.enums;

/**
 * @author ChonghaoGao
 * @date 2025/11/19 16:28)
 * 根据创建时间、更新时间、编辑时间进行过滤 比如过滤出一个月内更新的模板库
 */
public enum TemplateTimeFieldFilter {
    CREATE_TIME,
    UPDATE_TIME,
    EDIT_TIME;
    public static TemplateTimeFieldFilter from(String str) {
        for (TemplateTimeFieldFilter type : values()) {
            if (type.name().equalsIgnoreCase(str)) {
                return type;
            }
        }
        throw new IllegalArgumentException("TemplateTimeFieldFilter invalid range param: " + str);
    }
}
