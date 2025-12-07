package top.codestyle.config;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.codestyle.properties.AsyncThreadPoolProperties;
import top.codestyle.properties.ElasticsearchSearchProperties;

/**
 * @author ChonghaoGao
 * @date 2025/11/11 00:16
 */
@Configuration
public class SearchConfig {
    /**
     * 采用Bean的方式以便使其支持基于配置热更新
     */
    @Bean
    @RefreshScope
    public ElasticsearchSearchProperties elasticsearchSearchProperties() {
        return new ElasticsearchSearchProperties();
    }

    @Bean
    public AsyncThreadPoolProperties asyncThreadPoolProperties(){
        return new AsyncThreadPoolProperties();
    }
}
