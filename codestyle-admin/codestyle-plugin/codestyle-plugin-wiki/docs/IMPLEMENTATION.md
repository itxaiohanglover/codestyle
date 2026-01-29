# CodeStyle Wiki 插件 - 实现指南

> 开发实现指南和代码示例
> 
> **版本**: 1.0.0  
> **文档日期**: 2026-01-29

---

## 📋 目录

1. [开发环境搭建](#1-开发环境搭建)
2. [核心代码实现](#2-核心代码实现)
3. [配置说明](#3-配置说明)
4. [测试指南](#4-测试指南)

---

## 1. 开发环境搭建

### 1.1 前置条件

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+
- IDEA 2023+

### 1.2 配置环境变量

创建 `.env` 文件：

```bash
# 通义千问 API Key
DASHSCOPE_API_KEY=your_api_key_here

# GitHub Token（可选）
GITHUB_TOKEN=your_github_token_here
```

### 1.3 初始化数据库

执行 SQL 脚本：

```bash
mysql -u root -p < docs/sql/wiki_tables.sql
```

---

## 2. 核心代码实现

### 2.1 LlmHelper 实现

```java
package top.codestyle.admin.wiki.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * LLM 调用辅助类
 *
 * @author CodeStyle Team
 * @since 2026-01-29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LlmHelper {

    private final ChatClient chatClient;
    private final StringRedisTemplate redisTemplate;

    /**
     * 调用 LLM
     *
     * @param prompt 提示词
     * @return LLM 响应
     */
    public String chat(String prompt) {
        return chat(prompt, null, true);
    }

    /**
     * 调用 LLM（带系统提示词和缓存）
     *
     * @param prompt       提示词
     * @param systemPrompt 系统提示词
     * @param useCache     是否使用缓存
     * @return LLM 响应
     */
    public String chat(String prompt, String systemPrompt, boolean useCache) {
        log.debug("调用 LLM，提示词长度: {}", prompt.length());

        // 1. 检查缓存
        if (useCache) {
            String cacheKey = buildCacheKey(prompt, systemPrompt);
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("从缓存获取 LLM 响应");
                return cached;
            }
        }

        // 2. 构建请求
        ChatClient.ChatClientRequest request = chatClient.prompt()
                .user(prompt);

        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            request.system(systemPrompt);
        }

        // 3. 调用 LLM
        long startTime = System.currentTimeMillis();
        String response = request.call().content();
        long duration = System.currentTimeMillis() - startTime;

        log.info("LLM 调用完成，耗时: {}ms，响应长度: {}", duration, response.length());

        // 4. 缓存结果
        if (useCache) {
            String cacheKey = buildCacheKey(prompt, systemPrompt);
            redisTemplate.opsForValue().set(cacheKey, response, 24, TimeUnit.HOURS);
        }

        return response;
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(String prompt, String systemPrompt) {
        String combined = (systemPrompt != null ? systemPrompt : "") + "|" + prompt;
        String md5 = DigestUtils.md5DigestAsHex(combined.getBytes(StandardCharsets.UTF_8));
        return "wiki:llm:cache:" + md5;
    }
}
```

### 2.2 GitHelper 实现

```java
package top.codestyle.admin.wiki.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Component;
import top.codestyle.admin.wiki.config.WikiProperties;
import top.codestyle.admin.wiki.model.CodeFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Git 操作辅助类
 *
 * @author CodeStyle Team
 * @since 2026-01-29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GitHelper {

    private final WikiProperties wikiProperties;

    /**
     * 克隆 GitHub 仓库
     *
     * @param repoUrl GitHub 仓库 URL
     * @return 本地目录路径
     */
    public String cloneRepository(String repoUrl) {
        try {
            // 生成临时目录
            Path tempDir = Files.createTempDirectory("wiki-repo-");
            File localPath = tempDir.toFile();

            log.info("开始克隆仓库: {} 到 {}", repoUrl, localPath);

            // 克隆仓库
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(localPath)
                    .call();

            log.info("仓库克隆完成: {}", localPath);

            return localPath.getAbsolutePath();

        } catch (Exception e) {
            log.error("克隆仓库失败: {}", repoUrl, e);
            throw new RuntimeException("克隆仓库失败", e);
        }
    }

    /**
     * 读取目录中的代码文件
     *
     * @param dirPath          目录路径
     * @param includePatterns  包含模式
     * @param excludePatterns  排除模式
     * @param maxFileSize      最大文件大小
     * @return 代码文件列表
     */
    public List<CodeFile> readCodeFiles(
            String dirPath,
            List<String> includePatterns,
            List<String> excludePatterns,
            long maxFileSize
    ) {
        List<CodeFile> codeFiles = new ArrayList<>();
        Path rootPath = Path.of(dirPath);

        try (Stream<Path> paths = Files.walk(rootPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> matchesPatterns(path, includePatterns))
                    .filter(path -> !matchesPatterns(path, excludePatterns))
                    .filter(path -> {
                        try {
                            return Files.size(path) <= maxFileSize;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            String relativePath = rootPath.relativize(path).toString();
                            String content = Files.readString(path);

                            CodeFile codeFile = new CodeFile();
                            codeFile.setPath(relativePath);
                            codeFile.setContent(content);

                            codeFiles.add(codeFile);

                        } catch (IOException e) {
                            log.warn("读取文件失败: {}", path, e);
                        }
                    });

        } catch (IOException e) {
            log.error("遍历目录失败: {}", dirPath, e);
            throw new RuntimeException("遍历目录失败", e);
        }

        log.info("读取代码文件完成，共 {} 个文件", codeFiles.size());

        return codeFiles;
    }

    /**
     * 检查路径是否匹配模式
     */
    private boolean matchesPatterns(Path path, List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }

        String pathStr = path.toString().replace('\\', '/');

        return patterns.stream().anyMatch(pattern -> {
            String regex = pattern.replace("*", ".*").replace("?", ".");
            return pathStr.matches(regex);
        });
    }
}
```

### 2.3 WikiGenerateServiceImpl 实现

```java
package top.codestyle.admin.wiki.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.codestyle.admin.wiki.helper.GitHelper;
import top.codestyle.admin.wiki.helper.LlmHelper;
import top.codestyle.admin.wiki.model.CodeFile;
import top.codestyle.admin.wiki.model.entity.WikiGenerateTaskDO;
import top.codestyle.admin.wiki.model.entity.WikiProjectDO;
import top.codestyle.admin.wiki.service.WikiAnalysisService;
import top.codestyle.admin.wiki.service.WikiGenerateService;
import top.codestyle.admin.wiki.service.WikiProjectService;
import top.codestyle.admin.wiki.service.WikiTutorialService;

import java.util.List;

/**
 * Wiki 生成服务实现
 *
 * @author CodeStyle Team
 * @since 2026-01-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WikiGenerateServiceImpl implements WikiGenerateService {

    private final WikiProjectService projectService;
    private final WikiAnalysisService analysisService;
    private final WikiTutorialService tutorialService;
    private final GitHelper gitHelper;
    private final LlmHelper llmHelper;

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void generateTutorial(Long taskId) {
        log.info("开始生成教程，任务ID: {}", taskId);

        try {
            // 1. 获取任务信息
            WikiGenerateTaskDO task = getTask(taskId);
            WikiProjectDO project = projectService.getById(task.getProjectId());

            // 2. 更新任务状态
            updateTaskStatus(taskId, 2, 10, "获取代码文件");

            // 3. 获取代码文件
            List<CodeFile> files = fetchCodeFiles(project, task);
            updateTaskStatus(taskId, 2, 30, "识别核心抽象");

            // 4. 识别抽象概念
            List<Abstraction> abstractions = analysisService.identifyAbstractions(files);
            updateTaskStatus(taskId, 2, 50, "分析抽象关系");

            // 5. 分析关系
            Relationship relationship = analysisService.analyzeRelationships(abstractions, files);
            updateTaskStatus(taskId, 2, 60, "确定章节顺序");

            // 6. 确定章节顺序
            List<Integer> chapterOrder = analysisService.orderChapters(abstractions, relationship);
            updateTaskStatus(taskId, 2, 70, "生成章节内容");

            // 7. 生成章节内容
            List<Chapter> chapters = generateChapters(abstractions, chapterOrder, files);
            updateTaskStatus(taskId, 2, 90, "保存教程");

            // 8. 保存教程
            tutorialService.saveTutorial(task.getProjectId(), abstractions, relationship, chapters);
            updateTaskStatus(taskId, 3, 100, "生成完成");

            log.info("教程生成完成，任务ID: {}", taskId);

        } catch (Exception e) {
            log.error("教程生成失败，任务ID: {}", taskId, e);
            updateTaskStatus(taskId, 4, 0, "生成失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 获取代码文件
     */
    private List<CodeFile> fetchCodeFiles(WikiProjectDO project, WikiGenerateTaskDO task) {
        String dirPath;

        if (project.getSourceType() == 1) {
            // GitHub 仓库
            dirPath = gitHelper.cloneRepository(project.getSourceUrl());
        } else {
            // 本地目录
            dirPath = project.getSourceUrl();
        }

        return gitHelper.readCodeFiles(
                dirPath,
                task.getIncludePatterns(),
                task.getExcludePatterns(),
                task.getMaxFileSize()
        );
    }
}
```

---

## 3. 配置说明

### 3.1 application-wiki.yml

```yaml
# Wiki 插件配置
wiki:
  enabled: true
  
  # 生成配置
  generate:
    # 最大抽象数量
    max-abstractions: 10
    # 最大文件大小（字节）
    max-file-size: 100000
    # 默认语言
    default-language: zh-CN
    # 默认包含模式
    default-include-patterns:
      - "*.java"
      - "*.py"
      - "*.js"
      - "*.ts"
    # 默认排除模式
    default-exclude-patterns:
      - "*/test/*"
      - "*/tests/*"
      - "*/node_modules/*"
      - "*/target/*"
      - "*/build/*"
  
  # GitHub 配置
  github:
    token: ${GITHUB_TOKEN:}
  
  # 异步任务配置
  async:
    core-pool-size: 5
    max-pool-size: 10
    queue-capacity: 100

# Spring AI Alibaba 配置
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
      chat:
        options:
          model: qwen-plus
          temperature: 0.7
          max-tokens: 4000
```

---

## 4. 测试指南

### 4.1 单元测试

```java
@SpringBootTest
class LlmHelperTest {

    @Autowired
    private LlmHelper llmHelper;

    @Test
    void testChat() {
        String prompt = "请介绍一下 Spring Boot";
        String response = llmHelper.chat(prompt);
        
        assertNotNull(response);
        assertTrue(response.length() > 0);
    }
}
```

### 4.2 集成测试

```bash
# 创建项目
curl -X POST http://localhost:8000/api/wiki/project \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Project",
    "sourceType": 1,
    "sourceUrl": "https://github.com/test/repo"
  }'

# 生成教程
curl -X POST http://localhost:8000/api/wiki/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": 1,
    "maxAbstractions": 5
  }'
```

---

**文档维护**: CodeStyle Team  
**最后更新**: 2026-01-29

