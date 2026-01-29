# CodeStyle Wiki 插件实现指南

> 技术实现细节和代码示例
> 
> **文档日期**: 2026-01-29

---

## 📋 目录

1. [核心接口实现](#1-核心接口实现)
2. [工作流节点实现](#2-工作流节点实现)
3. [LLM 提供者实现](#3-llm-提供者实现)
4. [前端组件实现](#4-前端组件实现)
5. [配置和部署](#5-配置和部署)

---

## 1. 核心接口实现

### 1.1 LlmProvider 接口

```java
package top.codestyle.admin.wiki.provider.spi;

import top.codestyle.admin.wiki.enums.LlmProviderType;

/**
 * LLM 提供者接口
 *
 * @author CodeStyle Team
 */
public interface LlmProvider {

    /**
     * 调用 LLM
     *
     * @param prompt       提示词
     * @param systemPrompt 系统提示词
     * @param useCache     是否使用缓存
     * @return LLM 响应
     */
    String call(String prompt, String systemPrompt, boolean useCache);

    /**
     * 获取提供者类型
     *
     * @return 提供者类型
     */
    LlmProviderType getType();

    /**
     * 是否可用
     *
     * @return true-可用 false-不可用
     */
    boolean isAvailable();
}
```

### 1.2 CodeSourceProvider 接口

```java
package top.codestyle.admin.wiki.provider.spi;

import top.codestyle.admin.wiki.model.CodeFile;
import java.util.List;

/**
 * 代码源提供者接口
 *
 * @author CodeStyle Team
 */
public interface CodeSourceProvider {

    /**
     * 获取代码文件列表
     *
     * @param sourceUrl        源地址
     * @param includePatterns  包含模式
     * @param excludePatterns  排除模式
     * @param maxFileSize      最大文件大小
     * @return 代码文件列表
     */
    List<CodeFile> fetchFiles(
        String sourceUrl,
        List<String> includePatterns,
        List<String> excludePatterns,
        long maxFileSize
    );

    /**
     * 获取提供者类型
     *
     * @return 提供者类型
     */
    CodeSourceType getType();
}
```

### 1.3 WorkflowContext 上下文

```java
package top.codestyle.admin.wiki.workflow;

import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作流上下文
 *
 * @author CodeStyle Team
 */
@Data
public class WorkflowContext {

    /** 任务 ID */
    private Long taskId;

    /** 项目 ID */
    private Long projectId;

    /** 共享数据 */
    private Map<String, Object> shared = new ConcurrentHashMap<>();

    /** 当前节点 */
    private String currentNode;

    /** 进度（0-100） */
    private Integer progress = 0;

    /** 是否取消 */
    private volatile boolean cancelled = false;

    /**
     * 设置共享数据
     */
    public void put(String key, Object value) {
        shared.put(key, value);
    }

    /**
     * 获取共享数据
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) shared.get(key);
    }

    /**
     * 更新进度
     */
    public void updateProgress(String nodeName, int progress) {
        this.currentNode = nodeName;
        this.progress = progress;
    }
}
```

---

## 2. 工作流节点实现

### 2.1 BaseNode 基类

```java
package top.codestyle.admin.wiki.workflow.nodes;

import lombok.extern.slf4j.Slf4j;
import top.codestyle.admin.wiki.workflow.WorkflowContext;

/**
 * 工作流节点基类
 *
 * @author CodeStyle Team
 */
@Slf4j
public abstract class BaseNode {

    /**
     * 执行节点逻辑
     *
     * @param context 工作流上下文
     */
    public void execute(WorkflowContext context) {
        String nodeName = getName();
        log.info("开始执行节点: {}", nodeName);

        int maxRetries = getMaxRetries();
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                // 检查是否取消
                if (context.isCancelled()) {
                    log.warn("任务已取消，停止执行节点: {}", nodeName);
                    return;
                }

                // 执行节点逻辑
                doExecute(context);

                log.info("节点执行成功: {}", nodeName);
                return;

            } catch (Exception e) {
                attempt++;
                log.error("节点执行失败 (尝试 {}/{}): {}", attempt, maxRetries, nodeName, e);

                if (attempt >= maxRetries) {
                    throw new RuntimeException("节点执行失败: " + nodeName, e);
                }

                // 等待后重试
                try {
                    Thread.sleep(getRetryWaitTime());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("节点执行被中断: " + nodeName, ie);
                }
            }
        }
    }

    /**
     * 执行节点逻辑（子类实现）
     *
     * @param context 工作流上下文
     */
    protected abstract void doExecute(WorkflowContext context);

    /**
     * 节点名称
     *
     * @return 节点名称
     */
    public abstract String getName();

    /**
     * 最大重试次数
     *
     * @return 重试次数
     */
    protected int getMaxRetries() {
        return 3;
    }

    /**
     * 重试等待时间（毫秒）
     *
     * @return 等待时间
     */
    protected long getRetryWaitTime() {
        return 5000;
    }
}
```

### 2.2 IdentifyAbstractionsNode 实现

```java
package top.codestyle.admin.wiki.workflow.nodes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.codestyle.admin.wiki.model.CodeFile;
import top.codestyle.admin.wiki.model.Abstraction;
import top.codestyle.admin.wiki.provider.spi.LlmProvider;
import top.codestyle.admin.wiki.workflow.WorkflowContext;
import top.codestyle.admin.wiki.util.PromptUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.util.List;

/**
 * 识别抽象节点
 *
 * @author CodeStyle Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdentifyAbstractionsNode extends BaseNode {

    private final LlmProvider llmProvider;
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @Override
    protected void doExecute(WorkflowContext context) {
        // 1. 获取代码文件
        List<CodeFile> files = context.get("files");
        if (files == null || files.isEmpty()) {
            throw new IllegalStateException("代码文件列表为空");
        }

        // 2. 构建提示词
        String prompt = PromptUtils.buildIdentifyAbstractionsPrompt(
            files,
            context.get("maxAbstractions")
        );

        // 3. 调用 LLM
        String response = llmProvider.call(
            prompt,
            "You are a code analysis expert.",
            true
        );

        // 4. 解析响应
        List<Abstraction> abstractions = parseAbstractions(response, files.size());

        // 5. 保存到上下文
        context.put("abstractions", abstractions);
        context.updateProgress(getName(), 30);

        log.info("识别到 {} 个抽象概念", abstractions.size());
    }

    @Override
    public String getName() {
        return "IdentifyAbstractions";
    }

    @Override
    protected int getMaxRetries() {
        return 5;
    }

    /**
     * 解析抽象列表
     */
    private List<Abstraction> parseAbstractions(String yamlResponse, int fileCount) {
        try {
            // 提取 YAML 内容
            String yaml = extractYaml(yamlResponse);

            // 解析为对象
            AbstractionsWrapper wrapper = yamlMapper.readValue(
                yaml,
                AbstractionsWrapper.class
            );

            // 验证
            validateAbstractions(wrapper.getAbstractions(), fileCount);

            return wrapper.getAbstractions();

        } catch (Exception e) {
            throw new RuntimeException("解析抽象列表失败", e);
        }
    }

    /**
     * 提取 YAML 内容
     */
    private String extractYaml(String response) {
        int start = response.indexOf("```yaml");
        int end = response.lastIndexOf("```");

        if (start == -1 || end == -1) {
            throw new IllegalArgumentException("响应中未找到 YAML 内容");
        }

        return response.substring(start + 7, end).trim();
    }

    /**
     * 验证抽象列表
     */
    private void validateAbstractions(List<Abstraction> abstractions, int fileCount) {
        if (abstractions == null || abstractions.isEmpty()) {
            throw new IllegalArgumentException("抽象列表为空");
        }

        for (Abstraction abstraction : abstractions) {
            if (abstraction.getName() == null || abstraction.getName().isEmpty()) {
                throw new IllegalArgumentException("抽象名称不能为空");
            }

            if (abstraction.getFiles() == null || abstraction.getFiles().isEmpty()) {
                throw new IllegalArgumentException("抽象必须关联至少一个文件");
            }

            // 验证文件索引
            for (Integer fileIndex : abstraction.getFiles()) {
                if (fileIndex < 0 || fileIndex >= fileCount) {
                    throw new IllegalArgumentException(
                        "文件索引超出范围: " + fileIndex
                    );
                }
            }
        }
    }

    @Data
    private static class AbstractionsWrapper {
        private List<Abstraction> abstractions;
    }
}
```

---

## 3. LLM 提供者实现

### 3.1 GeminiLlmProvider 实现

```java
package top.codestyle.admin.wiki.provider.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import top.codestyle.admin.wiki.config.WikiProperties;
import top.codestyle.admin.wiki.enums.LlmProviderType;
import top.codestyle.admin.wiki.provider.spi.LlmProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

/**
 * Gemini LLM 提供者
 *
 * @author CodeStyle Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiLlmProvider implements LlmProvider {

    private final WikiProperties wikiProperties;
    private final OkHttpClient httpClient;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public String call(String prompt, String systemPrompt, boolean useCache) {
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
        String requestBody = buildRequestBody(prompt, systemPrompt);

        // 3. 调用 API
        String response = callApi(requestBody);

        // 4. 解析响应
        String content = parseResponse(response);

        // 5. 缓存结果
        if (useCache) {
            String cacheKey = buildCacheKey(prompt, systemPrompt);
            long ttl = wikiProperties.getLlm().getCache().getTtl();
            redisTemplate.opsForValue().set(cacheKey, content, ttl, TimeUnit.SECONDS);
        }

        return content;
    }

    @Override
    public LlmProviderType getType() {
        return LlmProviderType.GEMINI;
    }

    @Override
    public boolean isAvailable() {
        WikiProperties.LlmProperties.GeminiProperties gemini = 
            wikiProperties.getLlm().getGemini();
        return gemini.isEnabled() && gemini.getApiKey() != null;
    }

    /**
     * 构建请求体
     */
    private String buildRequestBody(String prompt, String systemPrompt) {
        try {
            Map<String, Object> request = new HashMap<>();
            
            List<Map<String, Object>> contents = new ArrayList<>();
            
            // 系统提示词
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", systemPrompt))
                ));
            }
            
            // 用户提示词
            contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", prompt))
            ));
            
            request.put("contents", contents);
            
            return objectMapper.writeValueAsString(request);
            
        } catch (Exception e) {
            throw new RuntimeException("构建请求体失败", e);
        }
    }

    /**
     * 调用 API
     */
    private String callApi(String requestBody) {
        try {
            WikiProperties.LlmProperties.GeminiProperties gemini = 
                wikiProperties.getLlm().getGemini();

            String url = String.format(
                "%s/v1beta/models/%s:generateContent?key=%s",
                gemini.getBaseUrl(),
                gemini.getModel(),
                gemini.getApiKey()
            );

            Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(
                    requestBody,
                    MediaType.parse("application/json")
                ))
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException(
                        "API 调用失败: " + response.code()
                    );
                }

                return response.body().string();
            }

        } catch (Exception e) {
            throw new RuntimeException("调用 Gemini API 失败", e);
        }
    }

    /**
     * 解析响应
     */
    private String parseResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            return root.at("/candidates/0/content/parts/0/text").asText();
        } catch (Exception e) {
            throw new RuntimeException("解析响应失败", e);
        }
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(String prompt, String systemPrompt) {
        String combined = systemPrompt + "|" + prompt;
        return "wiki:llm:cache:" + DigestUtils.md5Hex(combined);
    }
}
```

---

## 4. 前端组件实现

### 4.1 项目列表页面

```vue
<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Message } from '@arco-design/web-vue'
import { listWikiProject, deleteWikiProject } from '@/apis/wiki/project'
import type { WikiProjectQuery, WikiProjectResp } from '@/apis/wiki/project'

