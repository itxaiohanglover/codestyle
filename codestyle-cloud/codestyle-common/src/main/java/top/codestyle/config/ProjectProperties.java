package top.codestyle.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "project")
@Data
public class ProjectProperties {

    private String name;

    private String profile;

}
