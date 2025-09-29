package top.codestyle.generator;

import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.util.Collections;
import java.util.Map;

/**
 * MyBatis-Plus 代码生成器示例
 */
public class MyBatisPlusCodeGenerator {

    // 要生成的表名
    private static final String[] TABLE_NAMES = {"chat_history"};

    public static void main(String[] args) {
        // 读取 application.yml 获取数据源配置
        Dict dict = YamlUtil.loadByPath("application.yml");
        Map<String, Object> ds = dict.getByPath("spring.datasource");
        String url = String.valueOf(ds.get("url"));
        String username = String.valueOf(ds.get("username"));
        String password = String.valueOf(ds.get("password"));

        // 指定输出目录（建议先生成到一个临时文件夹）
        String outputDir = System.getProperty("user.dir") + "/src/main/java";

        // 开始生成
        FastAutoGenerator.create(url, username, password)
                // 1. 全局配置
                .globalConfig(builder -> {
                    builder.author("程序员鱼皮")
                            .enableSwagger()             // 如果需要 Swagger 注解
                            .disableOpenDir()            // 生成后不自动打开目录
                            .outputDir(outputDir)        // Java 文件输出路径
                            .commentDate("yyyy-MM-dd");
                })
                // 2. 包配置
                .packageConfig(builder -> {
                    builder.parent("top.codestyle.yuaicodemother.genresult") // 根包
                            .entity("entity")
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("service.impl")
                            .controller("controller")
                            // Mapper XML 文件输出路径（可自定义到 resources/mapper 下）
                            .pathInfo(Collections.singletonMap(
                                    OutputFile.xml,
                                    System.getProperty("user.dir") + "/src/main/resources/mapper"
                            ));
                })
                // 3. 策略配置
                .strategyConfig(builder -> {
                    builder.addInclude(TABLE_NAMES) // 指定表
                            .entityBuilder()
                            .enableLombok()     // 开启 Lombok
                            .logicDeleteColumnName("is_delete") // 逻辑删除字段
                            .serviceBuilder()
                            .formatServiceFileName("%sService")
                            .controllerBuilder()
                            .enableRestStyle(); // 生成 @RestController
                })
                // 4. 模板引擎（Velocity/Freemarker/Beetl）
                .templateEngine(new VelocityTemplateEngine())
                .execute();
    }
}
