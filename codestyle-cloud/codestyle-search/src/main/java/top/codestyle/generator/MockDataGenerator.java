package top.codestyle.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.codestyle.entity.es.CodeStyleTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author ChonghaoGao
 * @date 2025/11/11 22:14
 * 一个简单MOCK待检索数据的工具
 */
@Slf4j
@Component
public class MockDataGenerator {
    
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);
    private static final Random RANDOM = new Random();
    
    // 预定义的测试数据
    private static final String[] GROUP_IDS = {
        "com.example", "org.springframework", "io.github", "com.alibaba", 
        "org.apache", "io.netty", "com.google", "org.hibernate",
        "ch.qos.logback", "org.junit", "com.fasterxml.jackson", "org.mybatis"
    };
    
    private static final String[] ARTIFACT_IDS = {
        "core", "web", "data", "security", "boot", "cloud", "test", 
        "common", "utils", "api", "client", "server", "config", "cache"
    };
    
    private static final String[] FILE_PATHS = {
        "src/main/java", "src/test/java", "src/main/resources", 
        "src/test/resources", "src/main/webapp", "src/main/kotlin"
    };
    
    private static final String[] VERSIONS = {
        "1.0.0", "2.0.0", "1.5.3", "2.3.1", "3.0.0", "0.9.1", "1.2.0", "2.1.4"
    };
    
    private static final String[] FILE_NAMES = {
        "UserController", "ProductService", "OrderRepository", "ConfigUtils",
        "SecurityConfig", "Application", "TestBase", "DatabaseConfig",
        "RedisTemplate", "JwtUtils", "StringUtils", "DateUtils",
        "ResponseEntity", "RequestValidator", "ExceptionHandler"
    };
    
    private static final String[] FILE_EXTENSIONS = {
        ".java", ".kt", ".xml", ".properties", ".yml", ".yaml"
    };
    
    private static final String[] PROJECT_DESCRIPTIONS = {
        "微服务架构的用户管理系统",
        "基于Spring Boot的电商平台",
        "分布式配置管理中心",
        "高可用消息队列系统",
        "企业级权限管理系统",
        "大数据分析平台",
        "物联网设备管理平台",
        "云计算资源调度系统",
        "人工智能训练框架",
        "区块链智能合约平台"
    };
    
    private static final String[] DESCRIPTIONS = {
        "这是一个用于处理用户相关操作的控制器类",
        "商品服务的核心业务逻辑实现",
        "订单数据访问层接口定义",
        "系统配置工具类，包含各种配置读取方法",
        "安全配置类，集成Spring Security",
        "Spring Boot应用启动类",
        "测试基类，提供通用的测试方法",
        "数据库连接池和事务配置",
        "Redis模板配置和操作工具",
        "JWT令牌生成和验证工具",
        "字符串处理工具类",
        "日期时间格式化工具",
        "统一响应实体封装",
        "请求参数校验器",
        "全局异常处理器"
    };

    /**
     * 生成单个测试数据
     */
    public CodeStyleTemplate generateSingleTemplate() {
        CodeStyleTemplate template = new CodeStyleTemplate();
        
        template.setFileId("file_" + ID_GENERATOR.getAndIncrement());
        template.setGroupId(getRandomElement(GROUP_IDS));
        template.setArtifactId(getRandomElement(ARTIFACT_IDS));
        template.setFilePath(getRandomElement(FILE_PATHS));
        template.setVersion(getRandomElement(VERSIONS));
        template.setFileName(getRandomElement(FILE_NAMES) + getRandomElement(FILE_EXTENSIONS));
        template.setProjectDescription(getRandomElement(PROJECT_DESCRIPTIONS));
        template.setDescription(getRandomElement(DESCRIPTIONS));
        
        return template;
    }

    /**
     * 批量生成测试数据
     * @param count 生成数量
     * @return 测试数据列表
     */
    public List<CodeStyleTemplate> generateBatchTemplates(int count) {
        List<CodeStyleTemplate> templates = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            templates.add(generateSingleTemplate());
        }
        return templates;
    }

    private <T> T getRandomElement(T[] array) {
        return array[RANDOM.nextInt(array.length)];
    }
}