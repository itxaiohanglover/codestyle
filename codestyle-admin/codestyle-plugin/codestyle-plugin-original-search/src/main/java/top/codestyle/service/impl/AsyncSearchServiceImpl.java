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

package top.codestyle.service.impl;

import co.elastic.clients.elasticsearch._types.SortOrder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.codestyle.pojo.dto.HomePageSearchResultDTO;
import top.codestyle.pojo.dto.TimeRangeParamDTO;
import top.codestyle.pojo.enums.TemplateSortField;
import top.codestyle.pojo.vo.HomePageSearchPageableResultVO;
import top.codestyle.repository.CodeStyleTemplateRepository;
import top.codestyle.service.AsyncSearchService;
import top.codestyle.utils.VOConvertUtils;

import java.util.concurrent.CompletableFuture;

/**
 * @author ChonghaoGao
 * @date 2025/11/12 22:04
 *
 *       实验级原型接口 用于解决单端并发问题 实现异步化搜索能力增强单节点搜索的能力
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

    @Async("searchExecutor")
    public CompletableFuture<HomePageSearchPageableResultVO> searchAsync(String keyword,
                                                                         int page,
                                                                         int size,
                                                                         TemplateSortField sortField,
                                                                         SortOrder sortOrder,
                                                                         TimeRangeParamDTO timeRangeParamDTO) {

        try {
            Pageable pageable = PageRequest.of(page - 1, size);
            return CompletableFuture
                .completedFuture(doHomePageSearch(keyword, pageable, sortField, sortOrder, timeRangeParamDTO));

        } catch (Exception e) {
            log.error("一般搜索也失败，触发兜底回调: {}", e.getMessage());
            return CompletableFuture.completedFuture(HomePageSearchPageableResultVO.createEmptyResponseVO());
        }
    }

    private HomePageSearchPageableResultVO doHomePageSearch(String keyword,
                                                            Pageable pageable,
                                                            TemplateSortField sortField,
                                                            SortOrder sortOrder,
                                                            TimeRangeParamDTO timeRangeParamDTO) {
        HomePageSearchResultDTO homePageSearchResultDTO = repository
            .searchByKeywordWithParams(keyword, pageable, sortField, sortOrder, timeRangeParamDTO);
        return VOConvertUtils.searchCovertToHomePageSearchVO(pageable, homePageSearchResultDTO);
    }

    @PostConstruct
    private void init() {
        if (!useConnectionInitQuery)
            return;
        log.info("进行异步初始化预热搜索，检索关键词为:{}", preHeatWord);
        long startTime = System.currentTimeMillis();
        searchAsync(preHeatWord, 1, 10, TemplateSortField.HOT_SCORE, SortOrder.Desc, null).thenApply(result -> {
            long responseTime = System.currentTimeMillis() - startTime;
            log.info("预热异步搜索完成 - 关键词: {}, 耗时: {}ms, 结果数量: {}", preHeatWord, responseTime, result.getPage()
                .getContent()
                .size());

            return ResponseEntity.ok(result);
        }).exceptionally(throwable -> {
            log.error("预热异步搜索异常 - 关键词: {}", preHeatWord, throwable);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(HomePageSearchPageableResultVO.createEmptyResponseVO());
        });
        ;
    }

}
