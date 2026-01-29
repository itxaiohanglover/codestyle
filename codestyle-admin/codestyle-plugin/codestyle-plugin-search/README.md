# CodeStyle 检索模块

> 基于 AssistantAgent 架构的企业级多源混合检索模块

## ✨ 特性

- 🔍 **多源检索**：支持 Elasticsearch、Milvus 等多种数据源
- 🔄 **混合检索**：使用 RRF 算法融合多个数据源的检索结果
- 📊 **智能重排**：支持 BGE-Rerank 模型重排序
- 🚀 **高性能**：并行查询、结果缓存
- 🔌 **易扩展**：基于 SPI 机制，轻松扩展新的数据源

## 📦 已完成的功能

### ✅ 阶段一：基础架构（已完成）

- [x] 创建模块目录结构
- [x] 配置 pom.xml 依赖
- [x] 定义核心数据模型
  - `SearchRequest` - 检索请求
  - `SearchResult` - 检索结果
  - `SearchSourceType` - 数据源类型枚举
- [x] 定义 SPI 接口
  - `SearchProvider` - 检索提供者接口
  - `SearchFacade` - 检索门面接口
  - `RerankProvider` - 重排提供者接口
- [x] 实现配置类
  - `SearchProperties` - 模块配置
  - `ElasticsearchConfig` - ES 配置
- [x] 实现 Service 层
  - `SearchService` - 检索服务接口
  - `SearchServiceImpl` - 检索服务实现（含 RRF 算法）
- [x] 实现 Controller 层
  - `SearchController` - REST API
- [x] 实现 Elasticsearch Provider
  - `ElasticsearchSearchProvider` - ES 检索实现

## 🏗️ 项目结构

```
codestyle-plugin-search/
├── src/main/java/top/codestyle/admin/search/
│   ├── spi/                          # SPI 接口
│   │   ├── SearchProvider.java       # 检索提供者接口
│   │   ├── SearchFacade.java         # 检索门面接口
│   │   └── RerankProvider.java       # 重排提供者接口
│   ├── provider/                     # 检索提供者实现
│   │   └── ElasticsearchSearchProvider.java
│   ├── model/                        # 数据模型
│   │   ├── SearchRequest.java
│   │   ├── SearchResult.java
│   │   └── SearchSourceType.java
│   ├── service/                      # 业务服务
│   │   ├── SearchService.java
│   │   └── impl/SearchServiceImpl.java
│   ├── controller/                   # REST API
│   │   └── SearchController.java
│   └── config/                       # 配置类
│       ├── SearchProperties.java
│       └── ElasticsearchConfig.java
├── src/main/resources/
│   └── application-search.yml        # 模块配置
├── pom.xml
├── DESIGN.md                         # 设计文档
├── IMPLEMENTATION.md                 # 实现规划
└── README.md                         # 本文档
```

## 🚀 快速开始

### 1. 添加模块依赖

在父 pom.xml 中添加：

```xml
<module>codestyle-plugin-search</module>
```

### 2. 配置 Elasticsearch

在 `application.yml` 中添加：

```yaml
search:
  enabled: true
  elasticsearch:
    enabled: true
    hosts: localhost:9200
    index: codestyle_templates
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

### 4. 测试 API

#### 单源检索

```bash
curl -X POST http://localhost:18000/api/search/single \
  -H "Content-Type: application/json" \
  -d '{
    "query": "如何配置 MySQL 连接池",
    "sourceType": "ELASTICSEARCH",
    "topK": 10
  }'
```

#### 混合检索

```bash
curl -X POST http://localhost:18000/api/search/hybrid \
  -H "Content-Type: application/json" \
  -d '{
    "query": "如何配置 MySQL 连接池",
    "topK": 10
  }'
```

#### 快速检索（GET）

```bash
curl "http://localhost:18000/api/search/quick?query=MySQL&topK=5"
```

## 📝 API 文档

访问 Swagger UI：http://localhost:18000/swagger-ui.html

## 🔧 配置说明

### Elasticsearch 配置

```yaml
search:
  elasticsearch:
    enabled: true           # 是否启用
    hosts: localhost:9200   # ES 地址
    username:               # 用户名（可选）
    password:               # 密码（可选）
    index: codestyle_templates  # 索引名称
```

### 混合检索配置

```yaml
search:
  hybrid:
    enabled: true
    fusion-strategy: RRF    # 融合策略：RRF
```

### 重排配置

```yaml
search:
  rerank:
    enabled: false          # 是否启用重排
    provider: BGE
    api-url: http://localhost:8001/rerank
    model: BAAI/bge-reranker-v2-m3
    top-k: 10
```

## 🎯 下一步计划

### 阶段二：Milvus 集成（待实现）

- [ ] 实现 `MilvusSearchProvider`
- [ ] 配置 Milvus 客户端
- [ ] 实现向量检索
- [ ] 集成 Embedding 服务

### 阶段三：BGE-Rerank 集成（待实现）

- [ ] 实现 `BgeRerankProvider`
- [ ] 集成 BGE-Rerank API
- [ ] 实现重排序逻辑

### 阶段四：性能优化（待实现）

- [ ] 实现 Redis 缓存
- [ ] 优化并行查询
- [ ] 添加超时控制

## 📖 参考文档

- [设计文档](DESIGN.md)
- [实现规划](IMPLEMENTATION.md)
- [AssistantAgent 架构分析](../../../ASSISTANT_AGENT_ARCHITECTURE_ANALYSIS.md)
- [CodeStyle 最佳实践](../../../CODESTYLE_BEST_PRACTICES.md)

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

Apache License 2.0

---

**开发团队**: CodeStyle Team  
**最后更新**: 2026-01-29

