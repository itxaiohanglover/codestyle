/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this fileInfo except in compliance with the License.
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

package top.codestyle.admin.search.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import top.codestyle.admin.search.entity.SearchEntity;
import top.codestyle.admin.search.vo.RemoteMetaConfigVO;

import java.util.Optional;

/**
 * Elasticsearch搜索仓库接口
 * 
 * @author ChonghaoGao
 * @date 2025/12/22
 */
@Repository
public interface RemoteSearchESRepository extends ElasticsearchRepository<SearchEntity, String> {
    /**
     * 执行带聚合的搜索查询
     * 
     * @param query 搜索关键词
     * @return 搜索结果VO，包含聚合信息
     */
    Optional<RemoteMetaConfigVO> searchWithAggregations(String query);
}