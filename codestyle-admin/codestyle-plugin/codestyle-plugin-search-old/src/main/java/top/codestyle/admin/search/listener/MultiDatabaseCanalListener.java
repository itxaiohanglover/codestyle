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

package top.codestyle.admin.search.listener;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import top.codestyle.admin.search.config.CanalConfigProperties;
import top.codestyle.admin.search.model.dto.DataChangeMessage;
import top.codestyle.admin.search.service.impl.DataSyncServiceImpl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Canal数据变更监听器，从Canal读取变更并发送到Kafka
 * 
 * @author ChonghaoGao
 * @date 2025/12/23
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(name = "com.alibaba.otter.canal.client.CanalConnector")
@ConditionalOnProperty(name = "canal.enabled", havingValue = "true")
public class MultiDatabaseCanalListener {

    private final CanalConnector canalConnector;
    private final CanalConfigProperties canalConfigProperties;
    private final DataSyncServiceImpl dataSyncService;

    private ExecutorService executorService;
    private volatile boolean running = false;

    @PostConstruct
    public void init() {
        log.info("Initializing Canal listener...");

        if (canalConnector == null) {
            log.warn("Canal connector is not available, skipping initialization");
            return;
        }

        // 初始化线程池
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "canal-listener-thread");
            thread.setDaemon(true);
            return thread;
        });

        // 启动监听线程
        running = true;
        executorService.submit(this::processMessage);

        log.info("Canal listener initialized successfully");
    }

    /**
     * 守护线程：持续从Canal拉取消息并发送到Kafka
     * 
     * <p><b>注意：</b>此守护线程在企业级架构中通常不需要。
     * 如果使用Canal Server直连Kafka模式，Canal Server会自动发送消息到Kafka，
     * 应用层只需要消费Kafka消息即可，无需此守护线程。</p>
     * 
     * <p>参考：Canal+Kafka架构优化分析.md</p>
     */
    private void processMessage() {
        log.info("Starting Canal message processing (守护线程模式)");
        log.warn("注意：企业级推荐使用Canal Server直连Kafka模式，无需此守护线程");

        while (running) {
            try {
                // 获取消息
                Message message = canalConnector.getWithoutAck(canalConfigProperties.getBatchSize());

                long batchId = message.getId();
                int size = message.getEntries().size();

                if (batchId == -1 || size == 0) {
                    // 没有数据，休眠一小段时间
                    Thread.sleep(100);
                    continue;
                }

                log.debug("Received batch of messages, batchId={}, size={}", batchId, size);

                // 处理消息
                processEntries(message.getEntries());

                // 确认批次
                canalConnector.ack(batchId);

            } catch (InterruptedException e) {
                log.error("Canal message processing thread interrupted", e);
                break;
            } catch (Exception e) {
                log.error("Error processing Canal messages", e);
                // 处理失败，回滚
                try {
                    canalConnector.rollback();
                } catch (Exception rollbackEx) {
                    log.error("Failed to rollback Canal batch", rollbackEx);
                }

                // 休眠一段时间避免频繁失败
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        log.info("Canal message processing stopped");
    }

    private void processEntries(List<CanalEntry.Entry> entries) {
        for (CanalEntry.Entry entry : entries) {
            if (entry.getEntryType() != CanalEntry.EntryType.ROWDATA) {
                continue;
            }

            try {
                CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                CanalEntry.EventType eventType = rowChange.getEventType();

                // 获取数据库和表名
                String database = entry.getHeader().getSchemaName();
                String table = entry.getHeader().getTableName();
                String fullTableName = database + "." + table;

                log.info("Processing change event: {}, table: {}", eventType, fullTableName);

                // 处理不同类型的事件
                switch (eventType) {
                    case INSERT:
                        processInsert(rowChange.getRowDatasList(), database, table);
                        break;
                    case UPDATE:
                        processUpdate(rowChange.getRowDatasList(), database, table);
                        break;
                    case DELETE:
                        processDelete(rowChange.getRowDatasList(), database, table);
                        break;
                    default:
                        log.debug("Unhandled event type: {}", eventType);
                }
            } catch (Exception e) {
                log.error("Error processing entry", e);
            }
        }
    }

    private void processInsert(List<CanalEntry.RowData> rowDatas, String database, String table) {
        log.info("Processing INSERT event for table: {}.{}, row count: {}", database, table, rowDatas.size());

        for (CanalEntry.RowData rowData : rowDatas) {
            try {
                // 构建变更后的数据Map
                Map<String, Object> afterData = buildEnhancedDataMap(rowData.getAfterColumnsList());

                // 提取主键
                Object primaryKey = extractPrimaryKey(afterData);

                // 构建并发送数据变更消息到Kafka
                DataChangeMessage message = new DataChangeMessage();
                message.setOperationType("INSERT");
                message.setDatabase(database);
                message.setTable(table);
                message.setPrimaryKey(primaryKey);
                message.setAfterData(afterData);
                message.setTimestamp(System.currentTimeMillis());

                dataSyncService.sendDataChangeMessage(message);

            } catch (Exception e) {
                log.error("Error processing INSERT event for {}.{}", database, table, e);
            }
        }
    }

    private void processUpdate(List<CanalEntry.RowData> rowDatas, String database, String table) {
        log.info("Processing UPDATE event for table: {}.{}, row count: {}", database, table, rowDatas.size());

        for (CanalEntry.RowData rowData : rowDatas) {
            try {
                // 构建变更前后的数据Map
                Map<String, Object> beforeData = buildEnhancedDataMap(rowData.getBeforeColumnsList());
                Map<String, Object> afterData = buildEnhancedDataMap(rowData.getAfterColumnsList());

                // 提取主键
                Object primaryKey = extractPrimaryKey(afterData);

                // 构建并发送数据变更消息到Kafka
                DataChangeMessage message = getDataChangeMessage(database, table, primaryKey, beforeData, afterData);

                dataSyncService.sendDataChangeMessage(message);

            } catch (Exception e) {
                log.error("Error processing UPDATE event for {}.{}", database, table, e);
            }
        }
    }

    private static @NotNull DataChangeMessage getDataChangeMessage(String database,
                                                                   String table,
                                                                   Object primaryKey,
                                                                   Map<String, Object> beforeData,
                                                                   Map<String, Object> afterData) {
        DataChangeMessage message = new DataChangeMessage();
        message.setOperationType("UPDATE");
        message.setDatabase(database);
        message.setTable(table);
        message.setPrimaryKey(primaryKey);
        message.setBeforeData(beforeData);
        message.setAfterData(afterData);
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }

    private void processDelete(List<CanalEntry.RowData> rowDatas, String database, String table) {
        log.info("Processing DELETE event for table: {}.{}, row count: {}", database, table, rowDatas.size());

        for (CanalEntry.RowData rowData : rowDatas) {
            try {
                // 构建变更前的数据Map
                Map<String, Object> beforeData = buildEnhancedDataMap(rowData.getBeforeColumnsList());

                // 提取主键
                Object primaryKey = extractPrimaryKey(beforeData);

                // 构建并发送数据变更消息到Kafka
                DataChangeMessage message = new DataChangeMessage();
                message.setOperationType("DELETE");
                message.setDatabase(database);
                message.setTable(table);
                message.setPrimaryKey(primaryKey);
                message.setBeforeData(beforeData);
                message.setTimestamp(System.currentTimeMillis());

                dataSyncService.sendDataChangeMessage(message);

            } catch (Exception e) {
                log.error("Error processing DELETE event for {}.{}", database, table, e);
            }
        }
    }

    /**
     * 增强版的数据Map构建，包含空值处理和类型转换
     * 注意：已废弃buildDataMap方法，统一使用此方法
     */
    private Map<String, Object> buildEnhancedDataMap(List<CanalEntry.Column> columns) {
        Map<String, Object> data = new HashMap<>();
        for (CanalEntry.Column column : columns) {
            if (column.getIsNull()) {
                data.put(column.getName(), null);
            } else {
                data.put(column.getName(), convertValue(column));
            }
        }
        return data;
    }

    /**
     * 根据MySQL类型转换列值
     */
    private Object convertValue(CanalEntry.Column column) {
        String value = column.getValue();

        // 根据MySQL类型进行适当的类型转换
        if (column.getMysqlType().startsWith("int") || column.getMysqlType().equals("tinyint") || column.getMysqlType()
            .equals("smallint") || column.getMysqlType().equals("bigint")) {
            return Long.valueOf(value);
        } else if (column.getMysqlType().startsWith("float") || column.getMysqlType().startsWith("double") || column
            .getMysqlType()
            .startsWith("decimal")) {
            return Double.valueOf(value);
        } else if (column.getMysqlType().equals("boolean") || column.getMysqlType().equals("bit")) {
            return Boolean.valueOf(value);
        }

        // 默认返回字符串
        return value;
    }

    /**
     * 提取主键值
     */
    private Object extractPrimaryKey(Map<String, Object> data) {
        // 优先查找id字段
        if (data.containsKey("id")) {
            return data.get("id");
        }

        // 如果没有id字段，返回第一个字段的值作为备选
        if (!data.isEmpty()) {
            return data.values().iterator().next();
        }

        return null;
    }

    @PreDestroy
    public void destroy() {
        log.info("Stopping Canal listener...");
        running = false;

        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (canalConnector != null) {
            try {
                canalConnector.disconnect();
            } catch (Exception e) {
                log.error("Error disconnecting Canal connector", e);
            }
        }

        log.info("Canal listener stopped successfully");
    }
}