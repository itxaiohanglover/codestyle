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
 * @date 2025/11/19 16:28)
 *       根据创建时间、更新时间、编辑时间进行过滤 比如过滤出一个月内更新的模板库
 */
public enum TemplateTimeFieldFilter {
    CREATE_TIME, UPDATE_TIME, EDIT_TIME;

    public static TemplateTimeFieldFilter from(String str) {
        for (TemplateTimeFieldFilter type : values()) {
            if (type.name().equalsIgnoreCase(str)) {
                return type;
            }
        }
        throw new IllegalArgumentException("TemplateTimeFieldFilter invalid range param: " + str);
    }
}
