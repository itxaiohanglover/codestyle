package top.codestyle.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import top.codestyle.entity.es.CodeStyleTemplate;
import top.codestyle.properties.ElasticsearchSearchProperties;
import top.codestyle.repository.CodeStyleTemplateRepository;
import top.codestyle.service.AsyncSearchService;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author ChonghaoGao
 * @date 2025/11/12 22:04
 *
 * 实验级原型接口 用于解决单端并发问题 实现异步化搜索能力增强单节点搜索的能力
 */
@Slf4j
@Service

public class AsyncSearchServiceImpl implements AsyncSearchService {

    @Value("${elasticsearch.search.use-connection-init-query}")
    private Boolean useConnectionInitQuery;

    @Value("${elasticsearch.search.pre-heat-word}")
    private String preHeatWord;

    @Autowired
    private CodeStyleTemplateRepository repository;

    @Autowired
    private ElasticsearchSearchProperties properties;

    @Async("searchExecutor")
    public CompletableFuture<Page<CodeStyleTemplate>> searchAsync(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        try {

            return CompletableFuture.completedFuture(repository.searchByKeywordWithParams(
                    keyword,
                    properties.getMinimumShouldMatch(),
                    properties.getPhraseSlops().getFileNameSlop(),
                    properties.getPhraseSlops().getFileDescriptionSlop(),
                    properties.getPhraseSlops().getProjectDescriptionSlop(),
                    properties.getBoostFactors().getFileNameBoostFactor(),
                    properties.getBoostFactors().getFileDescriptionBoostFactor(),
                    properties.getBoostFactors().getProjectDescriptionBoostFactor(),
                    properties.getTimeoutMs(),
                    properties.getTrackTotalHits(),
                    properties.getSourceIncludes().toArray(String[]::new),
                    pageable
            ));

        } catch (Exception e) {
            log.error("一般搜索也失败，触发兜底回调: {}", e.getMessage());
            return CompletableFuture.completedFuture(new PageImpl<>(Collections.emptyList(), pageable, 0));
        }
    }

    @PostConstruct
    private void init(){
        if(!useConnectionInitQuery) return;
        log.info("进行异步初始化预热搜索，检索关键词为:{}",preHeatWord);
        long startTime = System.currentTimeMillis();
        searchAsync(preHeatWord,0,10).thenApply(result -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    log.info("预热异步搜索完成 - 关键词: {}, 耗时: {}ms, 结果数量: {}",
                            preHeatWord, responseTime, result.getContent().size());

                    return ResponseEntity.ok(result);
                })
                .exceptionally(throwable -> {
                    log.error("预热异步搜索异常 - 关键词: {}", preHeatWord, throwable);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));
                });;
    }

}
