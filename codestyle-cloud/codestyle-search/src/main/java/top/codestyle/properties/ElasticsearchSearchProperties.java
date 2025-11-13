package top.codestyle.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;


@ConfigurationProperties(prefix = "elasticsearch.search.homepage")
@Data
public class ElasticsearchSearchProperties {
    
    // 基础搜索配置
    private String minimumShouldMatch = "75%";
    private Long timeoutMs = 400L;
    private Integer terminateAfter = 5000;
    private Boolean trackTotalHits = false;

    // 权重配置
    private BoostFactors boostFactors = new BoostFactors();
    private PhraseSlops phraseSlops = new PhraseSlops();
    private List<String> sourceIncludes = new ArrayList<>();
    
    @Data
    public static class BoostFactors {
        private Double fileNameBoostFactor = 2.0;
        private Double fileDescriptionBoostFactor = 8.0;
        private Double projectDescriptionBoostFactor = 5.0;
    }

    @Data
    public static class PhraseSlops {
        private Integer fileNameSlop = 1;
        private Integer fileDescriptionSlop = 2;
        private Integer projectDescriptionSlop = 2;
    }
}