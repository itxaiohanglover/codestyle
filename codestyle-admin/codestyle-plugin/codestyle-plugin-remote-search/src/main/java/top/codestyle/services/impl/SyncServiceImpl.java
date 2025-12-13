

package top.codestyle.services.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.codestyle.canal.service.CanalSyncService;
import top.codestyle.mapper.RemoteMetaInfoMapper;
import top.codestyle.model.mysql.entity.RemoteMetaInfo;
import top.codestyle.respository.es.RemoteSearchESRepository;
import top.codestyle.services.SyncService;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ES同步服务实现类
 * 实现MySQL与ES之间的数据同步逻辑
 *
 * @author ChonghaoGao
 * @date 2025/12/4
 */
@Slf4j
@Service
@AllArgsConstructor
public class SyncServiceImpl implements CanalSyncService {

    private final RemoteMetaInfoMapper remoteMetaInfoMapper;
    private final RemoteSearchESRepository remoteSearchESRepository;

    /**
     * 全量同步：将MySQL中所有数据同步到ES
     * 注意：此方法会先删除ES中的所有数据，然后重新载入，不应频繁调用
     * 
     * @return 同步成功的数量
     */
    @Override
    public int fullSync() {
        log.info("开始全量同步MySQL数据到ES");

        try {
            // 1. 先删除ES中的所有数据
            remoteSearchESRepository.deleteAllMetaDocs();

            // 2. 查询MySQL中所有数据（未被逻辑删除的）
            List<RemoteMetaInfo> allMetaInfos = remoteMetaInfoMapper.selectList(null);
            log.info("从MySQL查询到 {} 条数据", allMetaInfos.size());

            if (allMetaInfos.isEmpty()) {
                log.info("MySQL中没有数据，无需同步");
                return 0;
            }

            // 3. 直接批量处理数据，减少日志打印
            int successCount = 0;
            for (RemoteMetaInfo metaInfo : allMetaInfos) {
                try {
                    // 直接处理数据，不调用syncById方法，减少冗余日志
                    remoteSearchESRepository.saveMetaDoc(metaInfo.toRemoteMetaDoc());
                    successCount++;
                } catch (Exception e) {
                    log.error("同步ID为 {} 的数据失败: {}", metaInfo.getId(), e.getMessage());
                    // 继续处理下一条数据
                }
            }

            log.info("全量同步完成，成功同步 {} 条数据", successCount);
            return successCount;

        } catch (Exception e) {
            log.error("全量同步失败: {}", e.getMessage());
            throw new RuntimeException("全量同步失败", e);
        }
    }

    
    

    /**
     * 通过Canal实现全量更新
     * 实际上是调用fullSync方法，确保只有一个全量更新入口
     *
     * @return 同步成功的数量
     */
    @Override
    public int fullSyncByCanal() {
        log.info("通过Canal实现全量更新，调用统一的fullSync方法");
        return fullSync();
    }

    /**
     * 增量同步：将指定时间后更新的数据同步到ES
     * 支持处理逻辑删除和物理删除
     * 
     * @param lastSyncTime 上次同步时间
     * @return 同步成功的数量
     */
    @Override
    public int incrementalSync(LocalDateTime lastSyncTime) {
        log.info("开始增量同步MySQL数据到ES，上次同步时间: {}", lastSyncTime);

        try {
            // 1. 查询指定时间后更新的数据
            // 注意：这里需要使用Date类型，因为RemoteMetaInfo中的updateTime是Date类型
            // 转换LocalDateTime为Date
            Date lastSyncDate = Date.from(lastSyncTime.atZone(java.time.ZoneId.systemDefault()).toInstant());

            // 2. 使用自定义方法查询更新的数据，包括已删除的
            // 调用自定义的selectUpdatedDataAfterTime方法，使用原生SQL查询，绕过逻辑删除自动过滤
            List<RemoteMetaInfo> updatedMetaInfos = remoteMetaInfoMapper.selectUpdatedDataAfterTime(lastSyncDate);

            log.info("从MySQL查询到 {} 条增量数据", updatedMetaInfos.size());

            // 3. 逐条同步到ES
            int successCount = 0;
            for (RemoteMetaInfo metaInfo : updatedMetaInfos) {
                try {
                    if (metaInfo.getDeleted() == 1) {
                        // 如果数据已被标记为删除，则从ES中删除该文档
                        remoteSearchESRepository.deleteMetaDoc(metaInfo.getId());
                        log.info("从ES中删除已标记为删除的数据，ID: {}", metaInfo.getId());
                    } else {
                        // 更新或插入ES文档
                        remoteSearchESRepository.saveMetaDoc(metaInfo.toRemoteMetaDoc());
                    }
                    successCount++;
                } catch (Exception e) {
                    log.error("同步ID为 {} 的数据失败: {}", metaInfo.getId(), e.getMessage());
                }
            }

            // 4. 处理物理删除：对比MySQL和ES数据，找出物理删除的数据并从ES中删除
            successCount += handlePhysicalDeletions();

            log.info("增量同步完成，成功同步 {} 条数据", successCount);
            return successCount;

        } catch (Exception e) {
            log.error("增量同步失败: {}", e.getMessage());
            throw new RuntimeException("增量同步失败", e);
        }
    }

