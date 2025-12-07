package top.codestyle.pojo.enums;

import co.elastic.clients.elasticsearch._types.SortOrder;

public enum TemplateSortOrderType {
    ASC, DESC;

    public static SortOrder fromString(String str) {
        if (str == null) return SortOrder.Desc; // 默认
        return switch (str.toUpperCase()) {
            case "ASC" -> SortOrder.Asc;
            case "DESC" -> SortOrder.Desc;
            default -> throw new IllegalArgumentException("Invalid sortOrder: " + str);
        };
    }

}