// 查询表单
const queryForm = reactive<WikiProjectQuery>({
  name: '',
  sourceType: undefined,
  status: undefined,
})

// 表格数据
const dataList = ref<WikiProjectResp[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

// 查询
const search = async () => {
  loading.value = true
  try {
    const { data } = await listWikiProject({
      ...queryForm,
      page: pagination.current,
      size: pagination.pageSize,
    })
    dataList.value = data.list
    pagination.total = data.total
  } finally {
    loading.value = false
  }
}

// 删除
const handleDelete = async (record: WikiProjectResp) => {
  await deleteWikiProject([record.id])
  Message.success('删除成功')
  search()
}

// 生成教程
const handleGenerate = (record: WikiProjectResp) => {
  router.push(`/wiki/generate?projectId=${record.id}`)
}

// 初始化
search()
</script>

<template>
  <GiTable
    row-key="id"
    :data="dataList"
    :loading="loading"
    :pagination="pagination"
    @refresh="search"
  >
    <template #top>
      <GiForm
        v-model="queryForm"
        search
        :columns="queryFormColumns"
        @search="search"
        @reset="reset"
      />
    </template>

    <template #toolbar-left>
      <a-button v-permission="['wiki:project:create']" type="primary" @click="onAdd">
        <template #icon><icon-plus /></template>
        新增项目
      </a-button>
    </template>

    <template #name="{ record }">
      <a-link @click="onView(record)">{{ record.name }}</a-link>
    </template>

    <template #sourceType="{ record }">
      <a-tag v-if="record.sourceType === 1" color="blue">GitHub</a-tag>
      <a-tag v-else color="green">本地</a-tag>
    </template>

    <template #status="{ record }">
      <GiCellStatus :status="record.status" />
    </template>

    <template #action="{ record }">
      <a-space>
        <a-link v-permission="['wiki:generate:create']" @click="handleGenerate(record)">
          生成教程
        </a-link>
        <a-link v-permission="['wiki:project:update']" @click="onUpdate(record)">
          修改
        </a-link>
        <a-link
          v-permission="['wiki:project:delete']"
          status="danger"
          @click="handleDelete(record)"
        >
          删除
        </a-link>
      </a-space>
    </template>
  </GiTable>
</template>
```

### 4.2 教程详情页面

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getWikiTutorialDetail } from '@/apis/wiki/tutorial'
import type { WikiTutorialDetailResp } from '@/apis/wiki/tutorial'
import MarkdownIt from 'markdown-it'
import mermaid from 'mermaid'

const route = useRoute()
const tutorialId = route.params.id as string

const tutorial = ref<WikiTutorialDetailResp>()
const loading = ref(false)
const activeChapter = ref(0)

const md = new MarkdownIt()

// 加载教程详情
const loadTutorial = async () => {
  loading.value = true
  try {
    const { data } = await getWikiTutorialDetail(tutorialId)
    tutorial.value = data
    
    // 渲染 Mermaid 图表
    nextTick(() => {
      mermaid.init(undefined, '.mermaid')
    })
  } finally {
    loading.value = false
  }
}

// 渲染 Markdown
const renderMarkdown = (content: string) => {
  return md.render(content)
}

onMounted(() => {
  loadTutorial()
})
</script>

<template>
  <div v-loading="loading" class="tutorial-detail">
    <div class="tutorial-header">
      <h1>{{ tutorial?.projectName }}</h1>
      <p>{{ tutorial?.summary }}</p>
    </div>

    <div class="tutorial-diagram">
      <div class="mermaid" v-html="tutorial?.mermaidDiagram" />
    </div>

    <div class="tutorial-content">
      <a-layout>
        <a-layout-sider width="250px">
          <a-menu
            v-model:selected-keys="activeChapter"
            :style="{ width: '100%' }"
          >
            <a-menu-item
              v-for="(chapter, index) in tutorial?.chapters"
              :key="index"
            >
              {{ chapter.title }}
            </a-menu-item>
          </a-menu>
        </a-layout-sider>

        <a-layout-content>
          <div
            v-if="tutorial?.chapters[activeChapter]"
            class="chapter-content markdown-body"
            v-html="renderMarkdown(tutorial.chapters[activeChapter].content)"
          />
        </a-layout-content>
      </a-layout>
    </div>
  </div>
</template>

<style scoped>
.tutorial-detail {
  padding: 20px;
}

.tutorial-header {
  margin-bottom: 30px;
}

.tutorial-diagram {
  margin-bottom: 30px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 4px;
}

.chapter-content {
  padding: 20px;
}
</style>
```

---

## 5. 配置和部署

### 5.1 数据库初始化脚本

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `continew_admin` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `continew_admin`;

-- Wiki 项目表
CREATE TABLE `wiki_project` (
  `id` bigint NOT NULL COMMENT 'ID',
  `name` varchar(100) NOT NULL COMMENT '项目名称',
  `description` varchar(500) COMMENT '项目描述',
  `source_type` tinyint NOT NULL COMMENT '代码源类型（1:GitHub 2:本地）',
  `source_url` varchar(500) COMMENT '源地址',
  `language` varchar(20) DEFAULT 'zh-CN' COMMENT '教程语言',
  `status` tinyint DEFAULT 1 COMMENT '状态（1:启用 2:禁用）',
  `tenant_id` bigint DEFAULT 0 COMMENT '租户ID',
  `create_user` bigint COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user` bigint COMMENT '修改人',
  `update_time` datetime ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` bigint DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB COMMENT='Wiki项目表';

-- 其他表省略...
```

### 5.2 环境变量配置

```bash
# .env
# Gemini API Key
GEMINI_API_KEY=your_gemini_api_key_here

# GitHub Token
GITHUB_TOKEN=your_github_token_here

# OpenAI API Key (可选)
OPENAI_API_KEY=your_openai_api_key_here
```

### 5.3 Docker 部署

```yaml
# docker-compose.yml
version: '3'
services:
  codestyle-admin:
    image: codestyle/admin:latest
    ports:
      - "18000:18000"
    environment:
      - GEMINI_API_KEY=${GEMINI_API_KEY}
      - GITHUB_TOKEN=${GITHUB_TOKEN}
    volumes:
      - ./data:/data
    depends_on:
      - mysql
      - redis
```

---

**文档维护**: CodeStyle Team  
**最后更新**: 2026-01-29

