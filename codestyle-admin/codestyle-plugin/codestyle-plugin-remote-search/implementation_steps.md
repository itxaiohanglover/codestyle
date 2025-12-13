# 延迟队列和死信队列优化实现步骤

## 1. 环境准备

### 1.1 安装RabbitMQ延迟插件
```bash
# 下载插件
wget https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/3.10.2/rabbitmq_delayed_message_exchange-3.10.2.ez

# 复制到RabbitMQ插件目录
cp rabbitmq_delayed_message_exchange-3.10.2.ez /usr/lib/rabbitmq/lib/rabbitmq_server-3.10.2/plugins/

# 启用插件
rabbitmq-plugins enable rabbitmq_delayed_message_exchange

# 重启RabbitMQ服务
systemctl restart rabbitmq-server
```

### 1.2 检查环境依赖
- 确保Spring Boot版本兼容性
- 检查Spring AMQP依赖配置

## 2. 配置文件修改

### 2.1 更新RabbitMQ配置类
```java
// 修改RabbitMQConfig.java，添加延迟队列和死信队列配置
```

### 2.2 添加相关配置属性
```yaml
# 在application.yml中添加延迟队列相关配置
rabbitmq:
  # 现有配置...
  delayed:
    enabled: true
    incremental:
      delay-time: 5000
      max-retry-count: 3
    single:
      delay-time: 3000
      max-retry-count: 3
    delete:
      delay-time: 60000  # 删除操作延迟1分钟
      max-retry-count: 3
```

## 3. 代码实现

### 3.1 消息模型类创建
创建统一的同步消息模型类：
```java
package top.codestyle.model.rabbitmq;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class SyncMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String syncType; // incremental, single, delete
    private Map<String, Object> data;
    private int retryCount = 0;
    private int maxRetryCount = 3;
    private long delayTime; // 毫秒
    private LocalDateTime timestamp = LocalDateTime.now();
}
```

### 3.2 RabbitMQ配置扩展
```java
// 在RabbitMQConfig.java中添加以下配置

// 1. 增量同步相关配置
public static final String INCREMENTAL_DELAY_EXCHANGE_NAME = "incremental.delay.exchange";
public static final String INCREMENTAL_DELAY_QUEUE_NAME = "incremental.delay.queue";
public static final String INCREMENTAL_DLX_EXCHANGE_NAME = "incremental.dlx.exchange";
public static final String INCREMENTAL_DLX_QUEUE_NAME = "incremental.dlx.queue";
public static final String INCREMENTAL_PROCESS_QUEUE_NAME = "incremental.process.queue";
public static final String INCREMENTAL_ROUTING_KEY = "incremental.routing.key";

// 2. 单条同步相关配置
public static final String SINGLE_DELAY_EXCHANGE_NAME = "single.delay.exchange";
public static final String SINGLE_DELAY_QUEUE_NAME = "single.delay.queue";
public static final String SINGLE_DLX_EXCHANGE_NAME = "single.dlx.exchange";
public static final String SINGLE_DLX_QUEUE_NAME = "single.dlx.queue";
public static final String SINGLE_PROCESS_QUEUE_NAME = "single.process.queue";
public static final String SINGLE_ROUTING_KEY = "single.routing.key";

// 3. 删除同步相关配置
public static final String DELETE_DELAY_EXCHANGE_NAME = "delete.delay.exchange";
public static final String DELETE_DELAY_QUEUE_NAME = "delete.delay.queue";
public static final String DELETE_DLX_EXCHANGE_NAME = "delete.dlx.exchange";
public static final String DELETE_DLX_QUEUE_NAME = "delete.dlx.queue";
public static final String DELETE_PROCESS_QUEUE_NAME = "delete.process.queue";
public static final String DELETE_ROUTING_KEY = "delete.routing.key";

// 4. 创建延迟交换机
@Bean
public CustomExchange incrementalDelayExchange() {
    Map<String, Object> args = new HashMap<>();
    args.put("x-delayed-type", "direct");
    return new CustomExchange(INCREMENTAL_DELAY_EXCHANGE_NAME, "x-delayed-message", true, false, args);
}

// 类似地创建其他延迟交换机...

// 5. 创建处理队列
@Bean
public Queue incrementalProcessQueue() {
    return QueueBuilder.durable(INCREMENTAL_PROCESS_QUEUE_NAME)
            .withArgument("x-dead-letter-exchange", INCREMENTAL_DLX_EXCHANGE_NAME)
            .withArgument("x-dead-letter-routing-key", INCREMENTAL_ROUTING_KEY)
            .build();
}

// 类似地创建其他处理队列...

// 6. 创建死信交换机和队列
@Bean
public DirectExchange incrementalDlxExchange() {
    return ExchangeBuilder.directExchange(INCREMENTAL_DLX_EXCHANGE_NAME)
            .durable(true)
            .build();
}

@Bean
public Queue incrementalDlxQueue() {
    return QueueBuilder.durable(INCREMENTAL_DLX_QUEUE_NAME).build();
}

// 类似地创建其他死信交换机和队列...

// 7. 创建绑定关系
@Bean
public Binding incrementalDelayBinding(Queue incrementalDelayQueue, CustomExchange incrementalDelayExchange) {
    return BindingBuilder.bind(incrementalDelayQueue)
            .to(incrementalDelayExchange)
            .with(INCREMENTAL_ROUTING_KEY)
            .noargs();
}

// 类似地创建其他绑定关系...
```

