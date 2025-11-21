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

