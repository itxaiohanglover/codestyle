package top.codestyle.service.impl;


import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import top.codestyle.entity.es.CodeStyleTemplate;
import top.codestyle.properties.ElasticsearchSearchProperties;
import top.codestyle.repository.CodeStyleTemplateRepository;
import top.codestyle.service.HomePageSearchService;

import java.util.Collections;


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

    private final ElasticsearchSearchProperties properties;

    /**
     * 使用参数化Repository（推荐） 主页查询的普通接口 平常状态下作为兜底查询
     */
    public Page<CodeStyleTemplate> searchHomePage(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        try {
//            System.out.println(keyword);
//            System.out.println(properties.toString());
            return repository.searchByKeywordWithParams(
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
            );

        } catch (Exception e) {
            log.error("一般搜索也失败，触发兜底回调: {}", e.getMessage());
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
    }


}
