package top.codestyle.canal.runner;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import top.codestyle.canal.service.CanalSyncService;

/**
 * 全量同步执行器
 * 实现ApplicationRunner接口，在应用启动时执行全量同步
 *
 * @author ChonghaoGao
 * @date 2025/12/13
 */
@Slf4j
@Component
@AllArgsConstructor
public class FullSyncRunner implements ApplicationRunner {

    private final CanalSyncService canalSyncService;

    /**
     * 应用启动时执行全量同步
     *
     * @param args 应用启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("应用启动，开始执行全量同步");
            int count = canalSyncService.fullSyncByCanal();
            log.info("全量同步完成，成功同步 {} 条数据到ES", count);
        } catch (Exception e) {
            log.error("全量同步失败: {}", e.getMessage(), e);
        }
    }
}
