# Elasticsearch测试工具使用说明

## 概述

本文档介绍了项目中提供的Elasticsearch测试工具，用于创建索引、插入测试数据以及清理调试数据。这些工具支持通过参数控制生成的数据量，便于进行各种测试场景。

## 测试类说明

### 1. ElasticsearchTestUtils

核心工具类，提供所有Elasticsearch操作的基础方法：

- **创建索引**: `createIndex()`
- **插入测试数据**: `insertTestData(int count)`
- **并行插入大量数据**: `insertTestDataParallel(int count, int threadCount)`
- **删除所有数据**: `deleteAllTestData()`
- **删除整个索引**: `deleteIndex()`

**直接使用方法**：
```java
// 创建索引并插入100条数据
ElasticsearchTestUtils.createIndex();
ElasticsearchTestUtils.insertTestData(100);

// 清理数据
ElasticsearchTestUtils.deleteAllTestData();
// 或者删除索引
ElasticsearchTestUtils.deleteIndex();
```

**主方法使用**：
```bash
java -cp target/test-classes:target/classes com.es.template.ElasticsearchTestUtils 500
```

### 2. ElasticsearchIntegrationTest

集成测试类，使用Spring Boot Test框架：

- 自动创建索引和插入测试数据
- 测试索引存在性、数据量、搜索功能
- 自动清理测试环境

**使用方法**：通过IDE运行JUnit测试，或使用命令行：
```bash
mvn test -Dtest=ElasticsearchIntegrationTest
```

### 3. ElasticsearchDataGeneratorTest

命令行数据生成工具，支持灵活的参数配置：

**参数说明**：
- 第一个参数：数据条数（必填）
- 第二个参数：是否清理数据（可选，true/false）
- 第三个参数：是否删除索引（可选，true/false）

**使用示例**：

1. 生成1000条测试数据：
```bash
java -cp target/test-classes:target/classes com.es.template.ElasticsearchDataGeneratorTest 1000
```

2. 生成500条数据后自动清理：
```bash
java -cp target/test-classes:target/classes com.es.template.ElasticsearchDataGeneratorTest 500 true
```

3. 仅清理数据和索引：
```bash
java -cp target/test-classes:target/classes com.es.template.ElasticsearchDataGeneratorTest 0 false true
```

## 测试数据生成说明

- 数据基于`MockDataGenerator`生成，包含随机的groupId、artifactId、文件路径等
- 小数据量（<1000条）使用单线程插入，大数据量使用并行插入提高效率
- 并行插入使用系统可用处理器数量的线程池

## 清理调试数据的方法

### 方法1：调用工具类方法

```java
// 删除所有数据但保留索引结构
ElasticsearchTestUtils.deleteAllTestData();

// 删除整个索引
ElasticsearchTestUtils.deleteIndex();
```

### 方法2：运行清理测试

```bash
mvn test -Dtest=ElasticsearchDataGeneratorTest#cleanTestData
```

### 方法3：使用命令行参数

```bash
# 清理数据但保留索引
java -cp target/test-classes:target/classes com.es.template.ElasticsearchDataGeneratorTest 0 true

# 删除整个索引
java -cp target/test-classes:target/classes com.es.template.ElasticsearchDataGeneratorTest 0 false true
```

## 注意事项

1. 确保Elasticsearch服务在本地9200端口运行
2. 大数据量测试可能需要调整JVM内存设置
3. 并行插入时注意控制线程数量，避免对Elasticsearch服务造成过大压力
4. 测试完成后记得清理测试数据，避免占用过多存储空间
