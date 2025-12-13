# Canal MySQL Binlog 配置指南

## 1. 概述

本文档描述了使用 Canal 进行 MySQL 数据同步时，对 MySQL Binlog 的配置要求。Canal 依赖 MySQL 的 Binlog 机制来捕获数据变更，因此需要特定的配置才能正常工作。

## 2. Canal 对 MySQL Binlog 的配置要求

Canal 依赖 MySQL 的 Binlog 机制来捕获数据变更，因此需要以下配置：

| 配置项 | 要求 | 说明 |
|-------|------|------|
| log_bin | 必须开启 | 开启 MySQL 的 Binlog 功能 |
| binlog_format | 必须为 ROW | Canal 仅支持 ROW 格式，不支持 STATEMENT 或 MIXED 格式 |
| server_id | 必须配置 | 每个 MySQL 实例需要唯一的 server_id |

## 3. 查看当前 MySQL Binlog 配置

使用以下 SQL 命令可以查看当前 MySQL 的 Binlog 配置：

```sql
-- 查看是否开启 Binlog
SHOW VARIABLES LIKE 'log_bin';

-- 查看 Binlog 格式
SHOW VARIABLES LIKE 'binlog_format';

-- 查看 server_id
SHOW VARIABLES LIKE 'server_id';
```

## 4. 修改 MySQL Binlog 配置

### 4.1 找到 MySQL 配置文件

MySQL 的配置文件位置因操作系统而异：

- **Linux/Mac**：`/etc/my.cnf` 或 `/etc/mysql/my.cnf`
- **Windows**：`C:\Program Files\MySQL\MySQL Server 8.0\my.ini`

### 4.2 修改配置文件

在 MySQL 配置文件中添加或修改以下配置：

```ini
[mysqld]
# 开启 Binlog，指定日志文件前缀
log-bin=mysql-bin

# Binlog 格式必须为 ROW
binlog_format=ROW

# 主库唯一 ID，从 1 开始，每个实例必须唯一
server_id=1

# 可选配置：记录每行变更时的 SQL 语句
binlog-rows-query-log-events=true

# 可选配置：Binlog 保留天数
 expire_logs_days=3
```

### 4.3 重启 MySQL 服务

修改配置后，需要重启 MySQL 服务使配置生效：

#### 4.3.1 Linux/Mac

```bash
# systemd 系统
sudo systemctl restart mysql

# SysV 系统
sudo service mysql restart
```

#### 4.3.2 Windows

- 通过服务管理器重启 MySQL 服务
- 或使用命令行：
  ```cmd
  net stop mysql80
  net start mysql80
  ```

## 5. 验证配置是否正确

重启 MySQL 服务后，再次执行以下 SQL 命令验证配置：

```sql
-- 应返回 ON，表示 Binlog 已开启
SHOW VARIABLES LIKE 'log_bin';

-- 应返回 ROW，表示 Binlog 格式正确
SHOW VARIABLES LIKE 'binlog_format';

-- 应返回您配置的 server_id
SHOW VARIABLES LIKE 'server_id';
```

## 6. MySQL 用户权限配置

确保 Canal 使用的 MySQL 用户具有以下权限：

```sql
-- 创建 Canal 用户
CREATE USER 'canal'@'%' IDENTIFIED BY 'canal_password';

-- 授予必要的权限
GRANT REPLICATION SLAVE, REPLICATION CLIENT, SELECT ON *.* TO 'canal'@'%';

-- 刷新权限
FLUSH PRIVILEGES;
```

## 7. 验证 Canal 是否正常工作

配置完成后，重启应用，查看日志是否有 Canal 相关的输出：

```
开始轮询获取 Canal 消息
获取到 Canal 消息，批次 ID: 1, 条目数: 1
Canal 行数据变更，表名: tb_remote_meta_info, 事件类型: INSERT
处理数据变更，表名: tb_remote_meta_info, 事件类型: INSERT, ID: 1
成功将 ID 为 1 的数据同步到 ES
```

## 8. 测试数据变更

在 MySQL 中执行以下操作，测试 Canal 是否能正常捕获数据变更：

```sql
-- 测试插入操作
INSERT INTO tb_remote_meta_info (meta_json) VALUES ('{"groupId": "test", "artifactId": "test", "description": "test"}');

-- 测试更新操作
UPDATE tb_remote_meta_info SET description = 'updated' WHERE id = 1;

-- 测试删除操作
DELETE FROM tb_remote_meta_info WHERE id = 1;
```

查看应用日志，确认是否有对应的同步记录。

## 9. 常见问题排查

### 9.1 Binlog 未开启

**问题**：`log_bin` 返回 `OFF`

**解决方法**：在 MySQL 配置文件中添加 `log-bin=mysql-bin` 并重启服务。

### 9.2 Binlog 格式错误

**问题**：`binlog_format` 返回 `STATEMENT` 或 `MIXED`

**解决方法**：在 MySQL 配置文件中添加 `binlog_format=ROW` 并重启服务。

### 9.3 server_id 未配置

**问题**：`server_id` 返回 `0` 或空

**解决方法**：在 MySQL 配置文件中添加 `server_id=1`（确保每个实例唯一）并重启服务。

### 9.4 Canal 连接问题

**问题**：应用日志中出现连接失败的错误

**解决方法**：
1. 检查 MySQL 服务是否正常运行
2. 检查 Canal 配置中的 MySQL 连接信息是否正确
3. 检查 MySQL 用户的权限是否正确

### 9.5 没有捕获到数据变更

**问题**：MySQL 中执行了数据变更，但 Canal 没有捕获到

**解决方法**：
1. 检查 Binlog 配置是否正确
2. 检查 Canal 配置中的数据库和表名是否正确
3. 检查 MySQL 用户是否有权限访问对应的数据库和表

## 10. 参考资料

- [Canal 官方文档](https://github.com/alibaba/canal)
- [MySQL Binlog 官方文档](https://dev.mysql.com/doc/refman/8.0/en/binary-log.html)

## 11. 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0 | 2025-12-13 | 初始版本 |
