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

package top.codestyle.admin.search.spi;

import top.codestyle.admin.search.model.SearchRequest;
import top.codestyle.admin.search.model.SearchResult;
import top.codestyle.admin.search.model.SearchSourceType;

import java.util.List;

/**
 * Wiki 数据源检索示例
 * <p>
 * 自定义数据源实现 SearchProvider 接口，
 * 通过 SPI 注册即可自动被 SearchExecutor 发现和使用
 *
 * @author CodeStyle Team
 * @since 2.0.0
 */
public class WikiSearchProvider implements SearchProvider {

    @Override
    public boolean supports(SearchSourceType type) {
        return SearchSourceType.CUSTOM == type;
    }

    @Override
    public List<SearchResult> search(SearchRequest request) {
        // 实现自定义检索逻辑
        // 例如：调用 Wiki API、数据库查询等

        // 示例：返回模拟数据
        return List.of(SearchResult.builder()
            .id("wiki-001")
            .sourceType(SearchSourceType.CUSTOM)
            .title("Wiki 文档示例")
            .content("这是来自 Wiki 的检索结果")
            .score(0.95)
            .build());
    }

    @Override
    public String getName() {
        return "WikiSearchProvider";
    }

    @Override
    public int getPriority() {
        return 50; // 较高优先级
    }
}