### 3.3 消息发送服务
```java
package top.codestyle.services;

public interface DelayMessageService {
    // 发送增量同步延迟消息
    void sendIncrementalSyncMessage(Map<String, Object> data, long delayTime);
    
    // 发送单条数据同步延迟消息
    void sendSingleSyncMessage(Map<String, Object> data, long delayTime);
    
    // 发送删除同步延迟消息
    void sendDeleteSyncMessage(Map<String, Object> data, long delayTime);
    
    // 处理重试消息
    void handleRetryMessage(SyncMessage message);
}
```

实现类：
```java
package top.codestyle.services.impl;

@Service
@AllArgsConstructor
public class DelayMessageServiceImpl implements DelayMessageService {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Override
    public void sendIncrementalSyncMessage(Map<String, Object> data, long delayTime) {
        SyncMessage message = new SyncMessage();
        message.setSyncType("incremental");
        message.setData(data);
        message.setDelayTime(delayTime);
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.INCREMENTAL_DELAY_EXCHANGE_NAME,
                RabbitMQConfig.INCREMENTAL_ROUTING_KEY,
                message,
                msg -> {
                    msg.getMessageProperties().setDelay((int) delayTime);
                    return msg;
                }
        );
    }
    
    // 实现其他方法...
    
    @Override
    public void handleRetryMessage(SyncMessage message) {
        if (message.getRetryCount() >= message.getMaxRetryCount()) {
            // 超过最大重试次数，发送到告警队列
            sendToAlarmQueue(message);
            return;
        }
        
        // 增加重试次数
        message.setRetryCount(message.getRetryCount() + 1);
        
        // 计算指数退避延迟时间
        long newDelayTime = calculateExponentialBackoffDelay(message.getRetryCount());
        message.setDelayTime(newDelayTime);
        
        // 根据消息类型发送到对应的延迟队列
        switch (message.getSyncType()) {
            case "incremental":
                sendToDelayQueue(message, RabbitMQConfig.INCREMENTAL_DELAY_EXCHANGE_NAME, RabbitMQConfig.INCREMENTAL_ROUTING_KEY);
                break;
            case "single":
                sendToDelayQueue(message, RabbitMQConfig.SINGLE_DELAY_EXCHANGE_NAME, RabbitMQConfig.SINGLE_ROUTING_KEY);
                break;
            case "delete":
                sendToDelayQueue(message, RabbitMQConfig.DELETE_DELAY_EXCHANGE_NAME, RabbitMQConfig.DELETE_ROUTING_KEY);
                break;
        }
    }
    
    private long calculateExponentialBackoffDelay(int retryCount) {
        // 指数退避算法：基础时间 * (2^重试次数) + 随机因子
        long baseDelay = 5000; // 基础延迟5秒
        return (long) (baseDelay * Math.pow(2, retryCount)) + ThreadLocalRandom.current().nextInt(1000);
    }
    
    private void sendToDelayQueue(SyncMessage message, String exchange, String routingKey) {
        rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                message,
                msg -> {
                    msg.getMessageProperties().setDelay((int) message.getDelayTime());
                    return msg;
                }
        );
    }
    
    private void sendToAlarmQueue(SyncMessage message) {
        // 实现告警队列发送逻辑
    }
}
```

