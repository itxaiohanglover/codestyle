package top.codestyle;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * Elasticsearch数据生成测试类，提供命令行参数控制的数据生成功能
 */
@Slf4j
public class ElasticsearchDataGeneratorTest {

    /**
     * 测试方法：根据参数生成测试数据
     * 可以通过IDE的测试配置传入参数
     */
    @Test
    public void generateTestData() {
        // 默认生成100条测试数据
        int dataCount = 100;
        boolean cleanData = false;
        boolean cleanIndex = false;

        log.info("开始生成Elasticsearch测试数据...");
        log.info("默认生成数据条数: {}", dataCount);
        log.info("如需自定义数据条数，请使用主方法运行并传入参数");

        try {
            // 调用工具类方法
            ElasticsearchIndexTest.setUp();
            
            // 创建索引
            ElasticsearchIndexTest.createIndex();
            
            // 插入测试数据
            if (dataCount > 1000) {
                log.info("数据量较大，将使用并行方式插入");
                ElasticsearchIndexTest.insertTestDataParallel(dataCount, Runtime.getRuntime().availableProcessors());
            } else {
                ElasticsearchIndexTest.insertTestData(dataCount);
            }

            log.info("测试数据生成成功！");
            log.info("索引名称: template_index");
            log.info("数据条数: {}", dataCount);
            
            // 根据参数决定是否清理数据
            if (cleanData) {
                ElasticsearchIndexTest.deleteAllTestData();
                log.info("测试数据已清理");
            } else if (cleanIndex) {
                ElasticsearchIndexTest.deleteIndex();
                log.info("索引已删除");
            } else {
                log.info("如需清理测试数据，请调用以下方法：");
                log.info("1. 删除数据但保留索引: ElasticsearchTestUtils.deleteAllTestData()");
                log.info("2. 删除整个索引: ElasticsearchTestUtils.deleteIndex()");
            }
        } catch (Exception e) {
            log.error("生成测试数据失败", e);
        } finally {
            try {
                ElasticsearchIndexTest.tearDown();
            } catch (Exception e) {
                log.error("关闭资源失败", e);
            }
        }
    }

    /**
     * 清理测试数据的方法
     */
    @Test
    public void cleanTestData() {
        log.info("开始清理测试数据...");
        
        try {
            ElasticsearchIndexTest.setUp();
            
            // 先删除所有数据
            ElasticsearchIndexTest.deleteAllTestData();
            log.info("测试数据清理完成");
            
            // 再删除索引
            ElasticsearchIndexTest.deleteIndex();
            log.info("索引删除完成");
        } catch (Exception e) {
            log.error("清理测试数据失败", e);
        } finally {
            try {
                ElasticsearchIndexTest.tearDown();
            } catch (Exception e) {
                log.error("关闭资源失败", e);
            }
        }
    }

    /**
     * 主方法，用于通过命令行运行
     * 支持以下参数：
     * - 第一个参数：数据条数（必填）
     * - 第二个参数：cleanData（可选，true/false，默认false）
     * - 第三个参数：cleanIndex（可选，true/false，默认false）
     * 
     * 示例：
     * java -cp target/test-classes:target/classes com.es.template.ElasticsearchDataGeneratorTest 1000
     * java -cp target/test-classes:target/classes com.es.template.ElasticsearchDataGeneratorTest 500 true
     * java -cp target/test-classes:target/classes com.es.template.ElasticsearchDataGeneratorTest 200 false true
     */
    public static void main(String[] args) {
        int dataCount = 100; // 默认值
        boolean cleanData = false;
        boolean cleanIndex = false;

        // 解析命令行参数
        if (args.length > 0) {
            try {
                dataCount = Integer.parseInt(args[0]);
                log.info("从命令行参数获取数据条数: {}", dataCount);
            } catch (NumberFormatException e) {
                log.warn("无效的数据条数参数，使用默认值: {}", dataCount);
            }
        }

        if (args.length > 1) {
            cleanData = Boolean.parseBoolean(args[1]);
            log.info("从命令行参数获取cleanData: {}", cleanData);
        }

        if (args.length > 2) {
            cleanIndex = Boolean.parseBoolean(args[2]);
            log.info("从命令行参数获取cleanIndex: {}", cleanIndex);
        }

        // 执行数据生成
        ElasticsearchDataGeneratorTest test = new ElasticsearchDataGeneratorTest();
        
        // 根据参数决定执行哪种操作
        if (cleanData || cleanIndex) {
            test.cleanTestData();
        } else {
            // 修改默认数据条数并执行生成
            ElasticsearchIndexTest utils = new ElasticsearchIndexTest();
            try {
                utils.setUp();
                utils.createIndex();
                utils.insertTestData(dataCount);
                log.info("成功生成 {} 条测试数据到索引 template_index", dataCount);
            } catch (Exception e) {
                log.error("生成数据失败", e);
            } finally {
                try {
                    utils.tearDown();
                } catch (Exception e) {
                    log.error("关闭资源失败", e);
                }
            }
        }
    }
}
