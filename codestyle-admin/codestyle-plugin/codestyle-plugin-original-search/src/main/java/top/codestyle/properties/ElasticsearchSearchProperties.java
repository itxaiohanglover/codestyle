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

package top.codestyle.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "elasticsearch.search.properties")
public class ElasticsearchSearchProperties {

    private String minimumShouldMatch;
    private Long timeoutMs;
    private Boolean trackTotalHits;
    private Integer hotScoreWeight;

    private BoostFactors boostFactors;
    private PhraseSlops phraseSlops;
    private UserActionSort userActionSort;

    private VersionSortConfig versionSortConfig;
    private QueryConfig queryConfig;

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

    @Data
    public static class VersionSortConfig {
        private Integer maxCandidateResults;
        private Boolean strictVersionComparison;
        private String versionFieldName;
        private String defaultVersion;
    }

    @Data
    public static class QueryConfig {
        private Boolean searchOnlyPublicTemplates = true;
        private Boolean includeDeletedTemplates = false;
        private Integer aggTopKth = 1;
    }
}
