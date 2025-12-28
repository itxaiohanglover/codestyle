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

package top.codestyle.admin.search.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class SearchUtil {

    /**
     * 创建Pageable对象
     * 
     * @param pageNum       页码（从1开始）
     * @param pageSize      每页大小
     * @param sortField     排序字段
     * @param sortDirection 排序方向
     * @return Pageable对象
     */
    public static Pageable createPageable(Integer pageNum, Integer pageSize, String sortField, String sortDirection) {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100; // 限制最大分页大小
        }

        Pageable pageable;
        if (sortField != null && !sortField.isEmpty()) {
            Sort.Direction direction = Sort.Direction.ASC;
            if ("desc".equalsIgnoreCase(sortDirection)) {
                direction = Sort.Direction.DESC;
            }
            pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(direction, sortField));
        } else {
            pageable = PageRequest.of(pageNum - 1, pageSize);
        }

        return pageable;
    }

    /**
     * 清理搜索关键词
     * 
     * @param keyword 原始关键词
     * @return 清理后的关键词
     */
    public static String cleanKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        // 去除首尾空格
        keyword = keyword.trim();
        // 替换特殊字符（保留常见的中文字符、英文字母、数字和一些标点）
        return keyword.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9\\s._-]", " ").replaceAll("\\s+", " ").trim();
    }
}