package top.codestyle.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.codestyle.entity.es.pojo.CodeStyleTemplateDO;
import top.codestyle.entity.es.vo.HomePageSearchResultVO;
import top.codestyle.entity.es.vo.McpSearchResultVO;
import top.codestyle.service.AsyncSearchService;
import top.codestyle.service.HomePageSearchService;
import top.codestyle.service.McpSearchService;

import java.util.Collections;
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
    public ResponseEntity<Page<HomePageSearchResultVO>> codestyleSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<HomePageSearchResultVO> result = homePageSearchService.searchHomePage(keyword, page, size);
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
    public CompletableFuture<ResponseEntity<Page<HomePageSearchResultVO>>> searchAsync(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("接收到异步搜索请求 - 关键词: {}, 页码: {}, 大小: {}", keyword, page, size);
        long startTime = System.currentTimeMillis();

        return asyncSearchService.searchAsync(keyword, page, size)
                .thenApply(result -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    log.info("异步搜索完成 - 关键词: {}, 耗时: {}ms, 结果数量: {}",
                            keyword, responseTime, result.getContent().size());

                    return ResponseEntity.ok(result);
                })
                .exceptionally(throwable -> {
                    log.error("异步搜索异常 - 关键词: {}", keyword, throwable);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0));
                });
    }




}