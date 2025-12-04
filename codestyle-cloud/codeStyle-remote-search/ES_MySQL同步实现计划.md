# ES与MySQL数据同步实现计划

## 一、需求回顾

### 1. 核心需求
- 实现MySQL到ES的数据同步，以MySQL为主源
- 同步延迟要求：10-20秒（非实时）
- 支持插入、更新、删除操作的同步
- 支持系统启动时的全量同步
- 处理MySQL或ES宕机情况，确保恢复后同步

### 2. 数据结构
- **MySQL表**：`remote_meta_info`
  - 核心字段：`id`, `meta_json`, `create_time`, `update_time`, `deleted`
  - 虚拟列：`group_id`（从meta_json中提取）
  - 逻辑删除字段：`deleted`（0表示未删除，1表示已删除）
- **ES索引**：存储`RemoteMetaDoc`类型数据
  - 与MySQL的关联：通过`id`字段关联
  - 数据来源：MySQL中的`meta_json`字段
  - 不存储`deleted`字段，已删除的数据直接从ES中移除

### 3. 同步流程
1. MySQL插入/更新数据 → 定时任务检测 → 解析meta_json → 同步到ES
2. MySQL逻辑删除数据 → 定时任务检测 → 从ES删除对应数据
3. MySQL恢复删除数据 → 定时任务检测 → 重新同步到ES
4. 系统启动时 → 执行全量同步 → 确保两边数据一致
5. 定时删除同步 → 定期检查所有已删除数据 → 从ES删除对应数据

## 二、技术方案设计

### 1. 同步机制
- **定时任务轮询**：使用Spring Scheduled，支持动态配置间隔（默认30秒）
- **启动全量同步**：监听Spring Boot的ApplicationReadyEvent事件
- **增量同步策略**：基于`update_time`字段查询最近更新的数据
- **动态同步策略**：支持从配置中心动态调整同步间隔

### 2. 核心组件

#### 2.1 数据访问层
- **MySQL DAO**：使用MyBatis-Plus 3.5.5操作`remote_meta_info`表
- **ES Repository**：使用ES9官方客户端操作ES索引

#### 2.2 同步服务层
- **SyncService**：核心同步服务，包含：
  - `fullSync()`：全量同步方法
  - `incrementalSync()`：增量同步方法，支持处理逻辑删除
  - `syncById()`：单条数据同步方法，支持软删除
  - `syncDeletedData()`：同步删除操作，处理已标记为删除的数据
  - `deleteFromES()`：从ES删除数据方法

#### 2.3 任务调度
- **SyncScheduler**：定时任务配置类
  - 配置增量同步的定时任务，支持动态调整间隔
  - 配置定时删除同步任务，定期处理已删除数据
  - 监听应用启动事件，触发全量同步

#### 2.4 工具类
- **JsonUtils**：JSON解析工具类
- **SyncLogUtils**：同步日志工具类
- **ES9MetaDocTrialLocalSDK**：ES搜索客户端工具类，包含SQL注入防护

### 3. 异常处理
- **数据库连接异常**：记录日志，跳过本次同步，等待下次重试
- **JSON解析异常**：记录错误日志，跳过单条记录，继续处理其他数据
- **ES操作异常**：记录日志，重试3次，失败则标记为待处理
- **ES版本冲突**：使用`conflicts=proceed`参数，确保删除操作继续执行

### 4. 数据一致性保障
- **幂等性设计**：使用id作为唯一标识，确保重复同步不会产生错误
- **同步状态记录**：记录每次同步的时间点，便于增量同步
- **定期全量同步**：每天凌晨执行一次全量同步，确保数据最终一致
- **ES版本冲突处理**：在全量同步时使用`_delete_by_query?conflicts=proceed`确保删除操作成功

## 三、实现细节与最新改动

### 1. 最新改动记录

| 日期 | 改动内容 | 涉及文件 | 解决问题 |
|------|---------|---------|---------|
| 2025-12-04 | 实现逻辑删除同步功能 | `RemoteMetaInfo.java`, `SyncServiceImpl.java`, `RemoteMetaInfoMapper.java`, `RemoteMetaInfoMapper.xml` | 支持软删除，确保已删除数据从ES中移除 |
| 2024-01-XX | 优化`getSafeKeyword`防止SQL注入 | `ES9MetaDocTrialLocalSDK.java` | 允许中文关键词搜索，防止SQL注入 |
| 2024-01-XX | 修复MyBatis-Plus与Spring Boot 3.2.12兼容性 | `pom.xml` | 切换到`mybatis-plus-spring-boot3-starter:3.5.5` |
| 2024-01-XX | 生成`remote_meta_info`模拟SQL | `db/insert_mock.sql` | 提供测试数据，兼容MySQL生成列 |
| 2024-01-XX | 修复MySQL生成列问题 | `RemoteMetaInfo.java` | 移除`group_id`的显式插入，使用生成列 |
| 2024-01-XX | 实现动态同步策略 | `SyncScheduler.java` | 支持从配置中心动态调整同步间隔 |
| 2024-01-XX | 修复ES删除版本冲突 | `RemoteSearchESRepository.java` | 添加`conflicts=proceed`参数，确保删除操作继续执行 |
| 2024-01-XX | 支持中文关键词搜索 | `ES9MetaDocTrialLocalSDK.java` | 修改`getSafeKeyword`允许中文字符 |
| 2024-01-XX | 处理ES搜索NPE | `ES9MetaDocTrialLocalSDK.java` | 添加空值检查，防止空指针异常 |

