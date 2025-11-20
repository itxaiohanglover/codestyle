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

package top.codestyle.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import top.codestyle.model.entity.StorageDO;
import top.continew.starter.data.mapper.BaseMapper;

import java.util.List;

/**
 * 存储 Mapper
 *
 * @author Charles7c
 * @since 2023/12/26 22:09
 */
@Mapper
public interface StorageMapper extends BaseMapper<StorageDO> {

    /**
     * 根据 ID 查询
     *
     * @param longs ID 列表
     * @return 存储列表
     */
    @Select("SELECT * FROM file_storage WHERE id IN (#{ids})")
    List<StorageDO> selectByIds(List<Long> longs);
}