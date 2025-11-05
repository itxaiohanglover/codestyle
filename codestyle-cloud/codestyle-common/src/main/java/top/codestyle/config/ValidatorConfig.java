package top.codestyle.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
public class ValidatorConfig {
    
    @Bean
    @Primary
    public Validator primaryValidator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }
}
