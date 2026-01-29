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

package top.codestyle.admin.search.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * 
 * 远程元配置数据实体
 * 注意：与search模块保持一致，使用remote_meta_info表
 * 
 * @author ChonghaoGao
 * @date 2025/12/21
 */
@Data
@TableName("remote_meta_info")
public class RemoteMetaDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 元数据JSON
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String metaJson;

    /**
     * 组ID（虚拟列，由数据库从meta_json中提取）
     */
    @TableField(exist = false)
    private String groupId;

    /**
     * 构件ID（从meta_json中提取）
     */
    @TableField(exist = false)
    private String artifactId;

    /**
     * 描述（从meta_json中提取）
     */
    @TableField(exist = false)
    private String description;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除标记
     */
    private Boolean deleted;

    /**
     * 创建用户
     */
    private Long createUser;

    /**
     * 更新用户
     */
    private Long updateUser;
}