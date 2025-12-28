package top.codestyle.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.codestyle.services.SyncService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 定时任务调度类
 * 用于定期执行MySQL到ES的数据同步
 * 支持从配置文件动态读取同步间隔
 *
 * @author ChonghaoGao
 * @date 2025/12/4
 */
@Slf4j
@Component
@RefreshScope  // 支持动态刷新配置
public class SyncScheduler implements ApplicationRunner {

    @Autowired
    private SyncService syncService;
    
    // 从配置文件读取同步间隔，单位毫秒，默认30秒
    @Value("${sync.interval:30000}")
    private long syncInterval;
    
    // 上次同步时间
    private LocalDateTime lastSyncTime;

    /**
     * 服务启动时执行全量同步
     * 实现ApplicationRunner接口，在应用启动后自动执行
     */
    @Override
    public void run(ApplicationArguments args) {
        log.info("服务启动，开始执行全量同步");
        try {
            int successCount = syncService.fullSync();
            log.info("服务启动全量同步完成，成功同步 {} 条数据", successCount);
            // 更新上次同步时间为当前时间
            lastSyncTime = LocalDateTime.now();
        } catch (Exception e) {
            log.error("服务启动全量同步失败: {}", e.getMessage());
            // 不影响服务启动
        }
    }

    /**
     * 定时执行增量同步
     * 同步间隔从配置文件读取，支持动态刷新
     */
    @Scheduled(fixedRateString = "${sync.interval:30000}") // 使用配置文件中的间隔
    public void scheduledIncrementalSync() {
        log.info("定时增量同步任务开始执行，当前间隔: {}ms", syncInterval);
        
        try {
            if (lastSyncTime == null) {
                // 如果上次同步时间为空，执行全量同步
                log.info("上次同步时间为空，执行全量同步");
                int successCount = syncService.fullSync();
                log.info("全量同步完成，成功同步 {} 条数据", successCount);
            } else {
                // 执行增量同步
                int successCount = syncService.incrementalSync(lastSyncTime);
                log.info("增量同步完成，成功同步 {} 条数据", successCount);
            }
            
            // 更新上次同步时间为当前时间
            lastSyncTime = LocalDateTime.now();
            // 计算下次同步时间，转换为秒
            long intervalSeconds = syncInterval / 1000;
            log.info("本次同步完成，下次同步时间: {}", 
                    lastSyncTime.plusSeconds(intervalSeconds).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
        } catch (Exception e) {
            log.error("定时同步任务执行失败: {}", e.getMessage());
            // 不影响下次任务执行
        }
    }
    
    /**
     * 定时执行删除数据同步
     * 每5分钟执行一次，确保ES中删除的数据与MySQL一致
     */
    @Scheduled(fixedRate = 300000) // 5分钟执行一次
    public void scheduledDeleteSync() {
        log.info("定时删除同步任务开始执行");
        
        try {
            // 执行删除数据同步
            int successCount = ((top.codestyle.services.impl.SyncServiceImpl)syncService).syncDeletedData();
            log.info("定时删除同步完成，成功处理 {} 条数据", successCount);
        } catch (Exception e) {
            log.error("定时删除同步任务执行失败: {}", e.getMessage());
            // 不影响下次任务执行
        }
    }
}