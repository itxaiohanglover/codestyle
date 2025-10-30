package top.codestyle;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.github.xiaoymin.knife4j.spring.configuration.Knife4jProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.ComponentScan;
import top.codestyle.config.ProjectProperties;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

@SpringBootApplication
@EnableDubbo
@MapperScan("top.codestyle.mapper")
@ComponentScan("top")
@Slf4j
@RequiredArgsConstructor
public class CodestyleUserApplication implements ApplicationRunner {

    private final ProjectProperties projectProperties;
    private final ServerProperties serverProperties;


    public static void main(String[] args) {
        SpringApplication.run(CodestyleUserApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws URISyntaxException, IOException {
        if (Objects.equals(projectProperties.getProfile(), "dev")) {
            String hostAddress = NetUtil.getLocalhostStr();
            Integer port = serverProperties.getPort();
            String contextPath = serverProperties.getServlet().getContextPath();
            String baseUrl = URLUtil.normalize("%s:%s%s".formatted(hostAddress, port, contextPath));
            log.info("----------------------------------------------");
            log.info("{} service started successfully.", projectProperties.getName());
            log.info("API地址：{}", baseUrl);
            Knife4jProperties knife4jProperties = SpringUtil.getBean(Knife4jProperties.class);
            if (!knife4jProperties.isProduction()) {
                log.info("API文档：{}/doc.html", baseUrl);
            }
            log.info("----------------------------------------------");
        }
    }
}