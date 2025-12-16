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

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.codestyle.admin.search.model.mysql.entity.RemoteMetaDO;
import java.util.Date;
import java.util.List;

/**
 * @author ChonghaoGao
 * @date 2025/12/1 19:51
 */
@Mapper // 添加@Mapper注解，确保MyBatis能识别这个Mapper
// 使用专门为Spring Boot 3.x设计的mybatis-plus-spring-boot3-starter
public interface RemoteMetaInfoMapper extends BaseMapper<RemoteMetaDO> {
    // 继承BaseMapper，获得所有CRUD方法

    // 自定义查询方法：获取所有已删除的数据，绕过MyBatis-Plus的逻辑删除自动过滤
    List<RemoteMetaDO> selectAllDeletedData();

    // 自定义查询方法：获取所有已恢复的数据（deleted=0）
    List<RemoteMetaDO> selectAllRestoredData();

    // 自定义查询方法：获取指定时间后更新的数据，包括已删除的
    List<RemoteMetaDO> selectUpdatedDataAfterTime(Date lastSyncTime);

    // 自定义查询方法：根据ID获取数据，包括已删除的
    RemoteMetaDO selectByIdIncludeDeleted(Long id);
}
