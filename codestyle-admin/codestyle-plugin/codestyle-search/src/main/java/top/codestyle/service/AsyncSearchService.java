package top.codestyle.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import top.codestyle.pojo.dto.TimeRangeParamDTO;
import top.codestyle.pojo.enums.TemplateSortField;
import top.codestyle.pojo.vo.HomePageSearchPageableResultVO;

import java.util.concurrent.CompletableFuture;

/**
 * @author ChonghaoGao
 * @date 2025/11/12 21:59
 * 实验级原型接口 用于解决单端并发问题 实现异步化搜索能力增强单节点搜索的能力
 */
public interface AsyncSearchService {

    CompletableFuture<HomePageSearchPageableResultVO> searchAsync(
            String keyword,
            int page,
            int size,
            TemplateSortField sortField,
            SortOrder sortOrder,
            TimeRangeParamDTO timeRangeParamDTO);
}
