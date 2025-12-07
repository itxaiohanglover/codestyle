# Elasticsearch测试工具使用说明

## 概述

本文档介绍了项目中提供的Elasticsearch测试工具，用于创建索引、插入测试数据以及清理调试数据。这些工具支持通过参数控制生成的数据量，便于进行各种测试场景。
新增功能包括全字段Mock数据生成、并行插入、私有仓库过滤和脚本排序优化。

## 测试类说明

### 1. ElasticsearchIndexTest

核心工具类，提供所有Elasticsearch操作的基础方法：

* **创建索引**: `createIndex()`，支持从 `template-settings.json` 加载索引设置
* **插入测试数据**: `insertTestData(int count)`，生成 `CodeStyleTemplateDO` 全字段Mock数据
* **并行插入大量数据**: `insertTestDataParallel(int count, int threadCount)`
* **删除所有数据**: `deleteAllTestData()`
* **删除整个索引**: `deleteIndex()`

**直接使用方法**：

```java
// 创建索引并插入100条数据
ElasticsearchIndexTest.createIndex();
ElasticsearchIndexTest.insertTestData(100);

// 清理数据
ElasticsearchIndexTest.deleteAllTestData();
// 或者删除索引
ElasticsearchIndexTest.deleteIndex();
```

### 2. Repository层查询注意事项

* 支持通过 `BoolQuery` 的 `filter` 条件过滤私有仓库 (`isPrivate=false`)
* `filter` 是硬性条件，不满足则文档不会返回
* Script排序已指定 `ScriptSortType.Number`，避免 `[type]` 异常
* 分词器已配置，但请确保插入数据字段与索引Mapping一致，避免无结果

### 3. MockDataGenerator

* 支持全字段随机数据生成，包括：`groupId`、`artifactId`、`文件路径`、`文件名`、`版本号`、`description` 等
* 单条数据生成示例：

```java
CodeStyleTemplateDO template = mockDataGenerator.generateSingleTemplate();
```

* 批量数据生成示例：

```java
List<CodeStyleTemplateDO> templates = mockDataGenerator.generateBatchTemplates(100);
```

## 清理调试数据的方法

### 方法1：调用工具类方法

```java
// 删除所有数据但保留索引结构
ElasticsearchIndexTest.deleteAllTestData();

// 删除整个索引
ElasticsearchIndexTest.deleteIndex();
```

### 方法2：运行测试类方法

```bash
mvn test -Dtest=ElasticsearchIndexTest#testCreateInsertAndDeleteData
```

### 方法3：命令行参数（数据生成/清理）

```bash
# 生成1000条全字段测试数据
java -cp target/test-classes:target/classes com.es.template.ElasticsearchIndexTest 1000

# 清理数据但保留索引
java -cp target/test-classes:target/classes com.es.template.ElasticsearchIndexTest 0

# 删除整个索引
java -cp target/test-classes:target/classes com.es.template.ElasticsearchIndexTest 0 deleteIndex
```

## 注意事项 本地测试的话

1. 确保Elasticsearch服务在9200端口运行
2. 并行插入时注意线程数量，避免对ES服务造成压力
3. `filter`条件严格匹配，务必确保测试数据字段与Mapping一致
4. 测试完成后及时清理数据，避免占用过多存储空间
