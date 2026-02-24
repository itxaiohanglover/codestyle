/*
 * Copyright (c) 2022-present CodeStyle Authors. All Rights Reserved.
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
 * 检索提供者 SPI 接口
 * <p>
 * 用于支持运行时动态注册新的数据源检索能力
 * 自定义数据源实现此接口并通过 SPI 注册即可被自动发现和使用
 *
 * @author CodeStyle Team
 * @since 2.0.0
 */
public interface SearchProvider {

    /**
     * 判断是否支持指定的数据源类型
     *
     * @param type 数据源类型
     * @return 是否支持
     */
    boolean supports(SearchSourceType type);

    /**
     * 执行检索
     *
     * @param request 检索请求
     * @return 检索结果列表
     */
    List<SearchResult> search(SearchRequest request);

    /**
     * 获取 Provider 优先级（数值越小优先级越高）
     * <p>
     * 当多个 Provider 支持同一类型时，使用优先级最高的
     *
     * @return 优先级，默认 100
     */
    default int getPriority() {
        return 100;
    }

    /**
     * 获取 Provider 名称，用于日志和监控
     *
     * @return Provider 名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
