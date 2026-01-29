# CodeStyle Wiki 插件快速开始

> 5分钟快速上手 Wiki 教程生成插件
> 
> **文档日期**: 2026-01-29

---

## 🚀 快速开始

### 前置条件

- ✅ JDK 17+
- ✅ Maven 3.8+
- ✅ MySQL 8.0+
- ✅ Redis 7.0+
- ✅ Node.js 18+
- ✅ Gemini API Key（或其他 LLM API Key）

---

## 📦 安装步骤

### 1. 克隆项目

```bash
git clone https://github.com/your-org/codestyle.git
cd codestyle
```

### 2. 配置环境变量

创建 `.env` 文件：

```bash
# LLM API Key
GEMINI_API_KEY=your_gemini_api_key_here

# GitHub Token（可选，用于访问私有仓库）
GITHUB_TOKEN=your_github_token_here
```

### 3. 初始化数据库

```bash
# 执行数据库脚本
mysql -u root -p < codestyle-admin/codestyle-plugin/sql/wiki_tables.sql
```

### 4. 配置应用

编辑 `codestyle-server/src/main/resources/application-local.yml`：

```yaml
# Wiki 模块配置
wiki:
  enabled: true
  llm:
    default-provider: GEMINI
    gemini:
      enabled: true
      api-key: ${GEMINI_API_KEY}
      model: gemini-2.0-flash-exp
```

### 5. 启动后端

```bash
cd codestyle-admin
mvn clean install
cd codestyle-server
mvn spring-boot:run
```

### 6. 启动前端

```bash
cd codestyle-admin-web
pnpm install
pnpm dev
```

---

## 🎯 使用示例

### 示例 1：分析 GitHub 仓库

#### 步骤 1：创建项目

访问：http://localhost:5173/wiki/project

点击"新增项目"，填写信息：

```
项目名称：FastAPI
代码源类型：GitHub
源地址：https://github.com/tiangolo/fastapi
教程语言：中文
```

#### 步骤 2：生成教程

点击"生成教程"按钮，配置生成参数：

```
最大抽象数量：10
包含文件模式：*.py
排除文件模式：*/tests/*, */docs/*
最大文件大小：100KB
```

#### 步骤 3：查看进度

系统会异步执行生成任务，可以在"生成任务"页面查看进度：

```
✓ 获取代码文件 (20%)
✓ 识别核心抽象 (40%)
✓ 分析抽象关系 (60%)
✓ 确定章节顺序 (70%)
⏳ 编写章节内容 (85%)
⏳ 组合教程文件 (95%)
```

#### 步骤 4：查看教程

生成完成后，点击"查看教程"，可以看到：

- 📊 项目摘要
- 🔗 Mermaid 关系图
- 📖 章节列表
- ✏️ 在线编辑

---

### 示例 2：分析本地代码

#### 步骤 1：准备代码

将代码放到服务器目录：

```bash
/data/wiki/repos/my-project/
```

#### 步骤 2：创建项目

```
项目名称：My Project
代码源类型：本地
源地址：/data/wiki/repos/my-project
教程语言：中文
```

#### 步骤 3：生成教程

同上，点击"生成教程"即可。

---

## 🔧 常见问题

### Q1: LLM API 调用失败？

**原因**：API Key 配置错误或网络问题

**解决**：
1. 检查 `.env` 文件中的 API Key 是否正确
2. 检查网络连接
3. 查看日志：`logs/continew-admin.log`

### Q2: 生成任务一直卡在某个节点？

**原因**：LLM 响应超时或代码文件过大

**解决**：
1. 增加超时时间（配置文件中）
2. 减少文件数量（使用排除模式）
3. 减小最大文件大小限制

### Q3: 生成的教程质量不高？

**原因**：提示词不够优化或代码库结构复杂

**解决**：
1. 调整最大抽象数量
2. 使用更强大的 LLM 模型（如 GPT-4）
3. 手动编辑优化教程内容

### Q4: 如何使用本地 LLM（Ollama）？

**配置**：

```yaml
wiki:
  llm:
    default-provider: OLLAMA
    ollama:
      enabled: true
      base-url: http://localhost:11434
      model: llama2
```

**启动 Ollama**：

```bash
ollama serve
ollama pull llama2
```

---

## 📚 API 使用示例

### 使用 cURL 调用 API

#### 1. 创建项目

```bash
curl -X POST http://localhost:18000/api/wiki/project \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "FastAPI",
    "sourceType": 1,
    "sourceUrl": "https://github.com/tiangolo/fastapi",
    "language": "zh-CN"
  }'
```

#### 2. 创建生成任务

