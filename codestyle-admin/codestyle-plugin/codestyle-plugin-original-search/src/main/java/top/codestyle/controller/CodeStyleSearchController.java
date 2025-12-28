package top.codestyle.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.codestyle.entity.es.vo.McpSearchResultVO;
import top.codestyle.pojo.dto.TimeRangeParamDTO;
import top.codestyle.pojo.enums.TemplateDateRangeFilter;
import top.codestyle.pojo.enums.TemplateSortField;
import top.codestyle.pojo.enums.TemplateSortOrderType;
import top.codestyle.pojo.vo.HomePageSearchPageableResultVO;
import top.codestyle.service.AsyncSearchService;
import top.codestyle.service.HomePageSearchService;
import top.codestyle.service.McpSearchService;

import java.util.concurrent.CompletableFuture;


/**
 * @author ChonghaoGao
 * @date 2025/11/9 19:58
 * 搜索接controller 含有主页搜索还有未来的mcp搜索
 */
@Slf4j
@RestController
@RequestMapping("/codestyle")
@AllArgsConstructor
public class CodeStyleSearchController {


    private final HomePageSearchService homePageSearchService;
    private final AsyncSearchService asyncSearchService;

    /**
     * 传统主页检索接口 - codestyleSearch
     * @param keyword Search keyword
     * @param page Page number (default 0)
     * @param size Page size (default 10)
     * @return Simplified template list
     */
    @GetMapping("/search")
    public ResponseEntity<HomePageSearchPageableResultVO> codestyleSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,

            // 排序字段（可选）
            @RequestParam(required = false, defaultValue = "HOT_SCORE")
            String sortField,

            // 排序方式（升序/降序）
            @RequestParam(required = false, defaultValue = "DESC")
            String sortOrder,

            // 时间过滤类型（默认 NONE）
            @RequestParam(required = false, defaultValue = "NONE")
            String timeRange,

            // 自定义时间（仅在 range=CUSTOM 时有效）
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime
    ) {

        TimeRangeParamDTO timeRangeParamDTO = new TimeRangeParamDTO();
        timeRangeParamDTO.setRangeType(TemplateDateRangeFilter.from(timeRange));

        if (timeRangeParamDTO.getRangeType() == TemplateDateRangeFilter.CUSTOM) {
            timeRangeParamDTO.setStartTime(startTime);
            timeRangeParamDTO.setEndTime(endTime);
        }

       HomePageSearchPageableResultVO result = homePageSearchService.searchHomePage(
                keyword,
                page,
                size,
                TemplateSortField.from(sortField),
                TemplateSortOrderType.fromString(sortOrder),
                timeRangeParamDTO
        );

        return ResponseEntity.ok(result);
    }




    /**
     * 异步搜索接口 - 立即返回，不阻塞线程
     *  @param keyword Search keyword
     *  @param page Page number (default 0)
     *  @param size Page size (default 10)
     *  @return Simplified template list in CompletableFuture
     */
    @GetMapping("/async_search")
    public CompletableFuture<ResponseEntity<HomePageSearchPageableResultVO>> searchAsync(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,

            // 排序字段（可选）
            @RequestParam(required = false, defaultValue = "HOT_SCORE")
            String sortField,

            // 排序方式（升序/降序）
            @RequestParam(required = false, defaultValue = "DESC")
            String sortOrder,

            // 时间过滤类型（默认 NONE）
            @RequestParam(required = false, defaultValue = "NONE")
            String range,

            // 自定义时间（仅在 range=CUSTOM 时有效）
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime)  {

        log.info("接收到异步搜索请求 - 关键词: {}, 页码: {}, 大小: {}", keyword, page, size);
        long stTime = System.currentTimeMillis();
        TimeRangeParamDTO timeRangeParamDTO = new TimeRangeParamDTO();
        timeRangeParamDTO.setRangeType(TemplateDateRangeFilter.from(range));

        if (timeRangeParamDTO.getRangeType() == TemplateDateRangeFilter.CUSTOM) {
            timeRangeParamDTO.setStartTime(startTime);
            timeRangeParamDTO.setEndTime(endTime);
        }
        return asyncSearchService.searchAsync(
                        keyword,
                        page,
                        size,
                        TemplateSortField.from(sortField),
                        TemplateSortOrderType.fromString(sortOrder),
                        timeRangeParamDTO
                ).thenApply(result -> {
                    long responseTime = System.currentTimeMillis() - stTime;
                    log.info("异步搜索完成 - 关键词: {}, 耗时: {}ms, 结果数量: {}",
                            keyword, responseTime, result.getPage().getContent().size());

                    return ResponseEntity.ok(result);
                })
                .exceptionally(throwable -> {
                    log.error("异步搜索异常 - 关键词: {}", keyword, throwable);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(HomePageSearchPageableResultVO.createEmptyResponseVO());
                });
    }




}