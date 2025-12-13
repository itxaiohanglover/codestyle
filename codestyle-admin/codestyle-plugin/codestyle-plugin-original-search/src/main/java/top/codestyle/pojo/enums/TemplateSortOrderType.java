/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.codestyle.pojo.enums;

import co.elastic.clients.elasticsearch._types.SortOrder;

public enum TemplateSortOrderType {
    ASC, DESC;

    public static SortOrder fromString(String str) {
        if (str == null)
            return SortOrder.Desc; // 默认
        return switch (str.toUpperCase()) {
            case "ASC" -> SortOrder.Asc;
            case "DESC" -> SortOrder.Desc;
            default -> throw new IllegalArgumentException("Invalid sortOrder: " + str);
        };
    }

}
