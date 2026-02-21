# .claude 目录

AI 辅助开发的技能库和工具模板。

## 📁 目录结构

```
.claude/
├── README.md                   # 本文件
├── OPTIMIZATION_REPORT.md      # 优化报告
├── api-test/                   # API 测试技能
│   └── SKILL.md
├── code-review/                # 代码审查技能
│   └── SKILL.md
└── doc-write/                  # 文档生成技能
    └── SKILL.md
```

## 🎯 技能列表

### 1. API 测试 (`api-test/`)

快速生成 CodeStyle 项目的 API 测试脚本。

**使用方法**：
```
@api-test 生成测试脚本：
- 接口：POST /api/user/create
- 需要登录：是
- 参数：username, email, password
```

**支持的测试类型**：
- ✅ 内部 API 测试（RSA 加密 + Bearer Token）
- ✅ OpenAPI 测试（MD5 签名 + 防重放）
- ✅ 简单测试（无需登录）
- ✅ PowerShell 快速测试

### 2. 代码审查 (`code-review/`)

基于 ContiNew Admin 最佳实践的代码审查工具。

**使用方法**：
```
@code-review 审查以下代码：
[粘贴代码]
```

**审查维度**：
- ✅ 代码规范（P3C、命名、注释）
- ✅ 架构设计（分层、单一职责）
- ✅ 安全性（SQL 注入、XSS、权限）
- ✅ 性能优化（N+1、缓存、分页）
- ✅ 业务逻辑（事务、异常、校验）

### 3. 文档生成 (`doc-write/`)

基于 CodeStyle 项目规范的文档生成工具。

**使用方法**：
```
@doc-write 生成 API 文档：
- 模块：用户管理
- 接口：POST /api/user/create
- 功能：创建新用户
```

**支持的文档类型**：
- ✅ API 文档（Swagger 注解）
- ✅ 项目文档（README、CHANGELOG）
- ✅ 技术文档（架构、数据库、部署）
- ✅ 开发文档（指南、最佳实践）

## 📝 添加新技能

如果需要添加新的 AI 辅助技能，请按照以下结构创建：

```
.claude/
└── <skill-name>/
    └── SKILL.md        # 技能文档
```

**SKILL.md 格式**：
- 技能说明
- 使用方法
- 模板代码
- 快速参考
- 示例

## 🔗 相关文档

- [CodeStyle 最佳实践](../archive/v1.0.0/best-practices/CodeStyle最佳实践.md)
- [API 测试指南](../archive/v1.0.0/api-testing/API测试指南.md)
- [OpenAPI 认证机制](../archive/v1.0.0/api-testing/OpenAPI认证机制.md)

## 💡 使用技巧

1. **引用技能**：使用 `@<folder-name>` 引用技能目录
2. **快速生成**：描述需求，AI 自动生成代码
3. **模板复用**：所有模板都可以直接复制使用
4. **组合使用**：可以同时引用多个技能

**示例**：
```
@code-review 审查代码后，@doc-write 生成 API 文档
```

## 🎓 最佳实践参考

所有技能都基于以下规范：
- ✅ ContiNew Admin 框架规范
- ✅ 阿里巴巴 P3C 编码规范
- ✅ Spring Boot 最佳实践
- ✅ Vue 3 + TypeScript 规范

## 🔄 版本历史

- **v1.0.0** (2026-02-21): 初始版本，包含 3 个核心技能
