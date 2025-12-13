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

package top.codestyle.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import top.codestyle.pojo.dto.TimeRangeParamDTO;
import top.codestyle.pojo.enums.TemplateSortField;
import top.codestyle.pojo.vo.HomePageSearchPageableResultVO;

/**
 * @author ChonghaoGao
 * @date 2025/11/9 20:04
 *       用于存放一些传统的ES检索设置
 *
 */
public interface HomePageSearchService {

    HomePageSearchPageableResultVO searchHomePage(String keyword,
                                                  int page,
                                                  int size,
                                                  TemplateSortField popularitySortField,
                                                  SortOrder sortOrder,
                                                  TimeRangeParamDTO timeRangeParamDTO);

}