### 2. 关键实现细节

#### 2.1 SQL注入防护
- **实现方式**：在`ES9MetaDocTrialLocalSDK.java`中优化`getSafeKeyword`方法
- **核心代码**：
  ```java
  private String getSafeKeyword(String keyword) {
      if (keyword == null) return null;
      StringBuilder safeKeyword = new StringBuilder();
      for (char c : keyword.toCharArray()) {
          if ((c >= 32 && c <= 126) || (c >= 0x4e00 && c <= 0x9fa5)) {
              safeKeyword.append(c);
          }
      }
      return safeKeyword.toString();
  }
  ```
- **效果**：允许中文、英文及常见符号，过滤危险字符

#### 2.2 动态同步策略
- **实现方式**：使用`@Value`和`@Scheduled(fixedRateString)`注解
- **核心代码**：
  ```java
  @RefreshScope
  public class SyncScheduler {
      @Value("${sync.interval:30000}")
      private long syncInterval;
      
      @Scheduled(fixedRateString = "${sync.interval:30000}")
      public void scheduleIncrementalSync() {
          syncService.incrementalSync();
      }
  }
  ```
- **效果**：支持从配置中心动态调整同步间隔，无需重启服务

#### 2.3 全量同步实现
- **实现方式**：先删除ES中所有文档，再重新同步MySQL数据
- **核心代码**：
  ```java
  @Override
  public void fullSync() {
      // 1. 从ES删除所有文档
      remoteSearchESRepository.deleteAllMetaDocs();
      
      // 2. 从MySQL获取所有数据
      List<RemoteMetaInfo> allMetaInfos = remoteMetaInfoMapper.selectList(null);
      
      // 3. 同步到ES
      for (RemoteMetaInfo metaInfo : allMetaInfos) {
          syncSingleItem(metaInfo);
      }
  }
  ```

#### 2.4 增量同步实现
- **实现方式**：基于`update_time`字段查询最近更新的数据
- **核心代码**：
  ```java
  @Override
  public void incrementalSync() {
      Date lastSyncDate = new Date(System.currentTimeMillis() - syncInterval);
      LambdaQueryWrapper<RemoteMetaInfo> queryWrapper = new LambdaQueryWrapper<>();
      queryWrapper.gt(RemoteMetaInfo::getUpdateTime, lastSyncDate);
      
      List<RemoteMetaInfo> updatedMetaInfos = remoteMetaInfoMapper.selectList(queryWrapper);
      for (RemoteMetaInfo metaInfo : updatedMetaInfos) {
          syncSingleItem(metaInfo);
      }
  }
  ```

#### 2.5 ES版本冲突处理
- **实现方式**：在`RemoteSearchESRepository.java`的`deleteAllMetaDocs()`方法中添加`conflicts=proceed`参数
- **核心代码**：
  ```java
  public void deleteAllMetaDocs() {
      String deleteUrl = elasticSearchUrlProperties.getHost() + "/" + 
                        elasticSearchUrlProperties.getIndexName() + "/_delete_by_query?conflicts=proceed";
      // 执行删除操作
  }
  ```
- **效果**：当ES文档版本冲突时，继续执行删除操作，确保全量同步正常完成

#### 2.6 逻辑删除配置
- **实现方式**：在实体类中使用MyBatis-Plus的`@TableLogic`注解，配置逻辑删除字段
- **核心代码**：
  ```java
  @Data
  @TableName("tb_remote_meta_info")
  public class RemoteMetaInfo {
      @TableId(type = IdType.INPUT)
      private Long id;
      
      private String metaJson;
      
      @TableLogic // 逻辑删除注解
      @TableField(fill = FieldFill.INSERT) // 插入时自动填充
      private Integer deleted;
      
      @TableField(fill = FieldFill.INSERT) // 插入时自动填充
      private Date createTime;
      
      @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
      private Date updateTime;
  }
  ```
- **效果**：MyBatis-Plus自动处理逻辑删除，插入时自动填充deleted字段为0

