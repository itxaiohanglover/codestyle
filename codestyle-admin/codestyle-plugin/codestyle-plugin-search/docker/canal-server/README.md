# Canal Server Docker部署指南

## 📋 概述

本指南说明如何在Docker Desktop中部署Canal Server，配置为直接发送消息到Kafka。

**版本**：Canal Server 1.1.8  
**模式**：Kafka模式（canal.server.mode=kafka）

---

## 🚀 快速开始

### 1. 前置条件

- ✅ Docker Desktop已安装并运行
- ✅ Kafka已部署（可在Docker中或宿主机）
- ✅ MySQL已部署（可在Docker中或宿主机）
- ✅ MySQL已开启binlog
- ✅ **Zookeeper**（Canal Server必需，docker-compose已包含，或使用外部Zookeeper）

### 2. 检查MySQL Binlog配置

**MySQL配置文件**（`my.cnf`或`my.ini`）：
```ini
[mysqld]
# 开启binlog
log-bin=mysql-bin
# binlog格式（ROW模式，Canal推荐）
binlog-format=ROW
# 服务器ID（主从复制需要）
server-id=1
# binlog过期时间（天）
expire_logs_days=7
```

**验证binlog是否开启**：
```sql
SHOW VARIABLES LIKE 'log_bin';
-- 应该返回 ON

SHOW VARIABLES LIKE 'binlog_format';
-- 应该返回 ROW
```

### 3. 创建MySQL用户（Canal需要）

```sql
-- 创建Canal用户
CREATE USER 'canal'@'%' IDENTIFIED BY 'canal';

-- 授予权限
GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'canal'@'%';

-- 刷新权限
FLUSH PRIVILEGES;
```

---

## 🐳 Docker部署

### 方式1：使用docker-compose（推荐）

```bash
# 进入目录
cd docker/canal-server

# 创建环境变量文件
cat > .env << EOF
MYSQL_HOST=host.docker.internal
MYSQL_PORT=3306
MYSQL_USER=root
MYSQL_PASSWORD=root
MYSQL_DATABASE=codestyle
CANAL_FILTER_REGEX=codestyle\\..*
EOF

# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f canal-server
```

### 方式2：使用docker run

```bash
docker run -d \
  --name canal-server \
  -p 11111:11111 \
  -p 11112:11112 \
  -e canal.server.mode=kafka \
  -e canal.mq.servers=host.docker.internal:9092 \
  -e canal.mq.topic=data-change \
  -e canal.instance.master.address=host.docker.internal:3306 \
  -e canal.instance.dbUsername=root \
  -e canal.instance.dbPassword=root \
  -e canal.instance.filter.regex=codestyle\\..* \
  -v $(pwd)/conf:/home/admin/canal-server/conf \
  -v $(pwd)/logs:/home/admin/canal-server/logs \
  canal/canal-server:v1.1.8
```

---

## ⚙️ 配置说明

### 关键配置项

#### 1. Kafka服务器地址

**Docker Desktop访问宿主机服务**：
```properties
canal.mq.servers = host.docker.internal:9092
```

**如果Kafka也在Docker中**：
```properties
canal.mq.servers = kafka:9092
```

#### 2. MySQL服务器地址

**Docker Desktop访问宿主机MySQL**：
```properties
canal.instance.master.address = host.docker.internal:3306
```

**如果MySQL也在Docker中**：
```properties
canal.instance.master.address = mysql:3306
```

#### 3. 订阅表达式

```properties
# 订阅codestyle数据库下的所有表
canal.instance.filter.regex = codestyle\\..*

# 订阅多个数据库
canal.instance.filter.regex = codestyle\\..*,test\\..*

# 订阅具体表
canal.instance.filter.regex = codestyle\\.meta_info,codestyle\\.user_info
```

---

## 🔍 验证部署

### 1. 检查容器状态

```bash
# 查看容器状态（应该看到canal-server和zookeeper）
docker ps | grep -E "canal|zookeeper"

# 查看Canal Server日志
docker logs -f canal-server

# 查看Zookeeper日志
docker logs -f canal-zookeeper
```

**注意**：如果使用外部Zookeeper，请确保Zookeeper已运行，并检查Canal Server是否能连接。

### 2. 检查Kafka消息

```bash
# 消费Kafka消息（应该能看到Canal发送的消息）
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic data-change \
  --from-beginning
```

### 3. 测试数据变更

```sql
-- 在MySQL中执行变更
UPDATE codestyle.meta_info SET description = 'test' WHERE id = 1;

-- 立即检查Kafka，应该能看到新消息
```

---

## 📊 消息格式

Canal Server发送到Kafka的消息格式（JSON）：

```json
{
  "type": "INSERT",
  "database": "codestyle",
  "table": "meta_info",
  "data": [
    {
      "id": 1,
      "groupId": "com.example",
      "artifactId": "demo",
      "description": "示例项目"
    }
  ],
  "old": null,
  "ts": 1234567890123,
  "isDdl": false,
  "pkNames": ["id"]
}
```

---

## 🔧 常见问题

### 1. 没有Zookeeper怎么办？

**问题**：我没有Zookeeper，docker-compose中的Zookeeper配置有问题吗？

**答案**：
- ✅ **docker-compose已包含Zookeeper**：直接使用`docker-compose up -d`即可，Zookeeper会自动启动
- ✅ **如果已有外部Zookeeper**：请参考`Zookeeper配置说明.md`，配置使用外部Zookeeper
- ⚠️ **Canal Server必需Zookeeper**：无法移除Zookeeper依赖

**解决方案**：
1. **使用docker-compose中的Zookeeper（推荐）**：无需额外配置，直接启动
2. **使用外部Zookeeper**：设置环境变量`ZOOKEEPER_SERVERS=your-zookeeper-host:2181`

详细说明请参考：`Zookeeper配置说明.md`

---

### 2. 无法连接MySQL

**问题**：Canal Server无法连接MySQL

**解决方案**：
- 检查MySQL是否允许远程连接
- 使用`host.docker.internal`访问宿主机MySQL
- 检查MySQL用户权限

### 3. 无法连接Kafka

**问题**：Canal Server无法连接Kafka

**解决方案**：
- 检查Kafka是否运行
- 使用`host.docker.internal:9092`访问宿主机Kafka
- 检查Kafka监听地址配置（`advertised.listeners`）

### 4. 没有消息发送到Kafka

**问题**：MySQL有变更，但Kafka没有消息

**解决方案**：
- 检查订阅表达式是否正确
- 检查MySQL binlog是否开启
- 查看Canal Server日志

---

## 📝 配置文件位置

- **主配置**：`conf/canal.properties`
- **实例配置**：`conf/example/instance.properties`
- **日志目录**：`logs/`

---

## 🚀 生产环境建议

1. **使用环境变量**：敏感信息（密码等）使用环境变量
2. **持久化配置**：使用volume挂载配置文件
3. **监控告警**：配置Canal Server监控
4. **高可用**：部署多个Canal Server实例
5. **资源限制**：设置容器资源限制

---

**文档版本**：v1.0  
**创建日期**：2025/12/23

