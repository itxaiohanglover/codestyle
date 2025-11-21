package top.codestyle.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import top.codestyle.pojo.dto.TimeRangeParamDTO;
import top.codestyle.pojo.enums.TemplateSortField;
import top.codestyle.pojo.vo.HomePageSearchPageableResultVO;

/**
 * @author ChonghaoGao
 * @date 2025/11/9 20:04
 * 用于存放一些传统的ES检索设置
 *
 */
public interface HomePageSearchService {

   HomePageSearchPageableResultVO searchHomePage(
            String keyword,
            int page,
            int size,
            TemplateSortField popularitySortField,
            SortOrder sortOrder,
            TimeRangeParamDTO timeRangeParamDTO);

}
