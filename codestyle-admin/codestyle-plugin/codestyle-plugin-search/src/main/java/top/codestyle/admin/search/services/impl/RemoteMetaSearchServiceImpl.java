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

package top.codestyle.admin.search.services.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.codestyle.admin.search.respository.es.RemoteSearchESRepository;
import top.codestyle.admin.search.services.RemoteMetaSearchService;
import top.codestyle.admin.search.vo.RemoteMetaConfigVO;

import java.util.Optional;

/**
 * @author ChonghaoGao
 * @date 2025/12/1 19:06)
 */
@Slf4j
@Service
@AllArgsConstructor
public class RemoteMetaSearchServiceImpl implements RemoteMetaSearchService {

    private final RemoteSearchESRepository repository;

    @Override
    public Optional<RemoteMetaConfigVO> search(String query) {
        try {
            return repository.searchInES(query);
        } catch (Exception e) {
            log.info("检索异常:{},尝试返回兜底数据", e.getMessage());
        }
        return Optional.empty();
    }
}
