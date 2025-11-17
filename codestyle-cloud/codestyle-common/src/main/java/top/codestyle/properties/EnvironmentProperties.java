package top.codestyle.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "project")
@Data
public class EnvironmentProperties {

    private String name;

    private String profile;

}
