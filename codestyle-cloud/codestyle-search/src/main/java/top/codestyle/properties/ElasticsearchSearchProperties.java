package top.codestyle.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "elasticsearch.search.homepage")
public class ElasticsearchSearchProperties {

    private String minimumShouldMatch;
    private Long timeoutMs;
    private Boolean trackTotalHits;
    private Integer hotScoreWeight;

    private BoostFactors boostFactors;
    private PhraseSlops phraseSlops;
    private UserActionSort userActionSort;

    @Data
    public static class BoostFactors {
        private Double nameChBoost;
        private Double nameEnBoost;
        private Double descriptionBoost;
        private Double searchTagsBoost;
    }

    @Data
    public static class PhraseSlops {
        private Integer nameChSlop;
        private Integer nameEnSlop;
        private Integer descriptionSlop;
        private Integer searchTagsSlop;
    }

    @Data
    public static class UserActionSort {
        private Double totalLikeCountWeight = 1.0;      // 默认权重
        private Double totalFavoriteCountWeight = 1.0;  // 默认权重
    }
}
