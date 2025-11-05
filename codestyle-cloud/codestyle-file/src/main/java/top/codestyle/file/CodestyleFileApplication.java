package top.codestyle.file;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.github.xiaoymin.knife4j.spring.configuration.Knife4jProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RestController;
import top.codestyle.config.ProjectProperties;
import top.codestyle.enums.DisEnableStatusEnum;
import top.codestyle.file.service.StorageService;
import top.codestyle.model.dto.file.StorageReq;
import top.codestyle.model.query.StorageQuery;
import top.codestyle.model.vo.StorageResp;
import top.continew.starter.extension.crud.annotation.EnableCrudRestController;


import java.util.List;
import java.util.Objects;

@SpringBootApplication
@MapperScan("top.codestyle.file.mapper")
@ComponentScan("top.codestyle")
@Slf4j
@RequiredArgsConstructor
@EnableMethodCache(basePackages = "top.codestyle.file")
@EnableFileStorage
@RestController
@EnableCrudRestController
public class CodestyleFileApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(CodestyleFileApplication.class, args);
    }

    private final StorageService storageService;
    private final ProjectProperties projectProperties;
    private final ServerProperties serverProperties;

    @Override
    public void run(ApplicationArguments args) {
        StorageQuery query = new StorageQuery();
        query.setStatus(DisEnableStatusEnum.ENABLE);
        List<StorageResp> storageList = storageService.list(query, null);
        if (storageList.isEmpty()) {
            return;
        }
        storageList.forEach(s -> storageService.load(BeanUtil.copyProperties(s, StorageReq.class)));

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