    /**
     * 处理物理删除：对比MySQL和ES数据，找出物理删除的数据并从ES中删除
     * 
     * @return 处理成功的数量
     */
    private int handlePhysicalDeletions() {
        log.info("开始处理物理删除");

        try {
            // 1. 获取MySQL中所有存在的数据ID（包括逻辑删除的）
            List<RemoteMetaInfo> allMysqlData = remoteMetaInfoMapper.selectList(null);
            // 提取MySQL中所有数据的ID
            Set<Long> mysqlIds = allMysqlData.stream()
                    .map(RemoteMetaInfo::getId)
                    .collect(Collectors.toSet());

            // 2. 获取ES中所有文档的ID
            Iterable<Long> esIdsIterable = remoteSearchESRepository.getAllMetaDocIds();
            Set<Long> esIds = new HashSet<>();
            esIdsIterable.forEach(esIds::add);

            log.info("MySQL中存在 {} 条数据，ES中存在 {} 条文档", mysqlIds.size(), esIds.size());

            // 3. 找出ES中存在但MySQL中不存在的ID（物理删除的数据）
            Set<Long> physicalDeletedIds = new HashSet<>(esIds);
            physicalDeletedIds.removeAll(mysqlIds);

            log.info("发现 {} 条物理删除的数据", physicalDeletedIds.size());

            // 4. 从ES中删除这些物理删除的数据
            int successCount = 0;
            for (Long id : physicalDeletedIds) {
                try {
                    remoteSearchESRepository.deleteMetaDoc(id);
                    log.info("从ES中删除物理删除的数据，ID: {}", id);
                    successCount++;
                } catch (Exception e) {
                    log.error("删除物理删除的数据失败，ID: {}, 错误: {}", id, e.getMessage());
                }
            }

            log.info("物理删除处理完成，成功处理 {} 条数据", successCount);
            return successCount;

        } catch (Exception e) {
            log.error("处理物理删除失败: {}", e.getMessage());
            // 不抛出异常，允许继续执行后续同步步骤
            return 0;
        }
    }

    /**
     * 同步删除操作：处理已标记为删除的数据
     * 注意：此方法查询所有已标记为删除的数据，并从ES中删除它们
     * 同时确保恢复删除状态的数据能被重新同步到ES
     * 
     * @return 处理成功的数量
     */
    @Override
    public int syncDeletedData() {
        log.info("开始同步删除操作");

        try {
            // 1. 使用自定义SQL查询所有已标记为删除的数据，绕过MyBatis-Plus的逻辑删除自动过滤
            List<RemoteMetaInfo> deletedMetaInfos = remoteMetaInfoMapper.selectAllDeletedData();

            log.info("从MySQL查询到 {} 条已删除数据", deletedMetaInfos.size());

            // 2. 逐条同步到ES（从ES中删除）
            int successCount = 0;
            for (RemoteMetaInfo metaInfo : deletedMetaInfos) {
                try {
                    // 从ES中删除该文档
                    remoteSearchESRepository.deleteMetaDoc(metaInfo.getId());
                    log.info("从ES中删除已标记为删除的数据，ID: {}", metaInfo.getId());
                    successCount++;
                } catch (Exception e) {
                    log.error("同步删除ID为 {} 的数据失败: {}", metaInfo.getId(), e.getMessage());
                }
            }

            // 3. 额外处理：查询所有已恢复的数据（deleted从1变为0），并同步到ES
            List<RemoteMetaInfo> restoredMetaInfos = remoteMetaInfoMapper.selectAllRestoredData();

            log.info("从MySQL查询到 {} 条已恢复数据", restoredMetaInfos.size());

            for (RemoteMetaInfo metaInfo : restoredMetaInfos) {
                try {
                    // 同步到ES
                    remoteSearchESRepository.saveMetaDoc(metaInfo.toRemoteMetaDoc());
                    log.info("同步已恢复的数据到ES，ID: {}", metaInfo.getId());
                    successCount++;
                } catch (Exception e) {
                    log.error("同步恢复ID为 {} 的数据失败: {}", metaInfo.getId(), e.getMessage());
                }
            }

            log.info("同步删除操作完成，成功处理 {} 条数据", successCount);
            return successCount;

        } catch (Exception e) {
            log.error("同步删除操作失败: {}", e.getMessage());
            throw new RuntimeException("同步删除操作失败", e);
        }
    }

    /**
     * 单条数据同步：根据ID将MySQL中的数据同步到ES
     * 支持软删除和物理删除：
     * - 如果数据在MySQL中存在且未删除：同步到ES
     * - 如果数据在MySQL中存在且已删除：从ES中删除
     * - 如果数据在MySQL中不存在（物理删除）：从ES中删除
     * 
     * @param id MySQL数据ID
     * @return 是否同步成功
     */
    @Override
    public boolean syncById(Long id) {
        log.info("开始同步ID为 {} 的数据到ES", id);

        try {
            // 1. 根据ID查询MySQL数据，包括已删除的
            RemoteMetaInfo metaInfo = remoteMetaInfoMapper.selectByIdIncludeDeleted(id);
            
            if (metaInfo == null) {
                // 2. 数据在MySQL中不存在（物理删除），从ES中删除
                log.warn("ID为 {} 的数据在MySQL中不存在，执行物理删除同步", id);
                remoteSearchESRepository.deleteMetaDoc(id);
                log.info("从ES中删除物理删除的数据，ID: {}", id);
            } else if (metaInfo.getDeleted() == 1) {
                // 3. 数据已被标记为逻辑删除，从ES中删除
                remoteSearchESRepository.deleteMetaDoc(metaInfo.getId());
                log.info("从ES中删除已标记为删除的数据，ID: {}", metaInfo.getId());
            } else {
                // 4. 数据正常，同步到ES
                remoteSearchESRepository.saveMetaDoc(metaInfo.toRemoteMetaDoc());
                log.info("同步ID为 {} 的数据成功", id);
            }
            return true;

        } catch (Exception e) {
            log.error("同步ID为 {} 的数据失败: {}", id, e.getMessage());
            return false;
        }
    }
}