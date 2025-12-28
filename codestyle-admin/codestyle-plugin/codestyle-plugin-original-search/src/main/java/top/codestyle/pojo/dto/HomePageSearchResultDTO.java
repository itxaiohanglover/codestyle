package top.codestyle.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.core.SearchHit;
import top.codestyle.pojo.entity.CodeStyleTemplateDO;

import java.util.List;

/**
 * @author ChonghaoGao
 * @date 2025/11/19 20:09)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HomePageSearchResultDTO {

    private long totalHits = 0;

    private List<SearchHit<CodeStyleTemplateDO>> searchHitList;

    private List<String> aggTags;

}
