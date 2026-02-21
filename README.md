# CodeStyle（码蜂）

<div align="center">
<img src="img/logo.png" alt="CodeStyle Logo" width="120" />

**让历史代码活起来，让 AI 写的更对味！**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.5-brightgreen.svg)](https://vuejs.org/)
[![GitHub Stars](https://img.shields.io/github/stars/codestyle-team/codestyle.svg?style=social&label=Star)](https://github.com/codestyle-team/codestyle)
[![CI](https://github.com/codestyle-team/codestyle/workflows/CI/badge.svg)](https://github.com/codestyle-team/codestyle/actions)

[官网](https://codestyle.top) | [文档](./Codestyle项目文档.md) | [快速开始](#-快速开始) | [贡献指南](./.github/CONTRIBUTING.md) | [问题反馈](https://github.com/codestyle-team/codestyle/issues)

</div>

---

## 📖 项目简介

**CodeStyle（码蜂）** 是一款企业级代码知识库管理系统，通过"官网在线制模 + 轻量化 MCP 插件检索"的创新架构，让 AI 精准理解并复用团队的代码基因。

### 核心理念

致力于构建企业级代码资产库，通过标准化的代码风格模板和智能检索能力，实现代码资产的有效管理和复用，同时解决 AI 代码生成风格不一致的问题，让 AI 成为团队开发的得力助手。

### 相关项目

| 项目 | 作用 | 关联说明 |
|------|------|---------|
| [mcp-codestyle-server](https://github.com/itxaiohanglover/mcp-codestyle-server) | MCP Server & Claude Skill 实现 | 提供 MCP 协议接入，让 AI IDE（Cursor、Windsurf 等）能够调用 CodeStyle 检索能力 |
| [continew-start-skill](https://github.com/itxaiohanglover/continew-start-skill) | ContiNew Admin 项目初始化工具 | 自动化项目初始化，支持自定义品牌和配置，快速搭建基于 ContiNew Admin 的项目 |
| [codestyle-repository](https://github.com/itxaiohanglover/codestyle-repository) | 代码模板仓库 | 存储各类代码模板（CRUD、接口、组件等），供 CodeStyle 检索和使用 |
| [continew-template](https://github.com/itxaiohanglover/continew-template) | 项目模板库 | 提供完整的项目模板，包含最佳实践和常见避坑指南 |

---

## 🎯 核心痛点与解决方案

### 痛点一：模型成本高昂 💰

**现状**
- Claude 等 SOTA 模型效果优秀但成本昂贵
- 企业大规模使用成本压力大

**需求**
- 使用国产平替模型（如 GLM、Doubao-Seed）降低成本
- 同时保持代码生成质量

**CodeStyle 方案：通过经验降低模型门槛**
- 将"框架最佳实践"和"常见避坑指南"沉淀为 Codestyle 模板
- 通过 RAG 检索提供精准上下文，降低对模型能力的依赖
- 让低成本模型也能生成高质量代码

### 痛点二：模型输出不稳定 📉

**现状**
- 代码模型输出质量波动大
- 某次效果差时无法追溯和改进

**需求**
- 将完整调用链路总结为经验，强化后续使用
- 收集失败案例用于微调团队内部模型

**CodeStyle 方案：经验收集**
- 记录每次代码生成的完整上下文和结果
- 将成功案例沉淀为模板，失败案例用于改进
- 支持团队内部模型的持续优化

### 痛点三：团队能力差异悬殊 👥

**现状**
- 使用 AI 编程时，团队效率差异巨大
- 有人提示词写得好，3 次迭代就出结果
- 有人盲目试错，10 次还不行

**需求**
- 让能力强的使用者经验，赋能到能力差的使用者

**CodeStyle 方案：经验共享**
- 将优秀开发者的经验沉淀为模板
- 通过 RAG 检索自动召回相关经验
- 用 MCP 协议 / Skill 接入 IDE，无缝集成开发流程

### 💡 根本差距：有没有复用过往成功经验

**CodeStyle 的核心价值**
1. 将"框架最佳实践"和"常见避坑指南"沉淀为 Codestyle 模板
2. 通过 RAG 检索，在编码时自动召回相关经验
3. 用 MCP 协议 / Skill 接入 IDE，无缝集成开发流程

### ✨ 核心特性

- 🚀 **3 分钟接入** - 零配置即插即用，不改造现有开发流程
- 🔒 **本地组装安全** - 模板与代码在本地组装，企业代码零外泄风险
- 🎨 **可视化制模** - 官网在线制作风格模板，支持 CRUD、接口、组件等场景
- 🔌 **多 IDE 支持** - 支持 Cursor、Windsurf、Trae、Cline 等主流 AI IDE
- 👥 **多租户架构** - 支持团队/企业级多租户隔离，满足大型组织需求
- 📚 **智能检索** - 基于 Elasticsearch 的全文检索，快速定位历史代码
- 🔧 **代码生成器** - 自动生成符合团队风格的前后端代码
- 🌐 **多语言支持** - 支持 Java、JavaScript、TypeScript、Python 等
- 📊 **数据分析** - 提供代码资产使用情况分析
- 🔍 **风格检查** - 自动检测代码风格偏差，确保团队代码风格统一

---

## 🏗️ 系统架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                      AI IDE 层                               │
│  Cursor / Windsurf / Trae / Cline / Claude Desktop          │
└─────────────────────────────────────────────────────────────┘
                            ↓ MCP 协议 / Skill
┌─────────────────────────────────────────────────────────────┐
│                   MCP Server 层                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  mcp-codestyle-server (Spring AI)                    │   │
│  │  - 接收检索请求                                       │   │
│  │  - 渐进式召回策略                                     │   │
│  │  - 结果返回                                           │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓ HTTP API
┌─────────────────────────────────────────────────────────────┐
│                   CodeStyle 核心层                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  检索插件     │  │  Wiki 插件    │  │  代码生成器   │      │
│  │  (Search)    │  │  (Wiki)      │  │  (Generator) │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  多租户       │  │  任务调度     │  │  开放能力     │      │
│  │  (Tenant)    │  │  (Schedule)  │  │  (Open)      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   数据存储层                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  MySQL       │  │  Redis       │  │ Elasticsearch │      │
│  │  (业务数据)   │  │  (缓存)      │  │  (全文检索)   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   模板仓库层                                 │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  codestyle-repository (Git)                          │   │
│  │  - 代码模板（CRUD、接口、组件）                       │   │
│  │  - meta.json 索引                                    │   │
│  │  - 最佳实践和避坑指南                                 │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 核心模块说明

#### 🔍 检索插件（codestyle-plugin-search）

**功能**：基于 Elasticsearch 的渐进式 RAG 检索系统

**核心特性**
- ✅ 两阶段召回策略（轻量预览 → 详细加载）
- ✅ meta.json 索引机制（避免频繁 I/O）
- ✅ 关键词和向量混合检索
- ✅ SPI 扩展机制

**实现细节**
```
阶段一：轻量预览
├─ 查询 meta.json 索引
├─ 返回 description 列表（轻量）
└─ 用户在 IDE 中选择目标

阶段二：详细加载
├─ 异步同步缺失文件到本地
└─ 返回完整模板内容
```

**收益**：减少 80% 无效上下文消耗，提升准确率

**技术文档**
- [DESIGN.md](./codestyle-admin/codestyle-plugin/codestyle-plugin-search/DESIGN.md) - 架构设计
- [IMPLEMENTATION.md](./codestyle-admin/codestyle-plugin/codestyle-plugin-search/IMPLEMENTATION.md) - 实现细节
- [SPI_EXTENSION_GUIDE.md](./codestyle-admin/codestyle-plugin/codestyle-plugin-search/SPI_EXTENSION_GUIDE.md) - 扩展指南

#### 📚 知识库插件（codestyle-plugin-wiki）

**功能**：代码知识库管理系统

**核心特性**
- ✅ Markdown 文档管理
- ✅ 代码片段存储和检索
- ✅ 版本控制和历史追溯
- ✅ 协作编辑和评论

**实现细节**
- 基于 Git 的版本控制
- 支持 Markdown 渲染和代码高亮
- 提供 RESTful API 接口

**技术文档**
- [DESIGN.md](./codestyle-admin/codestyle-plugin/codestyle-plugin-wiki/docs/DESIGN.md) - 设计文档
- [API.md](./codestyle-admin/codestyle-plugin/codestyle-plugin-wiki/docs/API.md) - API 文档

### meta.json 索引设计

借鉴 Maven 坐标体系，集中管理元信息：

```json
{
  "groupId": "RuoYI",
  "artifactId": "CRUD",
  "version": "1.0",
  "files": [{
    "filePath": "src/main/java/controller",
    "filename": "Controller.java.ftl",
    "description": "CRUD 控制器模板",
    "sha256": "722f185c...",
    "inputVariables": [
      {
        "variableName": "packageName",
        "variableType": "String",
        "variableComment": "项目根包名",
        "example": "com.air.order"
      }
    ]
  }]
}
```

**设计要点**
- `version + sha256`：版本控制和完整性校验
- `inputVariables`：模板需要的参数（类型、说明、示例）
- `description`：快速预览，减少 I/O

---

## 🚀 快速开始

### 环境要求

| 软件 | 版本要求 |
|------|---------|
| JDK | 17+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Redis | 5.0+ |
| Elasticsearch | 8.x |

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/codestyle-team/codestyle.git
   cd codestyle
   ```

2. **配置数据库**
   ```bash
   # 创建数据库
   mysql -u root -p
   CREATE DATABASE codestyle DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   
   # 导入初始化脚本
   mysql -u root -p codestyle < docs/sql/init.sql
   ```

3. **配置应用**
   ```bash
   # 修改后端配置
   cd codestyle-admin/codestyle-server/src/main/resources
   cp application-dev.yml.example application-dev.yml
   # 编辑 application-dev.yml，配置数据库、Redis、ES 连接信息
   ```

4. **启动后端**
   ```bash
   cd codestyle-admin
   mvn clean package -DskipTests
   java -jar codestyle-server/target/codestyle-server.jar
   ```

5. **启动前端**
   ```bash
   cd codestyle-admin-web
   pnpm install
   pnpm dev
   ```

6. **访问应用**
   - 前端地址：http://localhost:5173
   - 后端地址：http://localhost:8080
   - 默认账号：admin / admin123

### Docker 部署

```bash
cd codestyle-admin/docker
docker-compose up -d
```

---

## 📚 文档

- [项目总结](./PROJECT_SUMMARY.md) - 项目架构和技术栈详解
- [项目文档](./Codestyle项目文档.md) - 完整项目文档
- [贡献指南](./.github/CONTRIBUTING.md) - 如何参与贡献
- [安全政策](./.github/SECURITY.md) - 安全问题报告

---

## 🛠️ 技术栈

### 后端

- **框架**: Spring Boot 3.x + ContiNew Starter 2.14.0
- **ORM**: MyBatis-Plus
- **数据库**: MySQL 8.0 + Redis 5.0
- **搜索**: Elasticsearch 8.x
- **安全**: Sa-Token
- **构建**: Maven

### 前端

- **框架**: Vue 3.5 + TypeScript 5.0
- **UI**: Arco Design 2.57
- **构建**: Vite 5.1
- **状态管理**: Pinia 2.0
- **路由**: Vue Router 4.3
- **包管理**: pnpm 8

---

## 🤝 参与贡献

我们欢迎所有形式的贡献！请查看 [贡献指南](./.github/CONTRIBUTING.md) 了解详情。

### 贡献者

感谢所有为 CodeStyle 做出贡献的开发者！

<a href="https://github.com/codestyle-team/codestyle/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=codestyle-team/codestyle" />
</a>

---

## 📄 许可证

本项目采用 [Apache 2.0 许可证](./LICENSE)。

---

## 🌟 Star History

[![Star History Chart](https://api.star-history.com/svg?repos=itxaiohanglover/codestyle&type=date&legend=top-left)](https://www.star-history.com/#itxaiohanglover/codestyle&type=date&legend=top-left)

---

## 💬 交流与支持

- 💬 [讨论区](https://github.com/codestyle-team/codestyle/discussions) - 提问和交流
- 🐛 [问题反馈](https://github.com/codestyle-team/codestyle/issues) - 报告 Bug 和建议
- 📧 邮件：support@codestyle.top

---

<div align="center">

**如果这个项目对您有帮助，请给我们一个 ⭐️ Star！**

Made with ❤️ by CodeStyle Team

</div>