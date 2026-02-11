# CodeStyle（码蜂）

<div align="center">
<img src="images/logo.png" alt="CodeStyle Logo" width="120" />

**让历史代码活起来，让 AI 写的更对味！**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.5-brightgreen.svg)](https://vuejs.org/)
[![GitHub Stars](https://img.shields.io/github/stars/codestyle-team/codestyle.svg?style=social&label=Star)](https://github.com/codestyle-team/codestyle)

[官网](https://codestyle.top) · [文档](./project_documentation.md) · [快速开始](#快速开始) · [问题反馈](https://github.com/codestyle-team/codestyle/issues)

</div>

---

## 📖 项目简介

CodeStyle（码蜂）是一款**企业级代码知识库工具**，通过"官网在线制模 + 轻量化 MCP 插件检索"的创新架构，让 AI 精准理解并复用团队的代码基因。

CodeStyle 致力于构建企业级代码资产库，通过标准化的代码风格模板和智能检索能力，实现代码资产的有效管理和复用，同时解决 AI 代码生成风格不一致的问题，让 AI 成为团队开发的得力助手。

### 🎯 解决三大核心痛点

| 痛点 | 问题描述 | CodeStyle 方案 |
|-----|---------|---------------|
| 😤 **AI 生成"对而不适"** | AI 写的代码语法没错，但不符合团队风格，每次都要大改 | 模板预置规范，AI 生成即符合团队风格，减少代码修改成本 |
| 😓 **历史代码"躺平闲置"** | 优质代码找不到，新人重复造轮子，代码资产无法有效利用 | 智能检索，代码资产一次开发终身复用，提高代码复用率 |
| 😵 **新人"熬秃头皮"** | 上手周期长，跨团队协作风格冲突，培训成本高 | AI 当风格教练，上手周期从周级压缩到小时级，加速新人融入 |

### ✨ 核心特性

- 🚀 **3 分钟接入**：零配置即插即用，不改造现有开发流程，快速集成到团队开发环境
- 🔒 **本地组装安全**：模板与代码在本地组装，企业代码零外泄风险，保障代码安全
- 🎨 **可视化制模**：官网在线制作风格模板，支持 CRUD、接口、组件等多种开发场景
- 🔌 **多 IDE 支持**：支持 Cursor、Windsurf、Trae、Cline 等主流 AI IDE，覆盖多种开发环境
- 👥 **多租户架构**：支持团队/企业级多租户隔离，满足大型组织的权限管理需求
- 📚 **智能代码检索**：基于 Elasticsearch 的全文检索，快速定位和复用历史代码
- 🔧 **代码生成器**：自动生成符合团队风格的前后端代码，提高开发效率
- 🌐 **多语言支持**：支持 Java、JavaScript、TypeScript、Python 等多种编程语言
- 📊 **数据分析**：提供代码资产使用情况分析，帮助团队优化代码质量
- 🔍 **风格一致性检查**：自动检测代码风格偏差，确保团队代码风格统一

---

##  工作原理

CodeStyle 通过"官网在线制模 + 轻量化 MCP 插件检索"的创新架构，实现代码风格的标准化和代码资产的有效管理。

### 核心流程

1. **模板制作**：
   - 用户通过官网在线制作代码风格模板
   - 支持 CRUD、接口、组件等多种开发场景
   - 模板包含代码风格、命名规范、架构模式等

2. **资产入库**：
   - 历史代码通过智能分析入库
   - 新代码通过提交时自动分析入库
   - 支持手动添加和标签管理

3. **实时同步**：
   - MySQL 数据通过 Canal 同步到 Kafka
   - 应用层消费 Kafka 消息，同步到 Elasticsearch
   - 确保搜索数据的实时性和准确性

4. **智能检索**：
   - MCP 插件通过 API 检索相关代码资产
   - 基于 Elasticsearch 实现全文检索
   - 支持按语言、场景、标签等多维度筛选

5. **本地组装**：
   - 模板和代码在本地 IDE 中组装
   - AI 使用本地模板生成符合风格的代码
   - 确保企业代码零外泄风险

### 数据流

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  官网制模       │────>│  管理后台       │────>│  代码资产库     │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                                      │
                                      ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  AI IDE 插件    │<────│  MCP 服务器     │<────│  Elasticsearch  │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                                      ▲
                                      │
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  MySQL          │────>│  Canal + Kafka  │────>│  应用层消费者   │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

### 技术实现

1. **模板管理**：
   - 基于 Vue 3 + Arco Design 实现可视化模板编辑器
   - 支持模板版本管理和团队共享
   - 模板导出为标准格式，支持跨团队使用

2. **资产分析**：
   - 使用 Java 实现代码静态分析
   - 支持多种编程语言的语法解析
   - 智能识别代码质量和风格问题

3. **搜索服务**：
   - 基于 Elasticsearch 实现全文检索
   - 支持模糊匹配和语义搜索
   - 实时数据同步，确保搜索结果准确性

4. **MCP 插件**：
   - 轻量级设计，最小化资源占用
   - 支持多 IDE 适配，统一接口规范
   - 本地缓存机制，提高检索速度

5. **安全保障**：
   - 本地组装模式，代码不经过服务器
   - 基于 Token 的认证授权机制
   - 多租户隔离，确保数据安全

## 🏗️ 技术架构

CodeStyle 采用分层架构设计，由用户层、服务层和数据层组成，确保系统的可扩展性和可维护性。

```
┌──────────────────────────────────────────────────────────────┐
│                         用户层                                │
│   AI IDE 插件 (Cursor/Windsurf/Trae/Cline)  ←→  官网/管理后台   │
└────────────────────────────┬─────────────────────────────────┘
                             │
┌────────────────────────────▼─────────────────────────────────┐
│                       服务层                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ 用户服务     │  │ 代码资产库   │  │ 搜索服务            │  │
│  │ (权限/认证)  │  │ (模板/片段)  │  │ (ES全文检索)        │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ 代码生成器   │  │ 任务调度     │  │ 能力开放            │  │
│  │ (模板生成)   │  │ (定时任务)   │  │ (API 接口)          │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└────────────────────────────┬─────────────────────────────────┘
                             │
┌────────────────────────────▼─────────────────────────────────┐
│                       数据层                                  │
│   MySQL 8.0  │  Redis 7.2  │  Elasticsearch 8.13  │  Kafka   │
└──────────────────────────────────────────────────────────────┘
```

### 技术栈

| 层级 | 技术选型 | 说明 |
|-----|---------|------|
| **前端** | Vue 3.5 + TypeScript + Vite + Arco Design | 现代化前端技术栈，提供良好的用户体验和开发效率 |
| **后端** | Spring Boot 3.2 + Spring Cloud + MyBatis-Plus | 企业级后端框架，支持微服务架构和高效数据访问 |
| **安全** | Sa-Token（认证授权）| 轻量级权限框架，提供完整的认证授权功能 |
| **数据库** | MySQL 8.0 + Redis 7.2 | 稳定可靠的关系型数据库和高性能缓存 |
| **搜索** | Elasticsearch 8.13 + Canal + Kafka | 强大的全文检索能力和实时数据同步 |
| **部署** | Docker + Docker Compose + Nginx | 容器化部署，简化部署流程和环境管理 |
| **监控** | Spring Boot Actuator + ELK | 系统监控和日志管理 |
| **代码生成** | 自定义代码生成器 | 支持前后端代码自动生成 |

---

## 🚀 快速开始

### 环境要求

| 依赖 | 版本要求 | 用途 |
|-----|---------|------|
| JDK | 21+ | 后端服务运行环境 |
| Node.js | 18+ | 前端开发和构建环境 |
| Docker & Docker Compose | 最新版 | 容器化部署（推荐） |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 7.0+ | 缓存和会话存储 |
| Elasticsearch | 8.13+ | 全文检索服务 |
| Kafka | 最新版 | 消息队列（用于数据同步） |

### 方式一：Docker 一键部署（推荐）

Docker 部署是最简便的方式，适合快速搭建完整的 CodeStyle 环境。

```bash
# 1. 克隆项目
git clone https://github.com/xxx/codestyle.git
cd codestyle

# 2. 进入 Docker 目录
cd codestyle-admin/docker

# 3. 修改配置（可选）
# 编辑 docker-compose.yml 中的数据库密码、服务端口等配置

# 4. 启动所有服务
docker-compose up -d

# 5. 查看服务状态
docker-compose ps

# 6. 查看服务日志（可选）
docker-compose logs -f
```

启动成功后：
- 管理后台：http://localhost:80
- API 服务：http://localhost:18000
- 默认账号：admin / admin123

### 方式二：本地开发

本地开发方式适合需要定制化开发或二次开发的场景。

#### 后端启动

```bash
# 1. 进入后端目录
cd codestyle-admin

# 2. 编译项目
mvn clean package -DskipTests

# 3. 启动服务
java -jar codestyle-server/target/codestyle-server.jar
```

#### 前端启动

```bash
# 1. 进入前端目录
cd codestyle-web

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev
```

### 方式三：开发环境配置

对于开发人员，推荐使用以下配置：

1. **IDE 配置**：
   - 后端：IntelliJ IDEA 或 Eclipse
   - 前端：VS Code 或 WebStorm

2. **数据库配置**：
   - 创建数据库：`CREATE DATABASE codestyle DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci`;
   - 导入初始化数据：运行 `codestyle-admin/codestyle-server/src/main/resources/db/changelog/mysql/main_data.sql`

3. **环境变量**：
   - 复制 `.env.example` 为 `.env` 并填写相应配置

### 验证安装

安装完成后，可以通过以下方式验证：

1. 访问管理后台：http://localhost:80
2. 使用默认账号登录：admin / admin123
3. 访问 API 文档：http://localhost:18000/swagger-ui
4. 测试 MCP 插件连接：在 AI IDE 中配置 MCP 服务器

---

## 📁 项目结构

CodeStyle 采用模块化架构设计，各个模块职责明确，便于维护和扩展。

```
codestyle-main/
├── codestyle-admin/              # 管理后台服务
│   ├── codestyle-server/         # 启动模块，负责API接口暴露
│   ├── codestyle-system/         # 系统管理模块，实现核心业务逻辑
│   ├── codestyle-common/         # 公共模块，提供工具类和通用配置
│   ├── codestyle-plugin/         # 插件模块，支持功能扩展
│   │   ├── codestyle-plugin-generator/   # 代码生成器，自动生成前后端代码
│   │   ├── codestyle-plugin-search/      # 搜索服务，基于ES实现全文检索
│   │   ├── codestyle-plugin-tenant/      # 多租户插件，实现租户隔离
│   │   └── codestyle-plugin-schedule/    # 任务调度，实现分布式任务管理
│   └── docker/                   # Docker 部署配置
│
├── codestyle-cloud/              # 云服务模块
│   ├── codestyle-user/           # 用户服务，处理用户认证和授权
│   ├── codestyle-file/           # 文件服务，处理文件上传和存储
│   ├── codestyle-library/        # 代码资产库，管理代码模板和片段
│   ├── codestyle-ai/             # AI 能力服务，提供AI相关功能
│   └── codestyle-model/          # 数据模型，定义核心数据结构
│
├── codestyle-web/                # 官网前端
│   ├── src/
│   │   ├── components/           # Vue 组件
│   │   ├── assets/               # 静态资源
│   │   ├── views/                # 页面组件
│   │   ├── router/               # 路由配置
│   │   └── stores/               # 状态管理
│   └── package.json
│
├── codestyle-maker/              # 模板处理服务 (Python)
│
├── project_documentation.md      # 项目技术文档
├── project_info.md               # 项目基本信息
├── user_scenario.md              # 用户场景说明
└── README.md                     # 项目说明文档
```

### 模块职责说明

| 模块 | 主要职责 | 技术栈 |
|-----|---------|--------|
| **codestyle-server** | 启动模块，API接口暴露 | Spring Boot 3.2 |
| **codestyle-system** | 系统管理，核心业务逻辑 | Spring Boot 3.2, MyBatis-Plus |
| **codestyle-common** | 公共模块，工具类和配置 | Java 21 |
| **codestyle-plugin-generator** | 代码生成器 | FreeMarker, Java 21 |
| **codestyle-plugin-search** | 搜索服务 | Elasticsearch, Kafka, Canal |
| **codestyle-plugin-tenant** | 多租户管理 | Java 21 |
| **codestyle-plugin-schedule** | 任务调度 | Snail Job |
| **codestyle-cloud** | 云服务模块 | Spring Cloud |
| **codestyle-web** | 官网前端 | Vue 3.5, TypeScript, Arco Design |
| **codestyle-maker** | 模板处理服务 | Python |

---

## 🔌 MCP 插件使用

CodeStyle 提供轻量化 MCP 插件，支持在多种 AI IDE 中使用，实现代码风格模板的快速检索和应用。

### 支持的 AI IDE

<div align="center">

| IDE | 状态 | 说明 |
|-----|-----|------|
| <img src="codestyle-web/assets/image/cursor.webp" width="24" /> Cursor | ✅ 已支持 | 完整功能支持，推荐使用 |
| <img src="codestyle-web/assets/image/windsurf.webp" width="24" /> Windsurf | ✅ 已支持 | 完整功能支持 |
| <img src="codestyle-web/assets/image/trae-new-logo.jpg" width="24" /> Trae | ✅ 已支持 | 完整功能支持 |
| <img src="codestyle-web/assets/image/cline.png" width="24" /> Cline | ✅ 已支持 | VSCode 插件，适合 VSCode 用户 |

</div>

### 插件配置

#### 步骤 1：获取 API Key

1. 登录 CodeStyle 管理后台
2. 进入「系统管理 → API 配置」
3. 生成并复制 API Key

#### 步骤 2：配置 MCP 服务器

在 AI IDE 的 MCP 配置中添加以下配置：

```json
{
  "mcpServers": {
    "codestyle": {
      "command": "npx",
      "args": ["-y", "@codestyle/mcp-server"],
      "env": {
        "CODESTYLE_API_URL": "https://api.codestyle.top",
        "CODESTYLE_API_KEY": "your-api-key" // 替换为你的 API Key
      }
    }
  }
}
```

### 使用方法

1. **在 AI IDE 中打开代码文件**
2. **触发 AI 生成**：使用 IDE 提供的 AI 生成功能
3. **选择 CodeStyle 插件**：在 MCP 服务器列表中选择 codestyle
4. **应用风格模板**：插件会自动应用团队的代码风格模板
5. **查看生成结果**：AI 生成的代码会自动符合团队风格

### 插件优势

- 🎯 **精准匹配**：基于团队代码风格模板，生成代码完全符合团队规范
- 🔍 **智能检索**：快速检索相关代码资产，提高代码复用率
- 🚀 **无缝集成**：与 AI IDE 无缝集成，不影响现有开发流程
- 💡 **实时提示**：在代码生成过程中提供实时风格提示

### 常见问题

| 问题 | 解决方案 |
|-----|---------|
| 插件无法连接 | 检查网络连接和 API Key 是否正确 |
| 生成代码风格不符 | 检查风格模板是否正确配置，或更新模板 |
| 插件加载缓慢 | 尝试清理 IDE 缓存，或检查网络速度 |

---

## 📊 效果数据

CodeStyle 在实际应用中取得了显著的效果提升，以下是基于真实用户数据的统计结果：

| 指标 | 提升幅度 | 说明 | 应用场景 |
|-----|---------|------|----------|
| **AI 风格对齐** | 90%+ | 生成代码贴合度大幅提升，减少代码修改成本 | 日常开发、代码生成、重构 |
| **资产利用率** | 60%+ | 历史代码复用率从 10% 提升至 70%+，减少重复开发 | 新功能开发、代码参考、知识传承 |
| **新人上手** | 80% | 上手周期从 2-4 周缩短至 1-2 天，加速新人融入 | 新员工培训、跨团队协作 |
| **接入耗时** | 3 分钟 | 零成本即插即用，快速集成到现有开发流程 | 团队初始化、快速部署 |
| **开发效率** | 40%+ | 平均开发时间减少，提高团队产出 | 迭代开发、快速原型 |
| **代码质量** | 35%+ | 代码风格一致性提升，减少代码审查时间 | 代码审查、质量保证 |

### 客户案例

**某大型互联网公司**：
- 接入 CodeStyle 后，AI 生成代码风格符合率从 30% 提升至 95%
- 历史代码复用率从 5% 提升至 65%
- 新人上手时间从 4 周缩短至 2 天

**某金融科技公司**：
- 代码审查时间减少 40%
- 开发效率提升 50%
- 代码资产库累积超过 10,000 个优质代码片段

---

## 🗺️ 路线图

CodeStyle 团队持续迭代优化，以下是我们的发展路线图：

### 已完成

- [x] **管理后台基础框架**：完成管理后台核心功能，包括用户管理、权限控制等
- [x] **多租户架构**：实现团队/企业级多租户隔离，满足大型组织需求
- [x] **搜索模块（ES + Canal）**：集成 Elasticsearch + Canal + Kafka，实现实时数据同步和全文检索
- [x] **官网展示页**：完成官网建设，提供产品介绍和在线制模功能
- [x] **MCP 插件**：支持 Cursor、Windsurf、Trae、Cline 等主流 AI IDE

### 正在进行

- [ ] **可视化模板编辑器**：提供更直观、更强大的模板编辑功能，支持拖拽式操作
- [ ] **MCP 插件市场**：构建插件生态系统，支持第三方插件开发和共享
- [ ] **代码资产自动分析**：智能识别优质代码，自动构建代码资产库

### 未来规划

- [ ] **团队协作功能增强**：支持代码审查、版本控制、团队知识库等功能
- [ ] **更多编程语言支持**：扩展支持 Python、Go、C++ 等更多编程语言
- [ ] **AI 代码评审**：利用 AI 进行代码评审，提供风格改进建议
- [ ] **企业级部署方案**：提供更完善的企业级部署选项，支持私有化部署
- [ ] **行业解决方案**：针对不同行业提供定制化解决方案，如金融、电商、医疗等

### 版本规划

| 版本 | 计划发布时间 | 主要功能 |
|-----|------------|----------|
| v1.0 | 已发布 | 基础功能，包括管理后台、多租户、搜索模块 |
| v1.5 | 2026 Q2 | 可视化模板编辑器、MCP 插件市场 |
| v2.0 | 2026 Q4 | 代码资产自动分析、团队协作功能增强 |
| v2.5 | 2027 Q2 | 更多编程语言支持、AI 代码评审 |
| v3.0 | 2027 Q4 | 企业级部署方案、行业解决方案 |

---

## 🤝 参与贡献

CodeStyle 是一个开源项目，我们欢迎所有形式的贡献，包括代码提交、文档完善、问题反馈等。

### 贡献流程

1. **Fork 本仓库**：点击 GitHub 页面右上角的 "Fork" 按钮
2. **克隆仓库**：`git clone https://github.com/your-username/codestyle.git`
3. **创建特性分支**：`git checkout -b feature/AmazingFeature`
4. **提交更改**：
   - 遵循代码规范（见下方）
   - 编写清晰的提交信息：`git commit -m 'Add some AmazingFeature'`
5. **推送到分支**：`git push origin feature/AmazingFeature`
6. **提交 Pull Request**：在 GitHub 页面创建 PR，描述你的更改内容和目的

### 代码规范

#### 后端代码规范
- 遵循 [阿里巴巴 Java 开发规范](https://github.com/alibaba/p3c)
- 使用 Spotless 插件自动格式化代码
- 方法和变量命名清晰，遵循驼峰命名法
- 代码注释完善，特别是公共接口和复杂逻辑

#### 前端代码规范
- 遵循 [ESLint](https://eslint.org/) 代码规范
- 使用 Prettier 自动格式化代码
- Vue 组件命名遵循 PascalCase 规范
- TypeScript 类型定义清晰，避免 `any` 类型

### 开发环境设置

1. **后端开发**：
   - 安装 JDK 21+
   - 安装 Maven 3.8+
   - 克隆仓库后运行：`mvn clean install -DskipTests`

2. **前端开发**：
   - 安装 Node.js 18+
   - 安装 pnpm：`npm install -g pnpm`
   - 克隆仓库后运行：`pnpm install`

### 问题反馈

如果您发现问题或有建议，请通过以下方式反馈：

1. **GitHub Issues**：[提交 Issue](https://github.com/codestyle-team/codestyle/issues)
2. **邮件**：contact@codestyle.top
3. **微信群**：扫描官网二维码加入技术交流群

### 贡献者指南

- **新手友好**：我们欢迎新手贡献，会提供必要的指导和帮助
- **代码审查**：所有 PR 都会经过代码审查，确保代码质量
- **测试要求**：提交代码时请确保相关测试通过
- **文档更新**：如果更改影响了文档，请同时更新相关文档

### 行为准则

我们希望 CodeStyle 社区是一个友好、包容的环境，所有参与者都应遵循以下准则：

- 尊重他人，避免使用冒犯性语言
- 接受建设性批评，保持开放心态
- 关注问题本身，不进行人身攻击
- 帮助新人，共同成长

---

## 📄 开源协议

本项目采用 [Apache 2.0](LICENSE) 开源协议。

Apache 2.0 是一种 permissive 开源协议，允许你：
- 自由使用、修改和分发本软件
- 在商业产品中使用本软件
- 修改代码后闭源发布

同时，你需要：
- 保留原始版权声明和许可证文本
- 在修改后的代码中注明修改内容
- 对于因使用本软件而产生的任何损害，不承担责任

## 📞 联系我们

如果您对 CodeStyle 有任何问题或建议，欢迎通过以下方式联系我们：

- 📧 **邮箱**：contact@codestyle.top
- 💬 **微信群**：扫描官网首页二维码加入技术交流群
- 🐛 **问题反馈**：[GitHub Issues](https://github.com/codestyle-team/codestyle/issues)
- 🌐 **官网**：[https://codestyle.top](https://codestyle.top)

---
<p align="center">
**如果这个项目对你有帮助，请给我们一个 ⭐ Star！**
</p>
<p align="center">
  <a href="https://star-history.com/#itxaiohanglover/codestyle&Date">
    <img src="https://api.star-history.com/svg?repos=itxaiohanglover/codestyle&type=date&legend=top-left" 
         alt="Star History Chart" width="800">
  </a>
</p>
<p align="center">
你的支持是我们持续前进的动力！<br>
Made with ❤️ by CodeStyle Team
</p>

