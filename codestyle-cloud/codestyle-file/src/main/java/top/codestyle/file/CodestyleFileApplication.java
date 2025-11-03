package top.codestyle.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;

@SpringBootApplication
@MapperScan("top.codestyle.file.mapper")
@ComponentScan("top")
@Slf4j
@RequiredArgsConstructor
@EnableFileStorage
public class CodestyleFileApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodestyleFileApplication.class, args);
    }

}
