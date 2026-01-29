# 搜索模块技术文档

## 📋 目录

1. [模块概述](#模块概述)
2. [架构设计](#架构设计)
3. [数据同步流程](#数据同步流程)
4. [Docker Compose 配置说明](#docker-compose-配置说明)
5. [配置文件详解](#配置文件详解)
6. [部署指南](#部署指南)
7. [运维监控](#运维监控)
8. [常见问题](#常见问题)

---

## 模块概述

### 功能定位

搜索模块（`codestyle-plugin-search`）是一个基于 **Canal + Kafka + Elasticsearch** 的实时数据同步与搜索解决方案，主要功能包括：

- **实时数据同步**：通过 Canal 捕获 MySQL binlog 变更，实时同步到 Elasticsearch
- **全量数据同步**：应用启动时自动执行全量数据同步，确保数据一致性
- **增量数据同步**：通过 Kafka 消息队列实现增量数据的实时同步
- **数据搜索**：提供基于 Elasticsearch 的全文搜索能力
- **幂等性保证**：使用 Redis 实现消息幂等性，避免重复处理

### 技术栈

| 组件 | 版本 | 用途 |
|------|------|------|
| **Canal Server** | 1.1.7 | MySQL binlog 解析与消息推送 |
| **Kafka** | 2.13-2.7.0 | 消息队列中间件 |
| **ZooKeeper** | 3.9 | Kafka 和 Canal 的协调服务 |
| **Elasticsearch** | 8.13.0 | 搜索引擎 |
| **Spring Kafka** | - | Kafka 客户端集成 |
| **Redis** | - | 消息幂等性控制 |

---

## 架构设计

### 整体架构图

```
┌─────────────┐
│   MySQL     │
│  (Binlog)   │
└──────┬──────┘
       │
       │ Binlog 变更
       ▼
┌─────────────────────────────────┐
│      Canal Server (Docker)      │
│  - 解析 MySQL binlog            │
│  - 转换为 JSON 消息                │
│  - 推送到 Kafka                  │
└──────────────┬──────────────────┘
               │
               │ Kafka 消息
               ▼
┌─────────────────────────────────┐
│         Kafka (Docker)          │
│  - Topic: data-change           │
│  - 消息持久化                    │
└──────────────┬──────────────────┘
               │
               │ 消费消息
               ▼
┌─────────────────────────────────┐
│   Spring Boot Application       │
│  - CanalKafkaMessageConsumer    │
│  - 消息解析与转换                │
│  - 幂等性检查 (Redis)            │
│  - ES 同步服务                   │
└──────────────┬──────────────────┘
               │
               │ 同步数据
               ▼
┌─────────────────────────────────┐
│      Elasticsearch              │
│  - 索引: codestyle_remote_meta_* │
│  - 全文搜索                      │
└─────────────────────────────────┘
```

### 核心组件

#### 1. **Canal Server (Docker)**
- **作用**：监听 MySQL binlog，捕获数据变更
- **模式**：Kafka 模式（`canal.serverMode = kafka`）
- **输出**：将变更事件转换为 JSON 格式，发送到 Kafka

#### 2. **Kafka**
- **作用**：消息队列中间件，解耦 Canal 和应用
- **Topic**：`data-change`（3 个分区，1 个副本）
- **优势**：高吞吐、持久化、支持批量消费

#### 3. **Spring Boot 应用**
- **CanalKafkaMessageConsumer**：Kafka 消息消费者
- **CanalMessageConverter**：Canal 消息格式转换器
- **EsBulkSyncService**：Elasticsearch 批量同步服务
- **MessageIdempotencyService**：消息幂等性服务（基于 Redis）

#### 4. **Elasticsearch**
- **索引命名**：`codestyle_remote_meta_info`
- **文档 ID**：使用 MySQL 主键

---

## 数据同步流程

### 全量同步流程

```
应用启动
    │
    ▼
FullSyncRunner (ApplicationRunner)
    │
    ▼
SyncService.fullSync()
    │
    ├─→ 从 MySQL 查询所有数据
    │
    ├─→ 删除 ES 中所有旧数据
    │
    └─→ 批量写入 ES
```

**触发时机**：
- 应用启动时自动执行
- 通过 `FullSyncRunner` 实现

### 增量同步流程

```
MySQL 数据变更 (INSERT/UPDATE/DELETE)
    │
    ▼
MySQL Binlog 记录变更
    │
    ▼
Canal Server 解析 Binlog
    │
    ├─→ 转换为 Canal 消息格式
    │
    └─→ 发送到 Kafka Topic: data-change
    │
    ▼
Kafka 持久化消息
    │
    ▼
CanalKafkaMessageConsumer 批量消费
    │
    ├─→ 解析 Canal JSON 消息
    │
    ├─→ 转换为 DataChangeMessage
    │
    ├─→ 幂等性检查 (Redis)
    │   └─→ 如果已处理，跳过
    │
    ├─→ EsBulkSyncService 同步到 ES
    │   ├─→ INSERT → 创建文档
    │   ├─→ UPDATE → 更新文档
    │   └─→ DELETE → 删除文档
    │
    └─→ 提交 Kafka Offset
```

### 消息格式

#### Canal 原始消息（Kafka 中的 JSON）

```json
{
  "type": "UPDATE",
  "database": "codestyle",
  "table": "remote_meta_info",
  "data": [
    {
      "id": "11",
      "meta_json": "{...}",
      "group_id": "backend",
      "create_time": "2025-12-16 18:11:11"
    }
  ],
  "old": [
    {
      "deleted": "0"
    }
  ],
  "ts": 1766891232073,
  "isDdl": false,
  "pkNames": ["id"]
}
```

#### 转换后的 DataChangeMessage

```java
{
  "messageId": "codestyle.remote_meta_info.11.1766891232073",
  "operation": "UPDATE",
  "database": "codestyle",
  "table": "remote_meta_info",
  "primaryKey": "11",
  "afterData": {...},
  "beforeData": {...}
}
```

---

## Docker Compose 配置说明

### 文件位置

```
codestyle-plugin-search/
└── docker/
    └── canal-server/
        └── docker-compose.yml  ← 主配置文件
```

### 服务架构

```yaml
ZooKeeper (基础服务)
    │
    ▼
Kafka (消息队列)
    │
    ▼
Canal Server (数据同步)
```

### 服务启动顺序

通过 `depends_on` 和 `condition: service_healthy` 确保：

1. **ZooKeeper** 先启动并健康
2. **Kafka** 等待 ZooKeeper 健康后启动
3. **Canal Server** 等待 Kafka 健康后启动

### 详细配置说明

#### 1. ZooKeeper 服务

```yaml
zookeeper:
  image: zookeeper:3.9
  container_name: canal-zookeeper
  ports:
    - "2181:2181"  # 客户端连接端口
  environment:
    - ZOO_MY_ID=1
    - ZOO_SERVERS=server.1=0.0.0.0:2888:3888;2181
  healthcheck:
    test: ["CMD-SHELL", "nc -z localhost 2181 || exit 1"]
    interval: 10s
    timeout: 5s
    retries: 5
    start_period: 15s  # 启动宽限期
```

**作用**：
- 为 Kafka 提供协调服务（broker 注册、leader 选举）
- 为 Canal Server 提供元数据存储

**健康检查**：
- 检查 2181 端口是否可访问
- 15 秒启动宽限期，每 10 秒检查一次

#### 2. Kafka 服务

```yaml
canal-kafka:
  image: wurstmeister/kafka:2.13-2.7.0
  container_name: canal-kafka
  ports:
    - "9092:9092"  # Docker 内部通信
    - "9094:9094"  # 宿主机访问（EXTERNAL）
  environment:
    - KAFKA_BROKER_ID=1
    - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
    - KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092,EXTERNAL://0.0.0.0:9094
    - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://canal-kafka:9092,EXTERNAL://localhost:9094
    - KAFKA_CREATE_TOPICS=data-change:3:1  # 自动创建 topic
  depends_on:
    zookeeper:
      condition: service_healthy
  healthcheck:
    test: ["CMD-SHELL", "kafka-topics.sh --bootstrap-server localhost:9092 --list >/dev/null 2>&1 || nc -z localhost 9092 || exit 1"]
    interval: 10s
    timeout: 10s
    retries: 10
    start_period: 40s  # Kafka 启动较慢，需要更长的宽限期
```

**关键配置说明**：

| 配置项 | 说明 |
|--------|------|
| `KAFKA_LISTENERS` | 监听地址：9092（内部）、9094（外部） |
| `KAFKA_ADVERTISED_LISTENERS` | 客户端连接地址：Docker 内用 `canal-kafka:9092`，宿主机用 `localhost:9094` |
| `KAFKA_CREATE_TOPICS` | 自动创建 `data-change` topic（3 分区，1 副本） |
| `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1` | Offset topic 副本数（单节点环境） |

**健康检查**：
- 使用 `kafka-topics.sh` 验证 Kafka 完全就绪（包括 ZooKeeper 连接）
- 如果命令失败，回退到端口检查
- 40 秒启动宽限期（Kafka 启动较慢）

#### 3. Canal Server 服务

```yaml
canal-server:
  image: canal/canal-server:v1.1.7
  container_name: canal-sync-mysql-server
  ports:
    - "11111:11111"  # Canal 管理端口
  extra_hosts:
    - "host.docker.internal:host-gateway"  # 访问宿主机 MySQL
  volumes:
    - ./conf/canal.properties:/home/admin/canal-server/conf/canal.properties:ro
    - ./conf/example/instance.properties:/home/admin/canal-server/conf/example/instance.properties:ro
    - ./logs:/home/admin/canal-server/logs
  depends_on:
    canal-kafka:
      condition: service_healthy
    zookeeper:
      condition: service_healthy
```

**关键配置说明**：

| 配置项 | 说明 |
|--------|------|
| `host.docker.internal` | Docker Desktop 特殊主机名，用于访问宿主机服务 |
| `canal.properties` | Canal Server 主配置文件 |
| `instance.properties` | Canal 实例配置（MySQL 连接、Kafka topic） |
| `logs` | 日志目录（持久化） |

**版本选择**：
- 使用 `v1.1.7`（`v1.1.5` 在 Windows Docker 上有段错误问题）

### 网络配置

```yaml
networks:
  canal-kafka-network:
    driver: bridge
```

所有服务共享同一个网络，可以通过服务名互相访问：
- `zookeeper:2181`
- `canal-kafka:9092`

### 数据持久化

```yaml
volumes:
  kafka-data:        # Kafka 数据目录
  zookeeper-data:    # ZooKeeper 数据目录
  zookeeper-log:     # ZooKeeper 日志目录
```

**作用**：
- 容器删除后数据不丢失
- Kafka 消息持久化
- ZooKeeper 元数据持久化

---

## 配置文件详解

### 1. docker-compose.yml

**位置**：`docker/canal-server/docker-compose.yml`

**作用**：
- 定义服务编排（ZooKeeper、Kafka、Canal Server）
- 配置服务依赖和启动顺序
- 配置网络和存储卷
- 配置健康检查

**关键特性**：
- ✅ 健康检查机制确保服务按顺序启动
- ✅ 自动创建 Kafka topic
- ✅ 数据持久化（volumes）
- ✅ 网络隔离（bridge 网络）

### 2. canal.properties

**位置**：`docker/canal-server/conf/canal.properties`

**作用**：Canal Server 主配置文件

**关键配置**：

```properties
# ZooKeeper 连接（使用服务名）
canal.zkServers = zookeeper:2181

# 运行模式：Kafka
canal.serverMode = kafka

# Kafka 服务器地址（Docker 内部）
kafka.bootstrap.servers = canal-kafka:9092

# 实例配置
canal.destinations = example
canal.auto.scan = true
```

**说明**：
- `canal.zkServers`：使用 Docker 服务名 `zookeeper`，确保容器重启后仍能连接
- `kafka.bootstrap.servers`：使用 Docker 服务名 `canal-kafka`，内部通信使用 9092 端口

### 3. instance.properties

**位置**：`docker/canal-server/conf/example/instance.properties`

**作用**：Canal 实例配置（MySQL 连接、订阅规则、Kafka topic）

**关键配置**：

```properties
# MySQL 连接（通过 host.docker.internal 访问宿主机）
canal.instance.master.address = host.docker.internal:3306
canal.instance.dbUsername = root
canal.instance.dbPassword = root

# 订阅规则：codestyle 数据库下的所有表
canal.instance.filter.regex = codestyle\\..*

# Kafka Topic
canal.mq.topic = data-change

# 禁用 TSDB（避免连接问题）
canal.instance.tsdb.enable = false
```

**说明**：
- `host.docker.internal`：Docker Desktop 提供的特殊主机名，用于访问宿主机服务
- `canal.instance.filter.regex`：正则表达式，订阅 `codestyle` 数据库下的所有表
- `canal.instance.tsdb.enable = false`：禁用 TSDB，避免 MySQL 连接问题

### 4. KafkaConfig.java

**位置**：`src/main/java/top/codestyle/admin/search/config/KafkaConfig.java`

**作用**：Spring Boot 应用中的 Kafka 配置

**关键 Bean**：

| Bean | 说明 |
|------|------|
| `producerFactory()` | Kafka 生产者工厂 |
| `kafkaTemplate()` | Kafka 模板（用于发送消息） |
| `canalConsumerFactory()` | Canal 消息消费者工厂 |
| `canalKafkaListenerContainerFactory()` | Kafka 监听器容器工厂 |

**消费者配置**：

```java
// 手动提交 offset
configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

// 从最早的消息开始消费（如果没有 offset）
configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

// 批量消费大小
configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);

// 消费者活跃性配置
configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
configProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
```

### 5. CanalKafkaMessageConsumer.java

**位置**：`src/main/java/top/codestyle/admin/search/listener/CanalKafkaMessageConsumer.java`

**作用**：Kafka 消息消费者，处理 Canal 消息

**处理流程**：

```java
@KafkaListener(topics = "data-change", groupId = "codestyle-search")
public void consumeBatchCanalMessages(
    List<ConsumerRecord<String, String>> records,
    Acknowledgment acknowledgment
) {
    // 1. 解析 Canal JSON 消息
    // 2. 转换为 DataChangeMessage
    // 3. 幂等性检查（Redis）
    // 4. 同步到 Elasticsearch
    // 5. 提交 Kafka offset
}
```

**关键特性**：
- ✅ 批量消费（最多 500 条）
- ✅ 手动提交 offset（确保消息处理成功后才提交）
- ✅ 幂等性保证（基于 Redis）
- ✅ 异常处理和死信队列

---

## 部署指南

### 前置条件

1. **Docker Desktop** 已安装并运行
2. **MySQL** 已部署并开启 binlog
3. **Elasticsearch** 已部署（可在 Docker 或宿主机）
4. **Redis** 已部署（用于消息幂等性）

### MySQL 配置

**检查 binlog 是否开启**：

```sql
SHOW VARIABLES LIKE 'log_bin';
-- 应该返回 ON

SHOW VARIABLES LIKE 'binlog_format';
-- 应该返回 ROW（Canal 推荐）
```

**如果未开启，修改 MySQL 配置**（`my.cnf` 或 `my.ini`）：

```ini
[mysqld]
log-bin=mysql-bin
binlog-format=ROW
server-id=1
expire_logs_days=7
```

### 部署步骤

#### 1. 启动 Docker 服务

```bash
# 进入目录
cd codestyle-plugin/codestyle-plugin-search/docker/canal-server

# 启动所有服务（ZooKeeper、Kafka、Canal Server）
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

#### 2. 验证服务状态

```bash
# 检查容器状态
docker ps | grep -E "canal|zookeeper|kafka"

# 检查 Kafka topic 是否创建
docker exec canal-kafka kafka-topics.sh --bootstrap-server localhost:9092 --list

# 应该看到：data-change
```

#### 3. 验证 Canal Server 连接

```bash
# 查看 Canal Server 日志
docker logs -f canal-sync-mysql-server

# 应该看到：
# - "destination:example has been started"
# - "Canal Server startup successfully"
```

#### 4. 测试数据同步

```sql
-- 在 MySQL 中执行更新
UPDATE codestyle.remote_meta_info 
SET update_time = NOW() 
WHERE id = 1;
```

**检查 Kafka 消息**：

```bash
# 消费 Kafka 消息（查看是否有新消息）
docker exec canal-kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic data-change \
  --from-beginning
```

**检查应用日志**：

应用应该输出：
```
Kafka监听器被调用，收到 X 条消息
消息处理完成，已同步到ES
```

### 初次部署验证清单

- [ ] ZooKeeper 容器运行正常
- [ ] Kafka 容器运行正常，topic `data-change` 已创建
- [ ] Canal Server 容器运行正常，已连接 MySQL
- [ ] 应用启动时执行全量同步成功
- [ ] MySQL 数据变更能触发 Kafka 消息
- [ ] 应用能消费 Kafka 消息并同步到 ES

---

## 运维监控

### 日志位置

| 服务 | 日志位置 |
|------|----------|
| **Canal Server** | `docker/canal-server/logs/` |
| **Kafka** | `docker logs canal-kafka` |
| **ZooKeeper** | `docker logs canal-zookeeper` |
| **Spring Boot 应用** | 应用日志（控制台或日志文件） |

### 常用命令

#### 查看服务状态

```bash
# 查看所有容器状态
docker-compose ps

# 查看特定服务日志
docker-compose logs -f canal-server
docker-compose logs -f canal-kafka
docker-compose logs -f zookeeper
```

#### Kafka 管理

```bash
# 查看所有 topic
docker exec canal-kafka kafka-topics.sh --bootstrap-server localhost:9092 --list

# 查看 topic 详情
docker exec canal-kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic data-change

# 查看消费者组
docker exec canal-kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --list

# 查看消费者组详情
docker exec canal-kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group codestyle-search \
  --describe
```

#### 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart canal-server

# 停止所有服务
docker-compose down

# 停止并删除数据卷（谨慎使用）
docker-compose down -v
```

### 监控指标

#### Kafka 监控

- **消息积压**：检查消费者 lag
- **Topic 分区数**：确保分区数合理
- **消息吞吐量**：监控生产/消费速率

#### Canal Server 监控

- **连接状态**：检查是否成功连接 MySQL
- **Binlog 位置**：查看 `logs/example/meta.log`
- **消息发送速率**：监控 Kafka 消息生产速率

#### 应用监控

- **消息消费速率**：监控 Kafka 消费者处理速度
- **ES 同步成功率**：监控同步失败次数
- **幂等性命中率**：监控重复消息数量

---

## 常见问题

### 1. Canal Server 无法启动

**症状**：容器不断重启，日志显示段错误

**原因**：Canal Server 版本不兼容（Windows Docker）

**解决方案**：
- 使用 `canal/canal-server:v1.1.7`（已验证可用）
- 避免使用 `v1.1.5` 或更早版本

### 2. Kafka 连接失败

**症状**：应用启动时报 "Connection to node -1" 错误

**原因**：
- Kafka 未启动
- 端口配置错误
- 网络不通

**解决方案**：
1. 检查 Kafka 容器是否运行：`docker ps | grep kafka`
2. 检查端口配置：
   - Docker 内部：`canal-kafka:9092`
   - 宿主机：`localhost:9094`
3. 检查网络：确保所有服务在同一网络

### 3. Canal Server 无法连接 MySQL

**症状**：日志显示 "GetConnectionTimeoutException"

**原因**：
- MySQL 地址配置错误
- MySQL 用户权限不足
- 网络不通

**解决方案**：
1. 使用 `host.docker.internal:3306` 访问宿主机 MySQL
2. 确保 MySQL 用户有 `REPLICATION SLAVE` 权限
3. 检查 MySQL 是否允许远程连接

### 4. 消息消费不到

**症状**：MySQL 有变更，但应用没有收到消息

**可能原因**：
1. Canal Server 未启动或未连接 MySQL
2. Kafka topic 不存在
3. 消费者组配置错误
4. Offset 位置不对

**排查步骤**：

```bash
# 1. 检查 Canal Server 是否运行
docker ps | grep canal-server

# 2. 检查 Kafka topic 是否存在
docker exec canal-kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list

# 3. 检查消费者组状态
docker exec canal-kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group codestyle-search \
  --describe

# 4. 手动消费消息，验证是否有消息
docker exec canal-kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic data-change \
  --from-beginning
```

### 5. 服务启动顺序问题

**症状**：Canal Server 启动失败，提示无法连接 Kafka

**原因**：Canal Server 在 Kafka 完全就绪前启动

**解决方案**：
- 使用 `depends_on` + `condition: service_healthy` 确保启动顺序
- 健康检查配置已包含在 `docker-compose.yml` 中

### 6. 数据不同步

**症状**：MySQL 有变更，但 ES 没有更新

**排查步骤**：

1. **检查 Canal Server 是否捕获到变更**：
   ```bash
   docker logs -f canal-sync-mysql-server
   ```

2. **检查 Kafka 是否有消息**：
   ```bash
   docker exec canal-kafka kafka-console-consumer.sh \
     --bootstrap-server localhost:9092 \
     --topic data-change \
     --from-beginning
   ```

3. **检查应用是否消费消息**：
   - 查看应用日志，是否有 "Kafka监听器被调用" 的日志

4. **检查幂等性**：
   - 检查 Redis 中是否有消息 ID 记录
   - 如果消息被标记为已处理，可能是幂等性导致跳过

### 7. 初次部署失败

**症状**：删除容器后重新部署，服务无法正常启动

**解决方案**：
- 确保使用最新的 `docker-compose.yml`（包含健康检查）
- 检查数据卷是否已清理：`docker volume ls`
- 按顺序启动：先启动 ZooKeeper，再启动 Kafka，最后启动 Canal Server

---

## 总结

### 核心优势

1. **实时同步**：基于 MySQL binlog 的实时数据同步
2. **高可用**：Kafka 消息队列保证消息不丢失
3. **幂等性**：Redis 保证消息不重复处理
4. **易于部署**：Docker Compose 一键部署
5. **健康检查**：确保服务按正确顺序启动

### 关键配置要点

1. **服务启动顺序**：ZooKeeper → Kafka → Canal Server
2. **网络配置**：使用 Docker 服务名进行内部通信
3. **端口映射**：Kafka 使用 9092（内部）和 9094（外部）
4. **数据持久化**：使用 volumes 保证数据不丢失
5. **健康检查**：确保服务完全就绪后再启动依赖服务

### 版本信息

- **Canal Server**：1.1.7
- **Kafka**：2.13-2.7.0
- **ZooKeeper**：3.9
- **Elasticsearch**：8.13.0
- **Spring Kafka**：最新稳定版

---

**文档版本**：v1.0  
**最后更新**：2025-12-28  
**维护者**：开发团队