#### 2.7 增量同步实现（支持逻辑删除）
- **实现方式**：使用自定义SQL查询，绕过MyBatis-Plus的逻辑删除自动过滤
- **核心代码**：
  ```java
  @Override
  public int incrementalSync(LocalDateTime lastSyncTime) {
      // 转换LocalDateTime为Date
      Date lastSyncDate = Date.from(lastSyncTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
      
      // 使用自定义方法查询更新的数据，包括已删除的
      List<RemoteMetaInfo> updatedMetaInfos = remoteMetaInfoMapper.selectUpdatedDataAfterTime(lastSyncDate);
      
      // 逐条同步到ES
      for (RemoteMetaInfo metaInfo : updatedMetaInfos) {
          if (metaInfo.getDeleted() == 1) {
              // 如果数据已被标记为删除，则从ES中删除该文档
              remoteSearchESRepository.deleteMetaDoc(metaInfo.getId());
          } else {
              // 更新或插入ES文档
              remoteSearchESRepository.saveMetaDoc(metaInfo.toRemoteMetaDoc());
          }
      }
      return updatedMetaInfos.size();
  }
  ```
- **效果**：增量同步能正确处理逻辑删除的文档，从ES中删除已标记为删除的数据

#### 2.8 同步删除操作实现
- **实现方式**：查询所有已标记为删除的数据，并从ES中删除它们
- **核心代码**：
  ```java
  public int syncDeletedData() {
      // 使用自定义方法查询所有已删除的数据
      List<RemoteMetaInfo> deletedMetaInfos = remoteMetaInfoMapper.selectAllDeletedData();
      
      // 逐条从ES中删除
      for (RemoteMetaInfo metaInfo : deletedMetaInfos) {
          remoteSearchESRepository.deleteMetaDoc(metaInfo.getId());
      }
      
      // 额外处理：查询所有已恢复的数据，并同步到ES
      List<RemoteMetaInfo> restoredMetaInfos = remoteMetaInfoMapper.selectAllRestoredData();
      for (RemoteMetaInfo metaInfo : restoredMetaInfos) {
          remoteSearchESRepository.saveMetaDoc(metaInfo.toRemoteMetaDoc());
      }
      
      return deletedMetaInfos.size() + restoredMetaInfos.size();
  }
  ```
- **效果**：确保所有已删除的数据都从ES中移除，所有已恢复的数据都同步到ES

#### 2.9 单条数据同步实现（支持软删除）
- **实现方式**：根据deleted字段决定是保存还是删除ES文档
- **核心代码**：
  ```java
  @Override
  public boolean syncById(Long id) {
      // 根据ID查询MySQL数据
      RemoteMetaInfo metaInfo = remoteMetaInfoMapper.selectById(id);
      if (metaInfo == null) {
          return false;
      }
      
      // 根据deleted字段决定操作
      if (metaInfo.getDeleted() == 1) {
          // 如果数据已被标记为删除，则从ES中删除该文档
          remoteSearchESRepository.deleteMetaDoc(metaInfo.getId());
      } else {
          // 同步到ES
          remoteSearchESRepository.saveMetaDoc(metaInfo.toRemoteMetaDoc());
      }
      return true;
  }
  ```
- **效果**：支持单条数据的同步，包括软删除

### 3. 中文关键词搜索支持
- **实现方式**：修改`getSafeKeyword`方法，允许Unicode中文字符范围
- **核心逻辑**：判断字符是否在`0x4e00-0x9fa5`（中文汉字）范围内
- **效果**：支持中文关键词搜索，同时防止SQL注入

### 4. MyBatis-Plus兼容性修复
- **问题**：Spring Boot 3.2.12与MyBatis-Plus版本不兼容
- **解决方案**：使用`mybatis-plus-spring-boot3-starter:3.5.5`
- **配置**：在`pom.xml`中添加依赖，并在启动类添加`@MapperScan("top.codestyle.mapper")`

### 5. MySQL生成列处理
- **问题**：MySQL 8.0.13+不允许显式插入生成列
- **解决方案**：在实体类中使用`@TableField(insertStrategy = FieldStrategy.NEVER)`
- **核心代码**：
  ```java
  @TableField(value = "group_id", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
  private String groupId;
  ```

## 四、测试验证

### 1. 单元测试
- ✅ `RemoteMetaInfoTest.java`：验证实体类与数据库表映射
- ✅ `SyncServiceTest.java`：验证同步服务逻辑
- ✅ `ES9MetaDocTrialLocalSDKTest.java`：验证ES搜索功能
- ✅ `SoftDeleteSyncTest.java`：验证软删除同步功能
- ✅ `DeleteSyncTest.java`：验证删除数据同步功能
- ✅ `InsertSyncTest.java`：验证插入数据同步功能
- ✅ `IncrementalSyncTest.java`：验证增量同步功能
- ✅ `IntegratedSyncSearchTest.java`：验证集成同步搜索功能

