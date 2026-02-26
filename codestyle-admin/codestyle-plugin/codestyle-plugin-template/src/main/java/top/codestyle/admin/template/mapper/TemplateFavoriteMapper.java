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

package top.codestyle.admin.template.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.codestyle.admin.template.model.entity.TemplateFavoriteDO;
import top.continew.starter.data.mapper.BaseMapper;

import java.util.List;

public interface TemplateFavoriteMapper extends BaseMapper<TemplateFavoriteDO> {

    @Delete("DELETE FROM template_favorite WHERE template_id = #{templateId} AND user_id = #{userId}")
    int physicalDelete(@Param("templateId") Long templateId, @Param("userId") Long userId);

    @Delete("DELETE FROM template_favorite WHERE template_id = #{templateId}")
    int physicalDeleteByTemplateId(@Param("templateId") Long templateId);

    @Select("SELECT template_id FROM template_favorite WHERE user_id = #{userId} AND deleted = 0")
    List<Long> selectFavoriteTemplateIds(@Param("userId") Long userId);
}
