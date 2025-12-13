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

/**
 * @author ChonghaoGao
 * @date 2025/11/19 14:57)
 *       时间戳查询枚举类
 */
public enum TemplateDateRangeFilter {
    NONE,       // 不过滤
    LAST_1_DAY, LAST_7_DAYS, LAST_30_DAYS, LAST_180_DAYS, CUSTOM;     // 用户自传时间范围（比如指定 startTime/endTime）

    public static TemplateDateRangeFilter from(String str) {
        for (TemplateDateRangeFilter type : values()) {
            if (type.name().equalsIgnoreCase(str)) {
                return type;
            }
        }
        throw new IllegalArgumentException("TemplateDateRangeFileter Invalid range param: " + str);
    }
}
