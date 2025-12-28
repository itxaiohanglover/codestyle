

package top.codestyle.admin.search.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 搜索缓存配置类
 * 启用异步和定时任务支持（用于缓存预热）
 * 
 * @author chonghaoGao
 * @date 2025/12/23
 */
@Configuration
@EnableAsync
@EnableScheduling
@ConditionalOnProperty(name = "codestyle.search.enabled", havingValue = "true", matchIfMissing = true)
public class SearchCacheConfig {
    // 配置类，启用异步和定时任务
}
