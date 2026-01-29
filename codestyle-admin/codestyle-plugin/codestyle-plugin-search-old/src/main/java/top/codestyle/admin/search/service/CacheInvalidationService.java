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

import java.util.Set;

/**
 * 缓存失效服务接口
 * 
 * @author chonghaoGao
 * @date 2025/12/23
 */
public interface CacheInvalidationService {

    /**
     * 基于关键词的缓存失效
     * 
     * @param keyword 关键词
     */
    void invalidateByKeyword(String keyword);

    /**
     * 基于字段的缓存失效
     * 
     * @param field 字段名
     * @param value 字段值
     */
    void invalidateByField(String field, String value);

    /**
     * 批量失效缓存
     * 
     * @param patterns 缓存键模式列表
     */
    void batchInvalidateByPatterns(Set<String> patterns);

    /**
     * 失效所有搜索缓存
     */
    void invalidateAll();

    /**
     * 基于语义分析的智能缓存失效
     * 
     * @param content 变更内容
     */
    void intelligentInvalidate(String content);
}
