package top.codestyle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CodestyleSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodestyleSearchApplication.class, args);
    }

}