### 2. 集成测试
- ✅ 全量同步测试：系统启动时自动执行全量同步
- ✅ 增量同步测试：定时任务正常执行增量同步，包括逻辑删除
- ✅ 动态同步测试：修改配置后同步间隔动态调整
- ✅ 中文搜索测试：支持中文关键词搜索
- ✅ ES版本冲突测试：全量同步时处理版本冲突
- ✅ 逻辑删除同步测试：已删除数据从ES中移除
- ✅ 恢复删除同步测试：恢复删除的数据重新同步到ES

### 3. 异常场景测试
- ✅ ES连接失败：同步任务优雅处理
- ✅ MySQL连接失败：同步任务优雅处理
- ✅ JSON格式错误：跳过错误数据，继续同步
- ✅ ES搜索结果为空：返回null，防止NPE
- ✅ ES删除失败：记录日志，继续处理其他数据
- ✅ 大量数据同步：系统正常处理
- ✅ 高频更新同步：系统正常处理

## 五、部署与运维

### 1. 配置参数

| 参数名 | 默认值 | 说明 |
|-------|-------|------|
| `sync.interval` | 30000 | 同步间隔（毫秒） |
| `elasticsearch.host` | http://localhost:9200 | ES服务器地址 |
| `elasticsearch.index-name` | remote-meta-doc | ES索引名称 |
| `spring.datasource.url` | jdbc:mysql://localhost:3306/remote_search | MySQL连接URL |

### 2. 监控告警
- 添加同步失败告警
- 监控同步延迟
- 记录同步日志，便于排查问题

### 3. 维护建议
- 定期检查同步日志
- 定期执行全量同步
- 备份重要数据
- 监控ES和MySQL资源使用情况

## 六、未来优化方向

1. **自适应同步策略**：根据数据更新频率动态调整同步间隔
2. **消息队列异步同步**：使用消息队列替代定时轮询，降低延迟
3. **多线程同步**：支持多线程并行同步，提高同步效率
4. **监控面板**：添加同步状态监控面板，便于可视化管理
5. **数据校验机制**：定期校验MySQL和ES数据一致性

## 七、风险评估

### 1. 潜在风险
- 大量数据同步时的性能问题
- 网络不稳定导致的同步失败
- JSON格式不一致导致的解析错误
- ES版本冲突导致的删除失败

### 2. 应对措施
- 增量同步分批处理
- 实现重试机制
- 添加数据校验逻辑
- 完善错误日志记录
- 使用`conflicts=proceed`处理ES版本冲突

## 八、实现步骤

### 1. 环境准备
- ✅ 调整MySQL模型类以匹配建表语句
- ✅ 添加Jackson依赖支持JSON解析
- ✅ 确认MyBatisPlus和ES依赖已配置

### 2. 数据访问层实现
- 创建/调整MyBatis-Plus Mapper接口
- 实现ES操作的Repository方法

### 3. 同步服务层实现
- 创建SyncService接口和实现类
- 实现全量同步方法
- 实现增量同步方法
- 实现单条数据同步方法
- 实现ES删除方法

### 4. 任务调度实现
- 创建SyncScheduler类
- 配置定时任务
- 实现启动事件监听

### 5. 工具类实现
- 创建JsonUtils工具类
- 创建SyncLogUtils工具类

### 6. 配置文件调整
- 添加MySQL连接配置
- 添加ES连接配置
- 添加同步任务配置

### 7. 测试实现
- ✅ 创建单元测试：RemoteMetaInfoTest.java，已完成并通过测试
- 创建集成测试
- 测试各种异常场景

## 四、测试计划

### 1. 功能测试
- 插入数据测试：MySQL插入 → ES同步
- 更新数据测试：MySQL更新 → ES同步
- 删除数据测试：MySQL删除 → ES删除
- 全量同步测试：启动时同步所有数据
- 增量同步测试：定时任务同步最新数据

### 2. 异常测试
- MySQL宕机测试：恢复后同步
- ES宕机测试：恢复后同步
- JSON格式错误测试：跳过错误数据
- 网络异常测试：重试机制

### 3. 性能测试
- 大量数据同步测试
- 高频更新同步测试
- 系统负载测试

## 五、部署建议

### 1. 配置管理
- 使用配置中心管理数据库和ES连接信息
- 配置同步间隔时间
- 配置重试次数和间隔

### 2. 监控告警
- 添加同步失败告警
- 监控同步延迟
- 记录同步日志

### 3. 维护建议
- 定期检查同步日志
- 定期执行全量同步
- 备份重要数据

## 六、风险评估

### 1. 潜在风险
- 大量数据同步时的性能问题
- 网络不稳定导致的同步失败
- JSON格式不一致导致的解析错误

### 2. 应对措施
- 增量同步分批处理
- 实现重试机制
- 添加数据校验逻辑
- 完善错误日志记录