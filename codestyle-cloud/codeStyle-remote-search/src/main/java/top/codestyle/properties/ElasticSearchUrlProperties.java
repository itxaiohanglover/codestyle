package top.codestyle.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * @author ChonghaoGao
 * @date 2025/12/3 22:46)
 */
@Component
@ConfigurationProperties(prefix = "elasticsearch.config")
@Data
public class ElasticSearchUrlProperties {

    private String host;

    private String indexName;
}