```bash
curl -X POST http://localhost:18000/api/wiki/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "projectId": 1,
    "maxAbstractions": 10,
    "includePatterns": ["*.py"],
    "excludePatterns": ["*/tests/*"],
    "maxFileSize": 100000
  }'
```

#### 3. 查询任务状态

```bash
curl -X GET http://localhost:18000/api/wiki/generate/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

响应示例：

```json
{
  "code": "0",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "projectId": 1,
    "status": 2,
    "progress": 85,
    "currentNode": "WriteChapters"
  },
  "success": true
}
```

---

## 🎨 前端集成示例

### 在 Vue 3 中使用

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { createWikiGenerateTask, getWikiGenerateTaskStatus } from '@/apis/wiki/generate'

const projectId = ref(1)
const taskId = ref<number>()
const progress = ref(0)
const status = ref('')

// 创建生成任务
const handleGenerate = async () => {
  const { data } = await createWikiGenerateTask({
    projectId: projectId.value,
    maxAbstractions: 10,
    includePatterns: ['*.py'],
    excludePatterns: ['*/tests/*'],
    maxFileSize: 100000,
  })
  
  taskId.value = data
  
  // 轮询任务状态
  pollTaskStatus()
}

// 轮询任务状态
const pollTaskStatus = () => {
  const timer = setInterval(async () => {
    const { data } = await getWikiGenerateTaskStatus(taskId.value!)
    
    progress.value = data.progress
    status.value = data.currentNode
    
    // 任务完成或失败，停止轮询
    if (data.status === 3 || data.status === 4) {
      clearInterval(timer)
    }
  }, 2000)
}
</script>

<template>
  <div>
    <a-button @click="handleGenerate">生成教程</a-button>
    
    <a-progress
      v-if="taskId"
      :percent="progress"
      :status="status === 'Failed' ? 'danger' : 'normal'"
    />
    
    <p>当前节点：{{ status }}</p>
  </div>
</template>
```

---

## 🔐 权限配置

### 菜单权限

在系统管理 → 菜单管理中添加：

```
菜单名称：Wiki 教程
菜单路径：/wiki
权限标识：wiki:view

  ├─ 项目管理
  │   ├─ 查询：wiki:project:list
  │   ├─ 新增：wiki:project:create
  │   ├─ 修改：wiki:project:update
  │   └─ 删除：wiki:project:delete
  │
  ├─ 教程管理
  │   ├─ 查询：wiki:tutorial:list
  │   ├─ 查看：wiki:tutorial:query
  │   ├─ 发布：wiki:tutorial:publish
  │   └─ 导出：wiki:tutorial:export
  │
  └─ 生成任务
      ├─ 创建：wiki:generate:create
      ├─ 查询：wiki:generate:query
      └─ 取消：wiki:generate:cancel
```

### 角色权限

为角色分配权限：

```
管理员：所有权限
开发者：查询、创建、查看
访客：仅查询
```

---

## 📊 性能优化建议

### 1. 启用 Redis 缓存

```yaml
wiki:
  llm:
    cache:
      enabled: true
      ttl: 86400  # 24小时
```

### 2. 调整线程池大小

```yaml
wiki:
  async:
    core-pool-size: 10
    max-pool-size: 20
    queue-capacity: 200
```

### 3. 限制文件大小

```yaml
wiki:
  generate:
    max-file-size: 50000  # 50KB
    max-abstractions: 8   # 减少抽象数量
```

### 4. 使用本地 LLM

```yaml
wiki:
  llm:
    default-provider: OLLAMA  # 避免网络延迟
```

---

## 🐛 调试技巧

### 1. 查看日志

```bash
tail -f logs/continew-admin.log | grep "Wiki"
```

### 2. 启用 Debug 日志

```yaml
logging:
  level:
    top.codestyle.admin.wiki: DEBUG
```

### 3. 禁用缓存（调试时）

```yaml
wiki:
  llm:
    cache:
      enabled: false
```

### 4. 查看 LLM 请求详情

日志中会记录：
- 提示词内容
- LLM 响应
- 执行时间
- 错误信息

---

## 📖 更多资源

- 📘 [完整迁移规划](./WIKI_PLUGIN_MIGRATION_PLAN.md)
- 📗 [实现指南](./WIKI_IMPLEMENTATION_GUIDE.md)
- 📙 [CodeStyle 最佳实践](../../CODESTYLE_BEST_PRACTICES.md)
- 📕 [CodeStyle 进阶实践](../../CODESTYLE_BEST_PRACTICES_ADVANCED.md)

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

## 📄 许可证

Apache License 2.0

---

**文档维护**: CodeStyle Team  
**最后更新**: 2026-01-29

