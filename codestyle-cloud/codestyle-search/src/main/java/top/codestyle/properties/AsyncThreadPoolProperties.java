package top.codestyle.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ChonghaoGao
 * @date 2025/11/12 22:49
 */

@ConfigurationProperties(prefix = "elasticsearch.search.async.thread.pool")
@Data
public class AsyncThreadPoolProperties {
    private Integer corePoolSize = 12;
    private Integer maxPoolSize = 24;
    private Integer queueCapacity = 100;
}

