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

package top.codestyle.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.codestyle.pojo.enums.TemplateDateRangeFilter;
import top.codestyle.pojo.enums.TemplateTimeFieldFilter;

/**
 * @author ChonghaoGao
 * @date 2025/11/19 15:30)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeRangeParamDTO {
    private TemplateDateRangeFilter rangeType = TemplateDateRangeFilter.NONE;
    private Long startTime;   // 毫秒时间戳
    private Long endTime;     // 毫秒时间戳
    private TemplateTimeFieldFilter timeFilterField = TemplateTimeFieldFilter.CREATE_TIME; // 默认按创建时间过滤
}
