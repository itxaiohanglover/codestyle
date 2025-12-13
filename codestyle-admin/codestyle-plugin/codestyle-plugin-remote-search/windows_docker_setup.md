# Windows Docker Desktop环境下实现延迟队列和死信队列优化策略

## 1. Windows Docker Desktop环境准备

### 1.1 安装并配置Docker Desktop
1. 从[Docker官网](https://www.docker.com/products/docker-desktop)下载并安装Docker Desktop for Windows
2. 安装完成后，启动Docker Desktop并确保已启用WSL 2后端（性能更好）
3. 在设置中配置资源限制（建议至少4GB内存）
4. 验证Docker安装：
   ```powershell
   # 打开PowerShell或命令提示符
   docker --version
   docker-compose --version
   ```

### 1.2 创建项目目录结构
```powershell
# 在Windows上创建项目目录
mkdir -p D:\docker\rabbitmq-delayed\rabbitmq
cd D:\docker\rabbitmq-delayed
```

## 2. Windows环境下的Docker配置文件

### 2.1 创建适用于Windows的Dockerfile
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

### 2.2 创建适用于Windows的docker-compose.yml
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
      # Windows下的卷配置，使用Docker Desktop管理的卷
      - rabbitmq_data:/var/lib/rabbitmq
      - rabbitmq_log:/var/log/rabbitmq
    restart: always
    # Windows下的网络配置
    networks:
      - sync-network

networks:
  sync-network:
    driver: bridge

volumes:
  rabbitmq_data:
  rabbitmq_log:
```

## 3. Windows下的Docker容器启动步骤

### 3.1 构建并启动容器（PowerShell）
```powershell
# 进入docker-compose.yml所在目录
cd D:\docker\rabbitmq-delayed

# 构建并启动容器
# 注意：在Windows PowerShell中使用docker-compose命令
# 如果提示命令不存在，可以使用 docker compose (无连字符) 命令
# 或者安装docker-compose：Install-Module -Name DockerCompose

# 方法1：使用docker compose (适用于较新版本)
docker compose up -d

# 方法2：使用docker-compose (适用于旧版本)
docker-compose up -d

# 查看容器状态
docker ps
```

### 3.2 验证插件安装（PowerShell）
```powershell
# 查看容器内运行的插件
docker exec -it rabbitmq-delayed rabbitmq-plugins list

# 应该能看到 rabbitmq_delayed_message_exchange 插件显示为 [E*]
```

## 4. Windows环境下的应用配置调整

### 4.1 更新application.yml中的RabbitMQ连接配置
```yaml
spring:
  rabbitmq:
    # Windows Docker Desktop下可以使用localhost或宿主机IP
    host: localhost
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

## 5. Windows Docker Desktop的特殊注意事项

### 5.1 路径处理
- **Windows路径格式**：在Docker配置文件中，Windows路径需要使用正斜杠 `/` 或转义的反斜杠 `\\`
- **卷挂载**：Windows下建议使用命名卷，避免直接挂载Windows目录（可能有权限问题）

### 5.2 文件权限
- 在Windows上创建的配置文件可能有UTF-8 BOM问题，确保使用Notepad++等工具以UTF-8无BOM格式保存
- 避免Windows特有的行尾符问题（CRLF vs LF）

### 5.3 网络配置
- Windows Docker Desktop默认使用NAT网络模式
- 容器间通信可以通过服务名称访问
- 宿主机访问容器服务使用localhost:端口映射

### 5.4 内存和性能优化
- 在Docker Desktop设置中增加分配的内存（建议至少4GB）
- 启用WSL 2后端以获得更好的性能
- 定期清理未使用的镜像和容器：
  ```powershell
  docker system prune -f
  ```

## 6. Windows环境下的故障排除

### 6.1 常见问题及解决方法

#### 问题1：容器启动失败，提示端口被占用
```powershell
# 查找占用端口的进程
netstat -ano | findstr :5672
# 终止占用端口的进程（根据PID）
taskkill /PID <PID> /F
```

#### 问题2：容器内文件权限问题
```powershell
# 在Dockerfile中添加权限设置
RUN chmod -R 777 /opt/rabbitmq
```

#### 问题3：Windows下的防火墙阻止连接
- 检查Windows防火墙设置，确保端口5672和15672已开放
- 或临时关闭防火墙进行测试

## 7. Windows下的监控集成

### 7.1 使用Docker Desktop内置监控
- Docker Desktop提供了基本的容器CPU、内存、网络监控
- 在Dashboard页面可以查看容器状态

### 7.2 配置Prometheus和Grafana（Docker方式）
```yaml
# 在docker-compose.yml中添加监控服务
prometheus:
  image: prom/prometheus
  container_name: prometheus
  ports:
    - "9090:9090"
  volumes:
    - ./prometheus.yml:/etc/prometheus/prometheus.yml
  networks:
    - sync-network

grafana:
  image: grafana/grafana
  container_name: grafana
  ports:
    - "3000:3000"
  depends_on:
    - prometheus
  networks:
    - sync-network
```

## 8. Windows开发环境整合

### 8.1 IDE配置（如IntelliJ IDEA）
- 配置IDE使用Docker Desktop作为Docker环境
- 添加Docker Compose运行配置
- 启用Docker Compose日志查看

### 8.2 本地调试技巧
- 使用localhost访问RabbitMQ管理界面：http://localhost:15672
- 在应用中设置断点，结合Docker容器运行进行调试
- 使用Docker Desktop的日志功能查看容器输出

### 8.3 性能优化建议
- 使用WSL 2后端（比Hyper-V性能更好）
- 为WSL 2分配足够的内存和CPU资源
- 考虑将项目文件放在WSL 2文件系统中（如果适用），避免跨系统文件访问的性能损失