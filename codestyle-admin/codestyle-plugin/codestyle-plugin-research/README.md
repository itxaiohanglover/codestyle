# CodeStyle Plugin Research

> 深度研究插件 - 基于 Spring AI Alibaba Graph 的 DeepResearch Agent

---

## 📖 简介

`codestyle-plugin-research` 是 CodeStyle 的深度研究插件，提供基于状态图的智能模板生成能力。

### 核心功能

- ✅ **简单生成**：基于代码片段一次性生成模板
- ✅ **深度研究**：基于 GitHub 仓库深度分析生成模板
- ✅ **状态图驱动**：可视化、可中断、可恢复的研究流程
- ✅ **Human-in-the-Loop**：关键节点支持用户确认和反馈
- ✅ **SSE 流式返回**：实时推送研究进度

---

## 🏗️ 架构设计

### 双模式架构

| 模式 | 适用场景 | 技术方案 |
|------|---------|---------|
| **简单生成** | 快速生成单个模板 | 直接调用 AI 模型 |
| **深度研究** | 复杂仓库分析 | Spring AI Graph 状态图 |

### 状态图节点

```
SourceNode → CoordinatorNode → PlannerNode [用户确认]
    ↓
Execute Layer (并行)
├─ ResearcherNode (网络搜索)
├─ CoderNode (代码执行)
└─ AnalyzerNode (深度分析)
    ↓
ExtractNode → ReflectNode [用户确认] → GeneratorNode → IndexNode
```

---

## 🚀 快速开始

### 1. 添加依赖

在父模块 `codestyle-plugin/pom.xml` 中添加：

```xml
<module>codestyle-plugin-research</module>
```

在需要使用的模块中添加依赖：

```xml
<dependency>
    <groupId>top.codestyle.admin</groupId>
    <artifactId>codestyle-plugin-research</artifactId>
</dependency>
```

### 2. 配置

在 `application.yml` 中添加配置：

```yaml
research:
  enabled: true
  model:
    default-model: qwen-plus
    strong-model: qwen-max
  github:
    token: ${GITHUB_TOKEN}
  template:
    storage-path: ./templates
```

### 3. 使用 API

#### 简单生成模板

```bash
POST /api/templates/generate
Content-Type: application/json

{
  "codeSnippet": "public class UserController { ... }",
  "templateName": "CRUD Controller",
  "templateDescription": "标准的 CRUD 控制器模板",
  "language": "java",
  "framework": "spring-boot"
}
```

#### 启动深度研究

```bash
POST /api/research/start
Content-Type: application/json

{
  "sourceType": "GITHUB",
  "sourceContent": "https://github.com/username/repo",
  "templateName": "项目模板",
  "autoConfirm": false
}
```

#### 订阅研究进度

```bash
GET /api/research/{taskId}
Accept: text/event-stream
```

---

## 📦 模块结构

```
codestyle-plugin-research/
├── src/main/java/top/codestyle/admin/research/
│   ├── config/              # 配置类
│   │   └── ResearchProperties.java
│   ├── controller/          # 控制器
│   │   ├── TemplateController.java
│   │   └── ResearchController.java
│   ├── service/             # 服务接口
│   │   ├── TemplateService.java
│   │   ├── ResearchService.java
│   │   └── impl/            # 服务实现
│   ├── model/               # 数据模型
│   │   ├── req/             # 请求模型
│   │   ├── resp/            # 响应模型
│   │   └── enums/           # 枚举类
│   ├── node/                # 状态图节点（待实现）
│   │   ├── SourceNode.java
│   │   ├── PlannerNode.java
│   │   ├── ExtractNode.java
│   │   └── GeneratorNode.java
│   └── graph/               # 状态图定义（待实现）
│       └── ResearchGraph.java
└── src/main/resources/
    └── application-research.yml
```

---

## 🔧 开发计划

### Phase 1: 基础框架 ✅

- [x] 创建模块结构
- [x] 定义配置类
- [x] 定义数据模型
- [x] 创建 Controller 和 Service

### Phase 2: 简单生成 ⏳

- [ ] 实现 AI 模型调用
- [ ] 实现代码分析逻辑
- [ ] 实现模板生成逻辑
- [ ] 实现文件存储

### Phase 3: 深度研究 ⏳

- [ ] 实现状态图节点
- [ ] 实现状态图编排
- [ ] 实现 SSE 流式推送
- [ ] 实现用户交互

### Phase 4: 完善优化 ⏳

- [ ] 错误处理和重试
- [ ] 性能优化
- [ ] 单元测试
- [ ] 集成测试

---

## 📚 参考文档

- [架构设计文档](../../../docs/plans/2026-02-22-codestyle-architecture-design.md)
- [CodeStyle 最佳实践](../../../archive/v1.0.0/best-practices/CodeStyle最佳实践.md)
- [Spring AI Alibaba 文档](https://github.com/alibaba/spring-ai-alibaba)

---

## 📄 许可证

Apache License 2.0

---

**创建时间**: 2026-02-22  
**维护者**: CodeStyle Team
