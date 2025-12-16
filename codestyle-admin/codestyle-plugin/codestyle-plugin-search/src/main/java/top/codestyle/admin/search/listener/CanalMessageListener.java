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

package top.codestyle.admin.search.listener;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import top.codestyle.admin.search.config.CanalProperties;
import top.codestyle.admin.search.services.SyncService;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Canal消息监听器
 * 直接处理Canal消息，同步到ES
 * 简化实现，去掉不必要的事件分发，直接处理数据变更
 *
 * @author ChonghaoGao
 * @date 2025/12/13
 */
@Slf4j
@Component
public class CanalMessageListener implements ApplicationRunner, DisposableBean {

    @Resource
    private CanalConnector canalConnector;

    @Resource
    private CanalProperties canalProperties;

    @Resource
    private SyncService syncService;

    /**
     * 运行状态标志，用于优雅终止监听线程
     */
    private volatile boolean running = true;

    /**
     * Canal监听线程引用
     */
    private Thread canalListenerThread;

    /**
     * 阻塞方式获取并处理Canal消息
     * 持续监听Canal消息，采用阻塞方式
     */
    public void processCanalMessage() {
        log.info("启动阻塞式Canal消息处理器");
        while (running) {
            try {
                // 使用阻塞方式获取指定数量的消息，超时时间为配置的timeout
                Message message = canalConnector.getWithoutAck(canalProperties.getBatchSize(), canalProperties
                    .getTimeout(), TimeUnit.MILLISECONDS);
                long batchId = message.getId();
                if (batchId != -1) { // 正常获取batchId
                    List<CanalEntry.Entry> entries = message.getEntries();
                    log.info("获取到Canal消息，批次ID: {}, 条目数: {}", batchId, entries.size());

                    // 处理消息
                    processEntries(entries);

                    // 确认消息
                    canalConnector.ack(batchId);
                    log.info("确认Canal消息，batchId: {}", batchId);
                }
            } catch (Exception e) {
                // 检查线程中断状态
                if (Thread.currentThread().isInterrupted()) {
                    log.info("Canal监听线程被中断，准备退出");
                    Thread.currentThread().interrupt();
                    break;
                }
                
                // 检查是否为连接关闭相关异常
                if (e.getMessage() != null && (e.getMessage().contains("AsynchronousCloseException") || 
                    e.getMessage().contains("Connection closed") ||
                    e.getMessage().contains("Broken pipe") ||
                    e.getMessage().contains("Channel closed"))) {
                    log.warn("Canal连接已关闭，准备退出监听线程: {}", e.getMessage());
                    break;
                }
                
                log.error("处理Canal消息失败: {}", e.getMessage(), e);
                // 发生异常时回滚（仅在running状态下）
                if (running) {
                    try {
                        canalConnector.rollback();
                        log.info("Canal消息回滚成功");
                    } catch (Exception rollbackEx) {
                        // 检查回滚异常是否为连接关闭相关
                        if (rollbackEx.getMessage() == null || 
                            rollbackEx.getMessage().contains("NullPointerException") || 
                            rollbackEx.getMessage().contains("AsynchronousCloseException") ||
                            rollbackEx.getMessage().contains("Connection closed") ||
                            rollbackEx.getMessage().contains("Channel closed")) {
                            log.warn("Canal连接已关闭，回滚操作失败: {}", rollbackEx.getMessage());
                            break;
                        }
                        log.error("Canal消息回滚失败: {}", rollbackEx.getMessage(), rollbackEx);
                    }
                    // 短暂休眠后继续
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        log.info("Canal监听线程休眠被中断，准备退出");
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.info("Canal监听线程已停止，不再处理异常");
                    break;
                }
            }
        }
        log.info("Canal消息处理器已退出");
    }

