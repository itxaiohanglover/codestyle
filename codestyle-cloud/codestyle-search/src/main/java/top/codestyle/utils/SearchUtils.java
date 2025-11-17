package top.codestyle.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import top.codestyle.entity.es.pojo.CodeStyleTemplateDO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ChonghaoGao
 * @date 2025/11/12 20:45)
 */
public class SearchUtils {
    /**
     * 将 SearchHits 转换为普通 Page 对象
     */
    public static Page<CodeStyleTemplateDO> convertToPage(SearchHits<CodeStyleTemplateDO> searchHits, Pageable pageable) {
        List<CodeStyleTemplateDO> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        long totalElements = searchHits.getTotalHits();

        return new PageImpl<>(content, pageable, totalElements);
    }
}