### 3.4 消息处理器修改
创建新的消息处理器：

```java
package top.codestyle.handler;

@Slf4j
@Component
@AllArgsConstructor
public class DelaySyncMessageHandler {
    
    private final SyncService syncService;
    private final DelayMessageService delayMessageService;
    
    // 增量同步消息处理器
    @RabbitListener(queues = RabbitMQConfig.INCREMENTAL_PROCESS_QUEUE_NAME)
    public void handleIncrementalSyncMessage(SyncMessage message) {
        log.info("处理增量同步消息: {}, 重试次数: {}", message.getData(), message.getRetryCount());
        
        try {
            String lastSyncTimeStr = (String) message.getData().get("lastSyncTime");
            LocalDateTime lastSyncTime = LocalDateTime.parse(lastSyncTimeStr);
            int count = syncService.incrementalSync(lastSyncTime);
            log.info("增量同步成功，同步 {} 条数据", count);
        } catch (Exception e) {
            log.error("增量同步失败: {}", e.getMessage(), e);
            // 发送重试消息
            delayMessageService.handleRetryMessage(message);
        }
    }
    
    // 单条数据同步消息处理器
    @RabbitListener(queues = RabbitMQConfig.SINGLE_PROCESS_QUEUE_NAME)
    public void handleSingleSyncMessage(SyncMessage message) {
        log.info("处理单条同步消息: {}, 重试次数: {}", message.getData(), message.getRetryCount());
        
        try {
            Long id = Long.valueOf((String) message.getData().get("id"));
            boolean success = syncService.syncById(id);
            if (success) {
                log.info("单条数据同步成功，ID: {}", id);
            } else {
                throw new RuntimeException("单条数据同步失败");
            }
        } catch (Exception e) {
            log.error("单条数据同步失败: {}", e.getMessage(), e);
            // 发送重试消息
            delayMessageService.handleRetryMessage(message);
        }
    }
    
    // 删除同步消息处理器
    @RabbitListener(queues = RabbitMQConfig.DELETE_PROCESS_QUEUE_NAME)
    public void handleDeleteSyncMessage(SyncMessage message) {
        log.info("处理删除同步消息: {}, 重试次数: {}", message.getData(), message.getRetryCount());
        
        try {
            int count = ((SyncServiceImpl) syncService).syncDeletedData();
            log.info("删除同步成功，处理 {} 条数据", count);
        } catch (Exception e) {
            log.error("删除同步失败: {}", e.getMessage(), e);
            // 发送重试消息
            delayMessageService.handleRetryMessage(message);
        }
    }
    
    // 死信队列处理器
    @RabbitListener(queues = {RabbitMQConfig.INCREMENTAL_DLX_QUEUE_NAME,
                              RabbitMQConfig.SINGLE_DLX_QUEUE_NAME,
                              RabbitMQConfig.DELETE_DLX_QUEUE_NAME})
    public void handleDlxMessage(SyncMessage message, Channel channel, Message amqpMessage) throws IOException {
        log.warn("收到死信消息: {}, 类型: {}", message.getData(), message.getSyncType());
        
        // 直接调用重试处理
        delayMessageService.handleRetryMessage(message);
        
        // 确认消息已处理
        channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
    }
}
```

### 3.5 调度器修改
修改SyncScheduler，使用延迟消息服务：

