# CodeStyle Wiki 插件 - 文档索引

> 📚 完整的文档导航和快速开始指南
> 
> **版本**: 2.0.0  
> **创建日期**: 2026-01-29

---

## 🎯 快速导航

### 🚀 新用户从这里开始

1. **[README.md](./README.md)** - 项目概述和快速开始
2. **[MIGRATION_REPORT.md](./MIGRATION_REPORT.md)** - 迁移完成报告

### 📖 详细文档

| 文档 | 说明 | 适合人群 |
|------|------|---------|
| **[docs/DESIGN.md](./docs/DESIGN.md)** | 架构设计文档 | 架构师、技术负责人 |
| **[docs/DATABASE.md](./docs/DATABASE.md)** | 数据库设计文档 | 后端开发、DBA |
| **[docs/API.md](./docs/API.md)** | REST API 文档 | 前后端开发 |
| **[docs/IMPLEMENTATION.md](./docs/IMPLEMENTATION.md)** | 实现指南 | 后端开发 |

---

## 📊 文档统计

| 指标 | 数值 |
|------|------|
| 📄 文档总数 | 15 个 |
| 📦 总大小 | ~163 KB |
| 📝 核心文档 | 5 个 |
| 📋 历史文档 | 10 个 |

---

## 🏗️ 核心改进

### ✅ 符合 CodeStyle 规范

- **去除 SPI 机制**：改用 Spring 依赖注入
- **统一命名规范**：Service/Helper 替代 Provider/Facade
- **规范数据库设计**：符合 CodeStyle 表结构规范

### ✅ 使用 Spring AI Alibaba

- **简化 LLM 集成**：使用 ChatClient 统一接口
- **支持国产大模型**：通义千问、文心一言等
- **配置化切换**：无需修改代码

### ✅ 企业级特性

- **权限控制**：基于 Sa-Token
- **多租户支持**：租户级别数据隔离
- **异步处理**：提高系统性能
- **Redis 缓存**：减少 LLM 调用成本

---

## 🛠️ 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.x | 应用框架 |
| **Spring AI Alibaba** | **1.0.0-M3.2** | **AI 集成框架** ⭐ |
| MyBatis-Plus | 3.5.x | ORM 框架 |
| Sa-Token | 1.37.x | 认证授权 |
| Redis | 7.x | 缓存 |
| MySQL | 8.0 | 数据库 |
| JGit | 6.8.x | Git 操作 |

---

## 📂 目录结构

```
codestyle-plugin-wiki/
├── docs/                           # 文档目录
│   ├── DESIGN.md                   # 架构设计
│   ├── DATABASE.md                 # 数据库设计
│   ├── API.md                      # API 文档
│   └── IMPLEMENTATION.md           # 实现指南
│
├── src/                            # 源代码目录
│   ├── main/
│   │   ├── java/
│   │   │   └── top/codestyle/admin/wiki/
│   │   │       ├── controller/     # 控制器
│   │   │       ├── service/        # 服务
│   │   │       ├── helper/         # 辅助类
│   │   │       ├── mapper/         # 数据访问
│   │   │       ├── model/          # 数据模型
│   │   │       ├── config/         # 配置类
│   │   │       └── enums/          # 枚举类
│   │   └── resources/
│   │       ├── mapper/             # MyBatis XML
│   │       └── application-wiki.yml
│   └── test/                       # 测试代码
│
├── pom.xml                         # Maven 配置
├── README.md                       # 项目说明
├── MIGRATION_REPORT.md             # 迁移报告
└── INDEX.md                        # 本文档
```

---

## 🎯 使用场景

### 场景 1：我是项目经理

**推荐阅读**：
1. [README.md](./README.md) - 了解项目概况
2. [MIGRATION_REPORT.md](./MIGRATION_REPORT.md) - 查看迁移完成情况

### 场景 2：我是架构师

