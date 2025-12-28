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

package top.codestyle.admin.search.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.codestyle.admin.search.repository.RemoteSearchESRepositoryImpl;
import top.codestyle.admin.search.service.RemoteMetaSearchService;
import top.codestyle.admin.search.service.SearchCacheService;
import top.codestyle.admin.search.vo.RemoteMetaConfigVO;

import java.util.Optional;

/**
 * 远程元搜索服务实现类
 * 使用ES的multi_match查询和聚合查询实现搜索功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RemoteMetaSearchServiceImpl implements RemoteMetaSearchService {

    private final SearchCacheService searchCacheService;

    private final RemoteSearchESRepositoryImpl remoteSearchESRepositoryImpl;

    @Override
    public Optional<RemoteMetaConfigVO> search(String query) {
        // 1. 先从缓存中获取
        Optional<RemoteMetaConfigVO> cachedResult = searchCacheService.getFromCache(query);
        if (cachedResult.isPresent()) {
            log.debug("从缓存获取搜索结果: {}", query);
            return cachedResult;
        }

        // 2. 缓存未命中，从ES查询
        // 使用Repository的searchWithAggregations方法，实现multi_match查询和聚合查询
        Optional<RemoteMetaConfigVO> result = remoteSearchESRepositoryImpl.searchWithAggregations(query);

        if (result.isEmpty()) {
            log.debug("未找到匹配关键词: {}", query);
            return Optional.empty();
        }

        // 3. 将结果存入缓存
        RemoteMetaConfigVO vo = result.get();
        searchCacheService.putToCache(query, vo);

        return result;
    }
}