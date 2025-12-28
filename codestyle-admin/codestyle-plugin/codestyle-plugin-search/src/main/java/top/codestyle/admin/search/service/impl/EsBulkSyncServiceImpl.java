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

package top.codestyle.admin.search.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import top.codestyle.admin.search.config.BulkProcessorConfig.CustomBulkProcessor;
import top.codestyle.admin.search.model.dto.DataChangeMessage;
import top.codestyle.admin.search.service.EsBulkSyncService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * Elasticsearch批量同步服务实现类
 * 
 * @author ChonghaoGao
 * @date 2025/12/22
 */
@Service("esBulkSyncService")
@RequiredArgsConstructor
@ConditionalOnClass(name = "org.springframework.data.elasticsearch.core.ElasticsearchOperations")
public class EsBulkSyncServiceImpl implements EsBulkSyncService {

    private static final Logger logger = LoggerFactory.getLogger(EsBulkSyncServiceImpl.class);

    private final CustomBulkProcessor bulkProcessor;
    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 表名到索引名的映射缓存
     */
    private final Map<String, String> tableToIndexMapping = new ConcurrentHashMap<>();

    /**
     * 处理数据变更消息
     * 
     * @param message 数据变更消息
     */
    @Override
    public void processDataChangeMessage(DataChangeMessage message) {
        logger.info("========================================");
        logger.info("EsBulkSyncService 开始处理数据变更消息");
        logger.info("========================================");
        logger.info("Message ID: {}", message.getMessageId());
        logger.info("Operation: {}", message.getOperationType());
        logger.info("Database: {}", message.getDatabase());
        logger.info("Table: {}", message.getTable());
        logger.info("Primary Key: {}", message.getPrimaryKey());
        logger.info("After Data: {}", message.getAfterData());
        logger.info("Before Data: {}", message.getBeforeData());

        try {
            String indexName = getIndexName(message.getDatabase(), message.getTable());
            String documentId = String.valueOf(message.getPrimaryKey());

            logger.info("Index Name: {}", indexName);
            logger.info("Document ID: {}", documentId);

            switch (message.getOperationType()) {
                case "INSERT":
                    logger.info("执行INSERT操作: index={}, id={}", indexName, documentId);
                    processInsert(indexName, documentId, message.getAfterData());
                    logger.info("INSERT操作完成: index={}, id={}", indexName, documentId);
                    break;
                case "UPDATE":
                    // 检查是否是逻辑删除
                    if (isLogicalDelete(message.getAfterData())) {
                        // 逻辑删除：从ES中删除文档
                        logger.info("检测到逻辑删除: index={}, id={}", indexName, documentId);
                        processDelete(indexName, documentId);
                        logger.info("逻辑删除完成: index={}, id={}", indexName, documentId);
                    } else {
                        // 普通更新：执行upsert
                        logger.info("执行UPDATE操作: index={}, id={}", indexName, documentId);
                        processUpdate(indexName, documentId, message.getAfterData());
                        logger.info("UPDATE操作完成: index={}, id={}", indexName, documentId);
                    }
                    break;
                case "DELETE":
                    logger.info("执行DELETE操作: index={}, id={}", indexName, documentId);
                    processDelete(indexName, documentId);
                    logger.info("DELETE操作完成: index={}, id={}", indexName, documentId);
                    break;
                default:
                    logger.warn("未处理的操作类型: {}", message.getOperationType());
            }

            logger.info("========================================");
            logger.info("EsBulkSyncService 数据变更消息处理完成");
            logger.info("========================================");

        } catch (Exception e) {
            logger.error("处理数据变更消息时发生错误", e);
            logger.error("错误详情: messageId={}, operation={}, table={}.{}", message.getMessageId(), message
                .getOperationType(), message.getDatabase(), message.getTable());
            throw e; // 重新抛出异常，让调用者处理
        }
    }

    /**
     * 处理插入操作
     */
    private void processInsert(String indexName, String documentId, Map<String, Object> data) {
        bulkProcessor.add(data, indexName, documentId);
    }

    /**
     * 处理更新操作
     * 使用bulkProcessor的add方法实现upsert（如果不存在则插入，存在则更新）
     */
    private void processUpdate(String indexName, String documentId, Map<String, Object> data) {
        // UPDATE操作在ES中就是upsert，使用bulkProcessor的add方法即可
        // add方法会自动处理：如果文档不存在则插入，存在则更新
        bulkProcessor.add(data, indexName, documentId);
    }

    /**
     * 处理删除操作
     */
    private void processDelete(String indexName, String documentId) {
        elasticsearchOperations.delete(documentId, IndexCoordinates.of(indexName));
    }

    /**
     * 判断是否是逻辑删除
     * 
     * <p>MyBatis Plus的逻辑删除配置：
     * - logic-delete-field: deleted
     * - logic-not-delete-value: 0
     * - logic-delete-value: id（删除时设置为记录的主键ID）
     * </p>
     * 
     * <p>判断逻辑：
     * - 如果deleted字段存在且不为0/null，说明是逻辑删除
     * - 如果deleted字段为0/null或不存在，说明是普通更新
     * </p>
     * 
     * @param afterData 更新后的数据
     * @return true if logical delete, false otherwise
     */
    private boolean isLogicalDelete(Map<String, Object> afterData) {
        if (afterData == null || afterData.isEmpty()) {
            return false;
        }

        Object deletedValue = afterData.get("deleted");
        if (deletedValue == null) {
            return false;
        }

        // 逻辑删除时，deleted字段会被设置为记录的主键ID（Long类型）
        // 如果deleted不为0且不为null，说明是逻辑删除
        if (deletedValue instanceof Long deleted) {
            return deleted != 0L;
        } else if (deletedValue instanceof Integer deleted) {
            return deleted != 0;
        } else if (deletedValue instanceof Boolean deleted) {
            // 有些表可能使用Boolean类型
            return deleted;
        } else if (deletedValue instanceof String deleted) {
            // 字符串类型：如果非空且不是"0"，则认为是逻辑删除
            return !deleted.isEmpty() && !"0".equals(deleted);
        }

        // 其他类型：如果值不为null，则认为是逻辑删除
        return true;
    }

    /**
     * 获取索引名
     * 
     * @param database 数据库名
     * @param table    表名
     * @return 索引名
     */
    private String getIndexName(String database, String table) {
        String key = database + "." + table;

        return tableToIndexMapping.computeIfAbsent(key, k -> {
            // 默认使用数据库名+表名作为索引名，全部小写
            return (database + "_" + table).toLowerCase();
        });
    }

    /**
     * 刷新批量处理器
     */
    @Override
    public void flushBulkProcessor() {
        try {
            logger.info("Flushing bulk processor");
            bulkProcessor.flush();
        } catch (Exception e) {
            logger.error("Error flushing bulk processor", e);
        }
    }

    /**
     * 关闭批量处理器
     * 先刷新所有待处理的数据，然后关闭
     */
    @Override
    public void closeBulkProcessor() {
        try {
            logger.info("Closing bulk processor");
            // 先刷新所有待处理的数据
            bulkProcessor.flush();
            // CustomBulkProcessor没有awaitClose方法，flush后即可认为关闭完成
            logger.info("Bulk processor closed successfully");
        } catch (Exception e) {
            logger.error("Error closing bulk processor", e);
        }
    }
}
