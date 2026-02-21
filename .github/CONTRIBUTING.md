# 贡献指南

感谢您对 CodeStyle 项目的关注！我们欢迎任何形式的贡献。

## 🤝 如何贡献

### 报告问题

如果您发现了 Bug 或有功能建议，请：

1. 先搜索 [Issues](https://github.com/codestyle-team/codestyle/issues) 确认问题未被报告
2. 使用合适的 Issue 模板创建新 Issue
3. 提供详细的问题描述和复现步骤

### 提交代码

1. **Fork 项目**
   ```bash
   # Fork 项目到您的账号
   # 克隆到本地
   git clone https://github.com/YOUR_USERNAME/codestyle.git
   cd codestyle
   ```

2. **创建分支**
   ```bash
   # 从 main 分支创建新分支
   git checkout -b feature/your-feature-name
   # 或
   git checkout -b fix/your-bug-fix
   ```

3. **开发和测试**
   ```bash
   # 后端开发
   cd codestyle-admin
   mvn clean test
   
   # 前端开发
   cd codestyle-admin-web
   pnpm install
   pnpm dev
   pnpm lint
   ```

4. **提交代码**
   ```bash
   git add .
   git commit -m "feat: 添加新功能"
   # 或
   git commit -m "fix: 修复某个问题"
   ```

5. **推送并创建 PR**
   ```bash
   git push origin feature/your-feature-name
   # 在 GitHub 上创建 Pull Request
   ```

## 📝 代码规范

### 后端（Java）

- 遵循阿里巴巴 Java 开发手册
- 使用 Spotless 进行代码格式化
- 类和方法必须有 Javadoc 注释
- 单元测试覆盖率不低于 70%

### 前端（Vue/TypeScript）

- 遵循 ESLint 配置规则
- 使用 TypeScript 类型注解
- 组件必须有注释说明
- 遵循 Vue 3 Composition API 最佳实践

### Git 提交规范

使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type 类型：**
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建/工具相关

**示例：**
```
feat(search): 添加向量检索功能

- 集成 Milvus 向量数据库
- 实现混合检索算法
- 添加相关单元测试

Closes #123
```

## 🔍 代码审查

所有 PR 都需要经过代码审查：

- 至少一位维护者的批准
- 通过所有 CI 检查
- 解决所有审查意见
- 保持提交历史清晰

## 📚 文档

如果您的改动涉及：

- 新功能：更新 README.md 和相关文档
- API 变更：更新 API 文档
- 配置变更：更新配置说明

## 🎯 开发环境

### 环境要求

- **Java**: JDK 17+
- **Node.js**: 18+
- **Maven**: 3.8+
- **pnpm**: 8+
- **MySQL**: 8.0+
- **Redis**: 5.0+
- **Elasticsearch**: 8.x

### 本地开发

1. **启动后端**
   ```bash
   cd codestyle-admin
   mvn spring-boot:run
   ```

2. **启动前端**
   ```bash
   cd codestyle-admin-web
   pnpm dev
   ```

3. **访问应用**
   - 前端：http://localhost:5173
   - 后端：http://localhost:8080

## ❓ 获取帮助

- 📖 查看 [文档](./README.md)
- 💬 加入 [讨论区](https://github.com/codestyle-team/codestyle/discussions)
- 🐛 提交 [Issue](https://github.com/codestyle-team/codestyle/issues)

## 📄 许可证

通过贡献代码，您同意您的贡献将在 [Apache 2.0 许可证](./LICENSE) 下发布。

---

再次感谢您的贡献！🎉

