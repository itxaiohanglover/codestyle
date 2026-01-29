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

package top.codestyle.admin.search.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.codestyle.admin.search.model.dto.CanalMessage;
import top.codestyle.admin.search.model.dto.DataChangeMessage;

import java.util.List;
import java.util.Map;

/**
 * Canal消息转换器
 * 将Canal Server发送的JSON消息格式转换为应用层的DataChangeMessage格式
 * 
 * @author chonghaoGao
 * @date 2025/12/23
 */
@Slf4j
@Component
public class CanalMessageConverter {

    /**
     * 将Canal消息转换为DataChangeMessage
     * 
     * @param canalMessage Canal Server发送的消息
     * @return DataChangeMessage列表（一条Canal消息可能包含多条数据变更）
     */
    public List<DataChangeMessage> convert(CanalMessage canalMessage) {
        if (canalMessage == null) {
            return java.util.Collections.emptyList();
        }

        String operationType = mapOperationType(canalMessage.getType());
        String database = canalMessage.getDatabase();
        String table = canalMessage.getTable();

        List<Map<String, Object>> dataList = canalMessage.getData();
        List<Map<String, Object>> oldList = canalMessage.getOld();

        if (dataList == null || dataList.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        return dataList.stream().map(data -> {
            DataChangeMessage message = new DataChangeMessage();
            message.setOperationType(operationType);
            message.setDatabase(database);
            message.setTable(table);
            message.setAfterData(data);

            // 设置变更前的数据
            if (oldList != null && !oldList.isEmpty()) {
                // 找到对应的old数据（通常索引对应）
                int index = dataList.indexOf(data);
                if (index < oldList.size()) {
                    message.setBeforeData(oldList.get(index));
                }
            }

            // 提取主键
            Object primaryKey = extractPrimaryKey(data, canalMessage.getPkNames());
            message.setPrimaryKey(primaryKey);

            // 设置时间戳
            message.setTimestamp(canalMessage.getTs() != null ? canalMessage.getTs() : System.currentTimeMillis());

            // 生成消息ID
            String messageId = generateMessageId(database, table, primaryKey, message.getTimestamp());
            message.setMessageId(messageId);

            return message;
        }).collect(java.util.stream.Collectors.toList());
    }

    /**
     * 映射操作类型
     * 
     * @param canalType Canal消息类型
     * @return 应用层操作类型
     */
    private String mapOperationType(String canalType) {
        if (canalType == null) {
            return "UNKNOWN";
        }

        switch (canalType.toUpperCase()) {
            case "INSERT":
                return "INSERT";
            case "UPDATE":
                return "UPDATE";
            case "DELETE":
                return "DELETE";
            default:
                log.warn("未知的Canal操作类型: {}", canalType);
                return canalType;
        }
    }

    /**
     * 提取主键值
     * 
     * @param data    数据Map
     * @param pkNames 主键字段名列表
     * @return 主键值（如果是复合主键，则拼接）
     */
    private Object extractPrimaryKey(Map<String, Object> data, List<String> pkNames) {
        if (pkNames != null && !pkNames.isEmpty()) {
            if (pkNames.size() == 1) {
                // 单主键
                return data.get(pkNames.get(0));
            } else {
                // 复合主键，拼接
                return pkNames.stream().map(pkName -> {
                    Object value = data.get(pkName);
                    return value != null ? value.toString() : "";
                }).collect(java.util.stream.Collectors.joining("_"));
            }
        }

        // 如果没有主键信息，尝试从常见字段获取
        if (data.containsKey("id")) {
            return data.get("id");
        }
        if (data.containsKey("uid")) {
            return data.get("uid");
        }

        // 返回第一个字段的值作为备选
        if (!data.isEmpty()) {
            return data.values().iterator().next();
        }

        return null;
    }

    /**
     * 生成消息ID
     * 
     * @param database   数据库名
     * @param table      表名
     * @param primaryKey 主键值
     * @param timestamp  时间戳
     * @return 消息ID
     */
    private String generateMessageId(String database, String table, Object primaryKey, long timestamp) {
        return String.format("%s.%s.%s.%d", database != null ? database : "unknown", table != null
            ? table
            : "unknown", primaryKey != null ? primaryKey : "unknown", timestamp);
    }
}
