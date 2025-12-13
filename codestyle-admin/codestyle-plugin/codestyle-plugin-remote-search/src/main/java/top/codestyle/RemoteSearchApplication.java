

package top.codestyle;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author ChonghaoGao
 * @date 2025/12/1 18:49)
 */
@SpringBootApplication(scanBasePackages = {"top.codestyle"}, exclude = {
    SqlInitializationAutoConfiguration.class, // 排除SQL初始化自动配置
    top.continew.starter.extension.tenant.autoconfigure.TenantAutoConfiguration.class, // 排除租户自动配置
    top.continew.starter.extension.tenant.autoconfigure.TenantWebMvcAutoConfiguration.class // 排除租户Web MVC自动配置
})
@EnableScheduling
@MapperScan("top.codestyle.mapper") // 使用MyBatis-Plus的@MapperScan自动扫描Mapper接口
public class RemoteSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(RemoteSearchApplication.class, args);
    }

    /**
     * 配置MyBatis-Plus插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
