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

package top.codestyle.admin.system.service;

import top.codestyle.admin.common.base.service.BaseService;
import top.codestyle.admin.system.model.entity.DeptDO;
import top.codestyle.admin.system.model.query.DeptQuery;
import top.codestyle.admin.system.model.req.DeptReq;
import top.codestyle.admin.system.model.resp.DeptResp;
import top.continew.starter.data.service.IService;

import java.util.List;
import java.util.Set;

/**
 * 部门业务接口
 *
 * @author Charles7c
 * @since 2023/1/22 17:54
 */
public interface DeptService extends BaseService<DeptResp, DeptResp, DeptQuery, DeptReq>, IService<DeptDO> {

    /**
     * 查询子部门列表
     *
     * @param id ID
     * @return 子部门列表
     */
    List<DeptDO> listChildren(Long id);

    /**
     * 通过名称查询部门
     *
     * @param list 名称列表
     * @return 部门列表
     */
    List<DeptDO> listByNames(List<String> list);

    /**
     * 通过名称查询部门数量
     *
     * @param deptNames 名称列表
     * @return 部门数量
     */
    int countByNames(Set<String> deptNames);
}
