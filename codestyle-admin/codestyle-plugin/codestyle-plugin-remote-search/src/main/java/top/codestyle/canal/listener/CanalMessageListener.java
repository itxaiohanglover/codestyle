package top.codestyle.canal.listener;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.codestyle.properties.CanalProperties;
import top.codestyle.services.SyncService;

import java.util.List;

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
@EnableScheduling
public class CanalMessageListener {

    @Resource
    private CanalConnector canalConnector;

    @Resource
    private CanalProperties canalProperties;

    @Resource
    private SyncService syncService;

    /**
     * 定时获取Canal消息
     * 使用@Scheduled注解实现定时轮询
     */
    @Scheduled(fixedDelayString = "${canal.client.instances.example.timeout:10000}")
    public void processCanalMessage() {
        try {
            log.info("同步获取Canal中pull得到的消息");
            // 获取指定数量的消息
            Message message = canalConnector.getWithoutAck(canalProperties.getBatchSize());
            long batchId = message.getId();
            
            if (batchId != -1) {
                List<CanalEntry.Entry> entries = message.getEntries();
                log.info("获取到Canal消息，批次ID: {}, 条目数: {}", batchId, entries.size());
                
                // 处理消息
                processEntries(entries);
                
                // 确认消息
                canalConnector.ack(batchId);
                log.info("确认Canal消息，批次ID: {}", batchId);
            }
        } catch (Exception e) {
            log.error("处理Canal消息失败: {}", e.getMessage(), e);
            // 发生异常时回滚
            try {
                canalConnector.rollback();
                log.info("Canal消息回滚成功");
            } catch (Exception rollbackEx) {
                log.error("Canal消息回滚失败: {}", rollbackEx.getMessage(), rollbackEx);
            }
        }
    }

    /**
     * 处理Canal消息条目
     *
     * @param entries 消息条目列表
     */
    private void processEntries(List<CanalEntry.Entry> entries) {
        log.info("开始处理 {} 个Canal消息条目", entries.size());
        for (CanalEntry.Entry entry : entries) {
            log.info("处理Canal条目，类型: {}, 表名: {}, 事件类型: {}, 日志文件名: {}, 日志偏移量: {}", 
                    entry.getEntryType(), 
                    entry.getHeader().getTableName(), 
                    entry.getHeader().getEventType(),
                    entry.getHeader().getLogfileName(),
                    entry.getHeader().getLogfileOffset());
            
            if (entry.getEntryType() == CanalEntry.EntryType.ROWDATA) {
                // 处理行数据变更
                processRowData(entry);
            } else {
                // 其他类型消息，如事务开始/结束，直接忽略
                log.info("忽略非ROWDATA类型的Canal消息: {}", entry.getEntryType());
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
            
            log.info("Canal行数据变更，表名: {}, 事件类型: {}, 受影响行数: {}", 
                    tableName, eventType, rowChange.getRowDatasCount());
            
            // 打印所有行数据变更
            log.info("行数据变更详情: {}", rowChange.toString());
            
            // 只处理tb_remote_meta_info表的变更
            if (!"tb_remote_meta_info".equals(tableName)) {
                log.info("忽略非目标表的变更: {}", tableName);
                return;
            }
            
            // 处理每一行数据
            log.info("开始处理 {} 行数据变更", rowChange.getRowDatasCount());
            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                // 获取数据ID
                Long id = getIdFromRowData(rowData, eventType);
                if (id == null) {
                    log.warn("未找到数据ID");
                    log.warn("行数据详情: 前数据: {}, 后数据: {}", 
                            rowData.getBeforeColumnsList(), rowData.getAfterColumnsList());
                    continue;
                }
                
                log.info("处理数据变更，表名: {}, 事件类型: {}, ID: {}", tableName, eventType, id);
                log.info("数据详情: 前数据: {}, 后数据: {}", 
                        rowData.getBeforeColumnsList(), rowData.getAfterColumnsList());
                
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
     * @param rowData    行数据
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
}
