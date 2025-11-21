package top.codestyle.service.impl;


import co.elastic.clients.elasticsearch._types.SortOrder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import top.codestyle.pojo.dto.HomePageSearchResultDTO;
import top.codestyle.pojo.dto.TimeRangeParamDTO;
import top.codestyle.pojo.enums.TemplateSortField;
import top.codestyle.pojo.vo.HomePageSearchPageableResultVO;
import top.codestyle.repository.CodeStyleTemplateRepository;
import top.codestyle.service.HomePageSearchService;
import top.codestyle.utils.VOConvertUtils;


import static top.codestyle.utils.VOConvertUtils.searchCovertToHomePageSearchVO;


/**
 * @author ChonghaoGao
 * @date 2025/11/9 19:58
 * 主页搜索接口的实现类
 */
@Service
@Slf4j
@AllArgsConstructor
public class HomePageSearchServiceImpl implements HomePageSearchService {



    private final CodeStyleTemplateRepository repository;

    /**
     * 使用参数化Repository（推荐） 主页查询的普通接口 平常状态下作为兜底查询
     */
    @Override
    public HomePageSearchPageableResultVO searchHomePage(
            String keyword,
            int page,
            int size,
            TemplateSortField sortField,
            SortOrder sortOrder,
            TimeRangeParamDTO timeRangeParamDTO) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size);
            return doHomePageSearch(keyword,pageable,sortField,sortOrder,timeRangeParamDTO);

        } catch (Exception e) {
            log.error("一般搜索也失败，触发兜底回调: {}", e.getMessage());
            return HomePageSearchPageableResultVO.createEmptyResponseVO();
        }
    }

    private HomePageSearchPageableResultVO doHomePageSearch(
            String keyword,
            Pageable pageable,
            TemplateSortField sortField,
            SortOrder sortOrder,
            TimeRangeParamDTO timeRangeParamDTO
    ){
        HomePageSearchResultDTO homePageSearchResultDTO = repository.searchByKeywordWithParams(
                keyword,
                pageable,
                sortField,
                sortOrder,
                timeRangeParamDTO
        );
        return VOConvertUtils.searchCovertToHomePageSearchVO(pageable,homePageSearchResultDTO);
    }

}
