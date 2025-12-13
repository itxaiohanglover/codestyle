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

package top.codestyle.pojo.dto;

import lombok.Data;
import org.springframework.data.domain.Page;
import java.util.List;

/**
 * @author ChonghaoGao
 * @date 2025/11/19 15:30)
 */
@Data
public class PageResponse<T> {
    private int page;          // 1-based 页码
    private int size;
    private long totalElements;
    private int totalPages;
    private List<T> content;
    private boolean first;
    private boolean last;

    public PageResponse(Page<T> pageData) {
        this.page = pageData.getNumber() + 1;  // 转换为 1-based
        this.size = pageData.getSize();
        this.totalElements = pageData.getTotalElements();
        this.totalPages = pageData.getTotalPages();
        this.content = pageData.getContent();
        this.first = pageData.isFirst();
        this.last = pageData.isLast();
    }

    // getter/setter ...
}
