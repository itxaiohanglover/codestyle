package top.codestyle.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.codestyle.pojo.entity.CodeStyleTemplateDO;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class MockDataGenerator {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);
    private static final Random RANDOM = new Random();

    private static final String[] CHINESE_NAMES = {
            "用户管理模板", "权限认证中心", "配置管理模板", "日志分析系统",
            "智能推荐模型", "大数据计算模板", "微服务基础框架", "分布式任务调度"
    };

    private static final String[] ENGLISH_NAMES = {
            "UserManager", "AuthCenter", "ConfigCenter", "LogAnalyzer",
            "RecommendationEngine", "BigDataSuite", "MicroServiceKit", "TaskScheduler"
    };

    private static final String[] DESCRIPTIONS = {
            "这是一个用于处理用户相关逻辑的核心模板",
            "适用于权限认证与统一登录的基础模板",
            "提供配置中心核心实现的模板",
            "日志采集与分析结构示例模板",
            "用于智能推荐与模型训练的模板",
            "大数据聚合分析任务的基础模板",
            "微服务架构基础工程模板",
            "面向分布式任务调度场景的模板"
    };

    private static final String[] TAGS = {
            "Java", "Spring Boot", "微服务", "Cloud", "AI", "Data", "Compute", "DevOps"
    };

    private static final String[] AVATARS = {
            "https://example.com/avatar1.png",
            "https://example.com/avatar2.png",
            "https://example.com/avatar3.png",
            "https://example.com/avatar4.png"
    };

    private static final String[] MEMBER_NAMES = {
            "Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace", "Helen"
    };

    private static final String[] MEMBER_AVATARS = {
            "https://example.com/user1.png",
            "https://example.com/user2.png",
            "https://example.com/user3.png",
            "https://example.com/user4.png"
    };

    private static final String[] VERSIONS = {
            "1.0.0", "1.1.0", "2.0.0", "2.1.3", "3.0.0"
    };

    private static final String[] CATEGORIES = {
            "后端开发", "大数据", "人工智能", "微服务", "权限系统", "前端工程"
    };

    /**
     * 生成一个完整 CodeStyleTemplateDO
     */
    public CodeStyleTemplateDO generateSingleTemplate() {

        CodeStyleTemplateDO t = new CodeStyleTemplateDO();

        // 主键
        t.setId("tpl_" + ID_GENERATOR.getAndIncrement());

        // 搜索字段
        t.setNameCh(getRandom(CHINESE_NAMES));
        t.setNameEn(getRandom(ENGLISH_NAMES));
        t.setDescription(getRandom(DESCRIPTIONS));

        // tags
        t.setSearchTags(randomList(TAGS, 3));

        // 展示字段
        t.setVersion(getRandom(VERSIONS));
        t.setAvatar(getRandom(AVATARS));

        t.setMemberNames(randomList(MEMBER_NAMES, 3));
        t.setMemberAvatarUrls(randomList(MEMBER_AVATARS, 3));

        // 非展示字段
        t.setCreatorId((long) RANDOM.nextInt(10000));
        t.setCreatorName(getRandom(MEMBER_NAMES));
        t.setMemberIds(randomLongList(3));

        // 统计字段
        t.setIsPrivate(1);
        t.setTotalFileSize((long) RANDOM.nextInt(50000));
        t.setTotalFileCount((long) RANDOM.nextInt(200));
        t.setTotalMemberCount((long) RANDOM.nextInt(20));
        t.setIsPrivate(RANDOM.nextInt(2));
        t.setTotalLikeCount((long) RANDOM.nextInt(5000));
        t.setTotalFavoriteCount((long) RANDOM.nextInt(5000));

        // 时间字段
        Date now = new Date();
        t.setCreateTime(randomPastDate());
        t.setUpdateTime(now);
        t.setEditTime(now);

        // 逻辑删除
        t.setIsDelete(0);

        // 热度分数（脚本排序可用）
        t.setHotScoreWeight(RANDOM.nextDouble() * 10);

        return t;
    }

    /**
     * 批量生成
     */
    public List<CodeStyleTemplateDO> generateBatchTemplates(int count) {
        List<CodeStyleTemplateDO> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(generateSingleTemplate());
        }
        return list;
    }

    // 工具函数

    private <T> T getRandom(T[] arr) {
        return arr[RANDOM.nextInt(arr.length)];
    }

    private List<String> randomList(String[] arr, int size) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(getRandom(arr));
        }
        return result;
    }

    private List<Long> randomLongList(int size) {
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add((long) RANDOM.nextInt(10000));
        }
        return result;
    }

    private Date randomPastDate() {
        long now = System.currentTimeMillis();
        long offset = RANDOM.nextInt( 3600 * 24 * 30); // 30 天内
        return new Date(now - offset);
    }
}
