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

package top.codestyle.admin.search.service;

import top.codestyle.admin.search.vo.RemoteMetaConfigVO;

import java.util.Optional;
import java.util.Iterator;

public interface SearchCacheService {
    /**
     * 从缓存中获取搜索结果
     * 
     * @param query 搜索关键词
     * @return 搜索结果
     */
    Optional<RemoteMetaConfigVO> getFromCache(String query);

    /**
     * 将搜索结果存入缓存
     * 
     * @param query  搜索关键词
     * @param result 搜索结果
     */
    void putToCache(String query, RemoteMetaConfigVO result);

    /**
     * 从缓存中删除搜索结果
     * 
     * @param query 搜索关键词
     */
    void removeFromCache(String query);

    /**
     * 批量删除缓存
     * 
     * @param queries 搜索关键词列表
     */
    void batchRemoveFromCache(Iterator<String> queries);
}