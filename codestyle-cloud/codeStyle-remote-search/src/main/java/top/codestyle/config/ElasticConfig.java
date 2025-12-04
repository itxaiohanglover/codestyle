package top.codestyle.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransportConfig;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.codestyle.properties.ElasticSearchUrlProperties;

/**
 * @author ChonghaoGao
 * @date 2025/12/3 13:38)
 */
@Configuration
@AllArgsConstructor
public class ElasticConfig {

        private final ElasticSearchUrlProperties elasticSearchUrlProperties;

        @Bean
        public ElasticsearchClient elasticsearchClient() {

            ElasticsearchTransportConfig config = new ElasticsearchTransportConfig.Builder()
                    .host(elasticSearchUrlProperties.getHost()).build();
            return new ElasticsearchClient(config);
        }

}
