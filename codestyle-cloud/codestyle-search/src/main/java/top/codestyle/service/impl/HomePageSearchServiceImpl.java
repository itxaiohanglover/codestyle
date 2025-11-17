package top.codestyle.service.impl;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import top.codestyle.entity.es.pojo.CodeStyleTemplateDO;
import top.codestyle.entity.es.vo.HomePageSearchResultVO;
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

    /**
     * 使用参数化Repository（推荐） 主页查询的普通接口 平常状态下作为兜底查询
     */
    public Page<HomePageSearchResultVO> searchHomePage(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        try {
            return repository.searchByKeywordWithParams(
                    keyword,
                    pageable
            );

        } catch (Exception e) {
            log.error("一般搜索也失败，触发兜底回调: {}", e.getMessage());
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
    }


}
