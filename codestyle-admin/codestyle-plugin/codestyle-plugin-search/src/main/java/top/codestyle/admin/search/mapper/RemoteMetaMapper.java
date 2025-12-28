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

package top.codestyle.admin.search.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.codestyle.admin.search.entity.RemoteMetaDO;
import top.continew.starter.data.mapper.BaseMapper;

import java.util.List;

/**
 * 
 * 远程元配置Mapper接口
 * 
 * @author ChonghaoGao
 * @date 2025/12/22
 */
@Mapper
public interface RemoteMetaMapper extends BaseMapper<RemoteMetaDO> {

    /**
     * 查询所有未删除的远程元配置
     * 
     * @return 远程元配置列表
     */
    List<RemoteMetaDO> selectAllActive();

    /**
     * 根据ID查询远程元配置
     * 
     * @param id 主键ID
     * @return 远程元配置
     */
    RemoteMetaDO selectById(@Param("id") Long id);

    /**
     * 根据group_id虚拟列查询远程元配置
     * 
     * @param groupId 组ID
     * @return 远程元配置列表
     */
    List<RemoteMetaDO> selectByGroupId(@Param("groupId") String groupId);

    /**
     * 根据ID列表批量查询
     * 
     * @param ids ID列表
     * @return 远程元配置列表
     */
    List<RemoteMetaDO> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 批量插入
     * 
     * @param list 远程元配置列表
     * @return 插入数量
     */
    int batchInsert(@Param("list") List<RemoteMetaDO> list);
}