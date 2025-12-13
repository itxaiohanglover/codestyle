

package top.codestyle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.continew.starter.auth.satoken.autoconfigure.SaTokenExtensionProperties;

/**
 * Sa-Token配置类，提供缺失的SaTokenExtensionProperties Bean
 *
 * @author ChonghaoGao
 * @date 2025/12/8
 */
@Configuration
public class SaTokenConfig {

    /**
     * 提供SaTokenExtensionProperties Bean，解决GlobalAuthenticationCustomizer的依赖问题
     */
    @Bean
    public SaTokenExtensionProperties saTokenExtensionProperties() {
        // 直接创建空的SaTokenExtensionProperties实例
        // 因为我们只需要满足依赖注入，不需要实际使用其功能
        return new SaTokenExtensionProperties();
    }
}