```java
@Slf4j
@Component
@RefreshScope
public class SyncScheduler implements ApplicationRunner {
    
    @Autowired
    private DelayMessageService delayMessageService;
    
    @Value("${rabbitmq.delayed.incremental.delay-time:5000}")
    private long incrementalDelayTime;
    
    @Value("${rabbitmq.delayed.single.delay-time:3000}")
    private long singleDelayTime;
    
    @Value("${rabbitmq.delayed.delete.delay-time:60000}")
    private long deleteDelayTime;
    
    // 上次同步时间
    private LocalDateTime lastSyncTime;
    
    @Override
    public void run(ApplicationArguments args) {
        log.info("服务启动，开始发送全量同步消息");
        try {
            // 构建全量同步消息
            Map<String, Object> message = new HashMap<>();
            message.put("syncType", "full");
            
            // 这里可以继续使用原有队列，或者也移到延迟队列中
            // 发送消息到RabbitMQ
            
            log.info("服务启动全量同步消息发送完成");
            lastSyncTime = LocalDateTime.now();
        } catch (Exception e) {
            log.error("服务启动全量同步消息发送失败: {}", e.getMessage());
        }
    }
    
    @Scheduled(fixedRateString = "${sync.interval:30000}")
    public void scheduledIncrementalSync() {
        log.info("定时发送增量同步消息，当前间隔: {}ms", syncInterval);
        
        try {
            Map<String, Object> data = new HashMap<>();
            
            if (lastSyncTime == null) {
                // 发送全量同步
                // ...
            } else {
                // 使用延迟消息服务发送增量同步消息
                data.put("lastSyncTime", lastSyncTime.toString());
                delayMessageService.sendIncrementalSyncMessage(data, incrementalDelayTime);
            }
            
            lastSyncTime = LocalDateTime.now();
        } catch (Exception e) {
            log.error("发送增量同步消息失败: {}", e.getMessage());
        }
    }
    
    // 修改其他调度方法...
}
```

## 4. 测试验证

### 4.1 单元测试
为每个消息处理器编写单元测试：
```java
@Test
public void testIncrementalSyncMessageHandler() {
    // 模拟增量同步消息
    SyncMessage message = new SyncMessage();
    message.setSyncType("incremental");
    Map<String, Object> data = new HashMap<>();
    data.put("lastSyncTime", LocalDateTime.now().minusHours(1).toString());
    message.setData(data);
    
    // 调用处理器
    delaySyncMessageHandler.handleIncrementalSyncMessage(message);
    
    // 验证结果
    // ...
}
```

### 4.2 集成测试
```java
@Test
public void testDelayQueueFlow() {
    // 发送延迟消息
    Map<String, Object> data = new HashMap<>();
    data.put("id", "123");
    delayMessageService.sendSingleSyncMessage(data, 1000);
    
    // 等待延迟时间
    Thread.sleep(1500);
    
    // 验证消息是否被正确处理
    // ...
}
```

### 4.3 性能测试
测试在高并发场景下的消息处理性能：
```java
@Test
public void testHighConcurrency() {
    int messageCount = 1000;
    CountDownLatch latch = new CountDownLatch(messageCount);
    
    ExecutorService executor = Executors.newFixedThreadPool(10);
    
    for (int i = 0; i < messageCount; i++) {
        final int id = i;
        executor.submit(() -> {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("id", String.valueOf(id));
                delayMessageService.sendSingleSyncMessage(data, 100);
            } finally {
                latch.countDown();
            }
        });
    }
    
    latch.await();
    // 验证处理结果
    // ...
}
```

## 5. 部署与监控

### 5.1 部署注意事项
- 确保RabbitMQ延迟插件正确安装
- 调整队列参数以适应生产环境
- 配置合理的资源限制

### 5.2 监控配置
添加Spring Boot Actuator监控：
```yaml
management:
  endpoints:
    web:
      exposure:
        include: rabbitmq,health,metrics
  endpoint:
    rabbitmq:
      enabled: true
```

### 5.3 告警配置
配置Prometheus和Grafana监控RabbitMQ队列状态：
- 监控队列消息积压情况
- 监控死信队列消息数量
- 监控消息处理延迟时间

## 6. 后续优化方向

### 6.1 消息去重
实现基于Redis的消息去重机制，避免重复处理相同消息

### 6.2 动态配置
实现消息处理参数的动态调整，如延迟时间、重试次数等

### 6.3 分片处理
对大批量同步任务实现更细粒度的分片处理

### 6.4 可视化管理
开发消息队列管理界面，方便运维人员监控和管理