# CodeStyle Wiki 插件

> 基于 Spring AI Alibaba 的代码库知识构建与教程生成插件
> 
> **版本**: 1.0.0  
> **文档日期**: 2026-01-29

---

## 📖 项目概述

CodeStyle Wiki 插件是一个基于 AI 的代码库知识构建工具，能够自动分析 GitHub 仓库或本地代码目录，识别核心抽象概念，生成适合初学者的教程文档。

### ✨ 核心特性

- 🔍 **智能代码分析**：自动识别核心抽象概念和关系
- 📚 **教程生成**：生成 Markdown 格式的教程文档
- 🌐 **多语言支持**：支持中文、英文等多语言教程
- 🎨 **可视化**：生成 Mermaid 关系图
- 🔐 **企业级**：权限控制、多租户、版本管理
- 🚀 **高性能**：异步处理、Redis 缓存

---

## 🏗️ 技术架构

### 分层架构

```
Controller 层 (REST API)
    ↓
Service 层 (业务逻辑)
    ↓
Helper 层 (工具辅助)
    ↓
Mapper 层 (数据访问)
```

### 核心组件

- **WikiProjectService**: 项目管理服务
- **WikiTutorialService**: 教程管理服务
- **WikiGenerateService**: 教程生成服务
- **WikiAnalysisService**: 代码分析服务
- **LlmHelper**: LLM 调用辅助类
- **GitHelper**: Git 操作辅助类
- **MarkdownHelper**: Markdown 处理辅助类

---

## 🚀 快速开始

### 1. 配置

在 `application-local.yml` 中添加：

```yaml
# Wiki 插件配置
wiki:
  enabled: true
  
  # Spring AI Alibaba 配置
  spring:
    ai:
      dashscope:
        api-key: ${DASHSCOPE_API_KEY}
        chat:
          options:
            model: qwen-plus
  
  # GitHub 配置
  github:
    token: ${GITHUB_TOKEN}
  
  # 生成配置
  generate:
    max-abstractions: 10
    max-file-size: 100000
    default-language: zh-CN
```

### 2. 使用示例

#### 创建项目

```bash
POST /api/wiki/project
{
  "name": "FastAPI",
  "sourceType": 1,
  "sourceUrl": "https://github.com/tiangolo/fastapi",
  "language": "zh-CN"
}
```

#### 生成教程

```bash
POST /api/wiki/generate
{
  "projectId": 1,
  "maxAbstractions": 10,
  "includePatterns": ["*.py"],
  "excludePatterns": ["*/tests/*"]
}
```

---

## 📊 数据库设计

### 核心表

- `wiki_project`: 项目表
- `wiki_tutorial`: 教程表
- `wiki_chapter`: 章节表
- `wiki_abstraction`: 抽象表
- `wiki_relationship`: 关系表
- `wiki_generate_task`: 生成任务表

详见：[DATABASE.md](./docs/DATABASE.md)

---

## 📚 文档导航

- [设计文档](./docs/DESIGN.md) - 详细的架构设计
- [实现指南](./docs/IMPLEMENTATION.md) - 开发实现指南
- [API 文档](./docs/API.md) - REST API 接口文档
- [数据库设计](./docs/DATABASE.md) - 数据库表结构

---

## 🛠️ 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.x | 应用框架 |
| Spring AI Alibaba | 1.0.0-M3.2 | AI 集成框架 |
| MyBatis-Plus | 3.5.x | ORM 框架 |
| Redis | 7.x | 缓存 |
| JGit | 6.8.x | Git 操作 |

---

## 📄 许可证

Apache License 2.0

---

**开发团队**: CodeStyle Team  
**最后更新**: 2026-01-29
