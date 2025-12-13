# Canal MySQL Binlog 配置指南

## 1. 为什么只有 TRANSACTIONBEGIN/END 而没有 ROWDATA？

当 Canal 只显示 `TRANSACTIONBEGIN` 和 `TRANSACTIONEND` 事件，而没有显示 `ROWDATA` 事件时，通常是由于 MySQL 的 binlog 格式配置不正确导致的。

## 2. MySQL Binlog 配置要求

Canal 基于 MySQL binlog 实现数据同步，必须要求 MySQL 使用 `ROW` 格式的 binlog。

### 2.1 检查当前 binlog 配置

在 MySQL 命令行中执行以下命令，检查当前的 binlog 配置：

```sql
SHOW VARIABLES LIKE 'binlog_format';
SHOW VARIABLES LIKE 'log_bin';
SHOW VARIABLES LIKE 'binlog_row_image';
```

### 2.2 正确的配置值

- `binlog_format`：必须设置为 `ROW`
- `log_bin`：必须设置为 `ON`
- `binlog_row_image`：建议设置为 `FULL`（默认值）

### 2.3 修改 MySQL 配置

1. 打开 MySQL 配置文件（通常是 `/etc/my.cnf` 或 `/etc/mysql/my.cnf`）
2. 在 `[mysqld]` 部分添加或修改以下配置：

```ini
[mysqld]
# 启用 binlog
log-bin=mysql-bin
# 设置 binlog 格式为 ROW
binlog_format=ROW
# 设置 binlog 行镜像为 FULL
binlog_row_image=FULL
# 设置 server-id（必须唯一）
server-id=1
# 可选：设置 binlog 保留时间（单位：秒）
expire_logs_days=7
```

3. 重启 MySQL 服务

### 2.4 验证配置

重启 MySQL 后，再次执行以下命令验证配置：

```sql
SHOW VARIABLES LIKE 'binlog_format';
```

确保输出结果为：

```
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| binlog_format | ROW   |
+---------------+-------+
```

## 3. 其他注意事项

### 3.1 表必须有主键

Canal 要求同步的表必须有主键，否则可能无法正确处理数据变更。

### 3.2 Canal 配置

确保 Canal 客户端配置正确，特别是：

- `canal.instance.master.address`：MySQL 主库地址
- `canal.instance.dbUsername`：具有 `REPLICATION SLAVE` 权限的 MySQL 用户
- `canal.instance.dbPassword`：MySQL 用户密码

### 3.3 MySQL 用户权限

Canal 用户需要以下权限：

```sql
GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'canal'@'%' IDENTIFIED BY 'canal';
```

## 4. 测试数据变更

配置完成后，可以测试数据变更是否能被 Canal 正确捕获：

1. 在 MySQL 中插入一条数据
2. 观察 Canal 日志，应该能看到包含 `ROWDATA` 的事件
3. 检查 ES 中是否已同步该数据

## 5. 常见问题排查

### 5.1 仍然没有 ROWDATA？

- 确认 MySQL binlog 格式确实为 `ROW`
- 确认表有主键
- 确认 Canal 客户端配置正确
- 查看 MySQL 错误日志，检查是否有 binlog 相关错误

### 5.2 数据同步延迟

- 检查 MySQL 服务器负载
- 检查网络连接
- 调整 Canal 客户端的 `batchSize` 和 `timeout` 参数

## 6. 总结

Canal 只显示事务开始和结束而没有行数据，通常是由于 MySQL binlog 格式不是 ROW 格式导致的。通过正确配置 MySQL binlog 格式为 ROW，并确保表有主键，可以解决这个问题。
