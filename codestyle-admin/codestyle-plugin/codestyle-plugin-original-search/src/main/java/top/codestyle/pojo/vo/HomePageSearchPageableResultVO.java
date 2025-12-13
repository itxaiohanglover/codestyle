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

package top.codestyle.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import top.codestyle.pojo.dto.PageResponse;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomePageSearchPageableResultVO {

    private PageResponse<HomePageSearchDocResultVO> page;

    private List<String> topTags; // 聚合得到的标签

    public static HomePageSearchPageableResultVO createEmptyResponseVO() {
        PageImpl<HomePageSearchDocResultVO> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest
            .of(0, 1), 0);
        return new HomePageSearchPageableResultVO(new PageResponse<>(emptyPage), null);
    }

    public static HomePageSearchPageableResultVO createResponseVO(List<HomePageSearchDocResultVO> docResultVOList,
                                                                  Pageable pageable,
                                                                  List<String> topTags,
                                                                  long totalHits) {

        PageImpl<HomePageSearchDocResultVO> page = new PageImpl<>(docResultVOList, pageable, totalHits);
        //二次封装以便是以1-based的分页尺度
        PageResponse<HomePageSearchDocResultVO> pageVO = new PageResponse<>(page);
        return new HomePageSearchPageableResultVO(pageVO, topTags);
    }
}