    /**
     * 处理Canal消息条目
     *
     * @param entries 消息条目列表
     */
    private void processEntries(List<CanalEntry.Entry> entries) {
        log.info("开始处理 {} 个Canal消息条目", entries.size());
        for (CanalEntry.Entry entry : entries) {
            log.info("处理Canal条目，类型: {}, 表名: {}, 事件类型: {}, 日志文件名: {}, 日志偏移量: {}", entry.getEntryType(), entry.getHeader()
                .getTableName(), entry.getHeader().getEventType(), entry.getHeader().getLogfileName(), entry.getHeader()
                    .getLogfileOffset());

            if (entry.getEntryType() == CanalEntry.EntryType.ROWDATA) {
                // 处理行数据变更
                processRowData(entry);
            } else {
                // 其他类型消息，如事务开始/结束，直接忽略
            }
        }
        log.info("处理Canal消息条目完成");
    }

    /**
     * 处理行数据变更
     *
     * @param entry 消息条目
     */
    private void processRowData(CanalEntry.Entry entry) {
        try {
            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            CanalEntry.EventType eventType = rowChange.getEventType();
            String tableName = entry.getHeader().getTableName();

            log.info("Canal行数据变更，表名: {}, 事件类型: {}, 受影响行数: {}", tableName, eventType, rowChange.getRowDatasCount());

            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                // 获取数据ID
                Long id = getIdFromRowData(rowData, eventType);
                if (id == null) {
                    log.warn("未找到数据ID");
                    continue;
                }

                log.info("处理数据变更，表名: {}, 事件类型: {}, ID: {}", tableName, eventType, id);

                // 同步到ES
                boolean success = syncService.syncById(id);
                if (success) {
                    log.info("成功将ID为 {} 的数据同步到ES", id);
                } else {
                    log.error("同步ID为 {} 的数据到ES失败", id);
                }
            }
            log.info("处理行数据变更完成");
        } catch (Exception e) {
            log.error("处理Canal行数据失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 从行数据中获取ID
     *
     * @param rowData   行数据
     * @param eventType 事件类型
     * @return ID值
     */
    private Long getIdFromRowData(CanalEntry.RowData rowData, CanalEntry.EventType eventType) {
        // 根据事件类型获取对应的列列表
        List<CanalEntry.Column> columns;
        if (eventType == CanalEntry.EventType.DELETE) {
            // 删除事件，从beforeColumns中获取ID
            columns = rowData.getBeforeColumnsList();
        } else {
            // 插入或更新事件，从afterColumns中获取ID
            columns = rowData.getAfterColumnsList();
        }

        // 查找ID列
        for (CanalEntry.Column column : columns) {
            if ("id".equals(column.getName())) {
                return Long.valueOf(column.getValue());
            }
        }

        return null;
    }

    /**
     * 应用启动后自动运行
     * 启动Canal消息监听线程
     *
     * @param args 应用启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        log.info("应用启动完成，开始启动Canal消息监听线程");
        // 启动一个新的线程来运行Canal消息监听，避免阻塞应用启动
        canalListenerThread = new Thread(this::processCanalMessage, "Canal-Listener-Thread");
        canalListenerThread.setDaemon(true);
        canalListenerThread.start();
        log.info("Canal消息监听线程已启动");
    }

    /**
     * 应用关闭时销毁资源
     * 优雅关闭Canal监听线程
     *
     * @throws Exception 销毁过程中的异常
     */
    @Override
    public void destroy() throws Exception {
        log.info("开始销毁Canal消息监听器");
        // 停止运行标志
        running = false;
        
        // 中断监听线程
        if (canalListenerThread != null && canalListenerThread.isAlive()) {
            log.info("中断Canal监听线程");
            canalListenerThread.interrupt();
            try {
                // 等待线程退出，最多等待3秒
                canalListenerThread.join(3000);
                log.info("Canal监听线程已退出");
            } catch (InterruptedException e) {
                log.warn("等待Canal监听线程退出时被中断: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        
        log.info("Canal消息监听器销毁完成");
    }
}
