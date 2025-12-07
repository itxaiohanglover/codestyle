package top.codestyle.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import top.codestyle.properties.AsyncThreadPoolProperties;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author ChonghaoGao
 * @date 2025/11/12 22:06)
 */
@Configuration
public class AsyncConfig {

    @Autowired
    private AsyncThreadPoolProperties asyncThreadPoolProperties;


    @Bean("searchExecutor")
    public ThreadPoolTaskExecutor searchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncThreadPoolProperties.getCorePoolSize());
        executor.setMaxPoolSize(asyncThreadPoolProperties.getMaxPoolSize());
        executor.setQueueCapacity(asyncThreadPoolProperties.getQueueCapacity());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("search-");
        executor.initialize();
        return executor;
    }
}
