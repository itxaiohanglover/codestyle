package top.codestyle.config.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import top.continew.starter.extension.datapermission.filter.DataPermissionUserContextProvider;

/**
 * MyBatis Plus 配置
 *
 * @author GALAwang
 * @since 2022/12/22 19:51
 */
@Configuration
public class MybatisPlusConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 元对象处理器配置（插入或修改时自动填充）
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MyBatisPlusMetaObjectHandler();
    }

    /**
     * 数据权限用户上下文提供者
     */
    @Bean
    public DataPermissionUserContextProvider dataPermissionUserContextProvider() {
        return new DefaultDataPermissionUserContextProvider();
    }

    /**
     * BCrypt 加/解密处理器
     */
    @Bean
    public BCryptEncryptor bCryptEncryptor(PasswordEncoder passwordEncoder) {
        return new BCryptEncryptor(passwordEncoder);
    }
}