**推荐阅读**：
1. [docs/DESIGN.md](./docs/DESIGN.md) - 详细架构设计
2. [docs/DATABASE.md](./docs/DATABASE.md) - 数据库设计
3. [MIGRATION_REPORT.md](./MIGRATION_REPORT.md) - 技术改进点

### 场景 3：我是后端开发

**推荐阅读**：
1. [docs/IMPLEMENTATION.md](./docs/IMPLEMENTATION.md) - 开发实现指南
2. [docs/API.md](./docs/API.md) - API 接口文档
3. [docs/DATABASE.md](./docs/DATABASE.md) - 数据库表结构

### 场景 4：我是前端开发

**推荐阅读**：
1. [docs/API.md](./docs/API.md) - API 接口文档
2. [README.md](./README.md) - 快速开始

---

## 🚀 快速开始

### 1. 配置环境变量

```bash
# .env
DASHSCOPE_API_KEY=your_api_key_here
GITHUB_TOKEN=your_github_token_here
```

### 2. 配置 application-wiki.yml

```yaml
wiki:
  enabled: true

spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
      chat:
        options:
          model: qwen-plus
```

### 3. 初始化数据库

```bash
mysql -u root -p < docs/sql/wiki_tables.sql
```

### 4. 启动应用

```bash
mvn spring-boot:run
```

### 5. 测试 API

```bash
# 创建项目
curl -X POST http://localhost:8000/api/wiki/project \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Project",
    "sourceType": 1,
    "sourceUrl": "https://github.com/test/repo"
  }'
```

---

## 📋 开发计划

### 阶段一：基础实现（1-2周）

- [ ] 创建实体类和 Mapper
- [ ] 实现 Service 层
- [ ] 实现 Helper 层
- [ ] 实现 Controller 层

### 阶段二：核心功能（2-3周）

- [ ] 实现代码分析功能
- [ ] 实现教程生成功能
- [ ] 实现异步任务处理
- [ ] 集成 Spring AI Alibaba

### 阶段三：前端开发（2-3周）

- [ ] 项目管理页面
- [ ] 教程列表页面
- [ ] 教程详情页面
- [ ] 生成任务页面

### 阶段四：测试优化（1-2周）

- [ ] 单元测试
- [ ] 集成测试
- [ ] 性能优化
- [ ] 文档完善

**预计总时间**：6-10 周

---

## 📚 参考资料

### 项目规范

- [CodeStyle 最佳实践](../../CODESTYLE_BEST_PRACTICES.md)
- [CodeStyle 进阶实践](../../CODESTYLE_BEST_PRACTICES_ADVANCED.md)

### 技术文档

- [Spring AI Alibaba](https://github.com/alibaba/spring-ai-alibaba)
- [通义千问 API](https://help.aliyun.com/zh/dashscope/)
- [JGit 文档](https://www.eclipse.org/jgit/)

---

## 🤝 贡献指南

### 开发规范

1. **命名规范**：遵循 CodeStyle 项目规范
2. **代码格式**：使用 Spotless 自动格式化
3. **注释规范**：所有类和方法必须有 Javadoc
4. **测试要求**：单元测试覆盖率 > 70%

### 提交流程

1. Fork 项目
2. 创建特性分支
3. 提交代码（遵循规范）
4. 创建 Pull Request

---

## 📞 联系方式

如有任何问题或建议，请联系：

- 📧 Email: team@codestyle.top
- 💬 Issues: [GitHub Issues](https://github.com/Charles7c/continew-admin/issues)

---

## 🎉 总结

本次迁移规划已完成，主要成果：

✅ **15 个文档**，总计 ~163 KB  
✅ **符合 CodeStyle 规范**  
✅ **使用 Spring AI Alibaba**  
✅ **完整的设计和实现指南**  
✅ **可以直接开始开发**  

**祝您开发顺利！** 🚀

---

**文档维护**: CodeStyle Team  
**创建日期**: 2026-01-29  
**文档版本**: 2.0.0

