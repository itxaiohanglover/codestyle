package top.codestyle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Codestyle Library 应用启动类
 *
 * @author huxc2020
 */
@SpringBootApplication
@MapperScan("top.codestyle.mapper")
public class CodestyleLibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodestyleLibraryApplication.class, args);
    }

}
