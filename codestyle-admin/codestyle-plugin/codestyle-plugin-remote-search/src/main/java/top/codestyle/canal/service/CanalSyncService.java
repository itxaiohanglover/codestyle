package top.codestyle.canal.service;

import top.codestyle.services.SyncService;

/**
 * Canal同步服务接口
 * 扩展SyncService，增加Canal相关的同步方法
 *
 * @author ChonghaoGao
 * @date 2025/12/13
 */
public interface CanalSyncService extends SyncService {
    
    /**
     * 全量同步：通过Canal实现全量更新
     * 将MySQL中所有数据同步到ES
     *
     * @return 同步成功的数量
     */
    int fullSyncByCanal();
}
