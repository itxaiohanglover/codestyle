package top.codestyle;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDubbo
@MapperScan("top.codestyle.mapper")
@ComponentScan("top")
public class CodestyleUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodestyleUserApplication.class, args);
    }
}