# Docker环境下实现延迟队列和死信队列优化策略

## 1. Docker环境准备

### 1.1 创建Dockerfile（包含RabbitMQ延迟插件）
```dockerfile
FROM rabbitmq:3.10-management

# 安装延迟消息插件
RUN apt-get update && apt-get install -y wget && \
    wget https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/3.10.2/rabbitmq_delayed_message_exchange-3.10.2.ez && \
    mv rabbitmq_delayed_message_exchange-3.10.2.ez /opt/rabbitmq/plugins && \
    rabbitmq-plugins enable rabbitmq_delayed_message_exchange

# 暴露端口
EXPOSE 5672 15672

# 启动命令
CMD ["rabbitmq-server"]
```

### 1.2 创建docker-compose.yml配置
```yaml
version: '3.8'

services:
  rabbitmq:
    build: .
    container_name: rabbitmq-delayed
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      - RABBITMQ_DEFAULT_USER=admin
      - RABBITMQ_DEFAULT_PASS=admin123
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
      - rabbitmq_log:/var/log/rabbitmq
    restart: always

volumes:
  rabbitmq_data:
  rabbitmq_log:
```

### 1.3 启动Docker容器
```bash
# 构建并启动容器
docker-compose up -d

# 验证插件是否正确安装
docker exec -it rabbitmq-delayed rabbitmq-plugins list

# 应该能看到 rabbitmq_delayed_message_exchange 插件显示为 [E*]
```

## 2. 应用配置修改

### 2.1 更新application.yml中的RabbitMQ连接配置
```yaml
spring:
  rabbitmq:
    host: localhost  # Docker宿主机IP或容器名称
    port: 5672
    username: admin
    password: admin123
    virtual-host: /
    listener:
      simple:
        acknowledge-mode: manual
        concurrency: 3
        max-concurrency: 10
    template:
      retry:
        enabled: true
        initial-interval: 1000ms
        max-attempts: 3
        max-interval: 10000ms
        multiplier: 2

# 延迟队列配置
rabbitmq:
  delayed:
    enabled: true
    incremental:
      delay-time: 5000
      max-retry-count: 3
    single:
      delay-time: 3000
      max-retry-count: 3
    delete:
      delay-time: 60000
      max-retry-count: 3
```

## 3. Docker环境下的验证步骤

### 3.1 检查RabbitMQ管理界面
访问 http://localhost:15672 登录管理界面（用户名：admin，密码：admin123）

### 3.2 验证延迟交换机
在管理界面中检查是否存在配置的延迟交换机（类型为x-delayed-message）

### 3.3 启动应用并测试
```bash
# 构建应用
docker build -t sync-service:latest .

# 运行应用容器
docker run --name sync-service --link rabbitmq-delayed:rabbitmq -p 8080:8080 sync-service:latest
```

## 4. 容器化环境的优势

### 4.1 环境一致性
Docker确保了开发、测试和生产环境的一致性，避免了环境差异导致的问题

### 4.2 简化部署
通过docker-compose可以一键部署所有依赖服务

### 4.3 资源隔离
每个服务在独立容器中运行，提高了系统的稳定性和安全性

### 4.4 扩展性
可以轻松扩展为多实例部署，提高系统的可用性

## 5. 注意事项

### 5.1 数据持久化
确保RabbitMQ数据正确持久化到卷中，避免容器重启导致数据丢失

### 5.2 网络配置
容器间通信需要正确配置网络，使用容器名称或服务名称作为主机名

### 5.3 资源限制
生产环境中应设置合理的容器资源限制，避免资源竞争

### 5.4 监控集成
将RabbitMQ容器与Prometheus、Grafana等监控工具集成，监控队列状态