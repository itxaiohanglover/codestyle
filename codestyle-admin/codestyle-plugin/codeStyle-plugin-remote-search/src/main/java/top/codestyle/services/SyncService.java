package top.codestyle.services;

import java.time.LocalDateTime;

/**
 * ES同步服务接口
 * 定义MySQL与ES之间的数据同步方法
 *
 * @author ChonghaoGao
 * @date 2025/12/4
 */
public interface SyncService {

    /**
     * 全量同步：将MySQL中所有数据同步到ES
     * @return 同步成功的数量
     */
    int fullSync();

    /**
     * 增量同步：将指定时间后更新的数据同步到ES
     * @param lastSyncTime 上次同步时间
     * @return 同步成功的数量
     */
    int incrementalSync(LocalDateTime lastSyncTime);

    /**
     * 单条数据同步：根据ID将MySQL中的数据同步到ES
     * @param id MySQL数据ID
     * @return 是否同步成功
     */
    boolean syncById(Long id);
}