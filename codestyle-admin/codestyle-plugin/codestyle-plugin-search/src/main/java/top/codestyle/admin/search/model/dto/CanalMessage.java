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

package top.codestyle.admin.search.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author chonghaoGao
 * @date 2025/12/23
 */
@Data
public class CanalMessage {

    /**
     * 操作类型：INSERT/UPDATE/DELETE
     */
    private String type;

    /**
     * 数据库名称
     */
    private String database;

    /**
     * 表名
     */
    private String table;

    /**
     * 变更后的数据（数组，每个元素是一条记录）
     */
    private List<Map<String, Object>> data;

    /**
     * 变更前的数据（UPDATE/DELETE时才有）
     */
    private List<Map<String, Object>> old;

    /**
     * 时间戳
     */
    private Long ts;

    /**
     * 是否为DDL操作
     */
    @JsonProperty("isDdl")
    private Boolean isDdl;

    /**
     * MySQL字段类型映射
     */
    private Map<String, String> mysqlType;

    /**
     * SQL类型映射
     */
    private Map<String, Integer> sqlType;

    /**
     * 主键字段名列表
     */
    private List<String> pkNames;

    /**
     * SQL语句（DDL时才有）
     */
    private String sql;

    /**
     * 事件时间戳（秒）
     */
    private Long es;

    /**
     * 消息ID
     */
    private Long id;
}
