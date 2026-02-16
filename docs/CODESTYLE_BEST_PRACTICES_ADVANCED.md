# CodeStyle 项目开发最佳实践（进阶篇）

> 深入探讨 CodeStyle 项目的高级开发实践与架构设计
> 
> **项目版本**: 4.1.0  
> **文档日期**: 2026-01-29

---

## 📋 目录

1. [多租户架构实践](#1-多租户架构实践)
2. [权限控制体系](#2-权限控制体系)
3. [数据同步方案](#3-数据同步方案)
4. [前端组件化开发](#4-前端组件化开发)
5. [异常处理体系](#5-异常处理体系)
6. [API 设计规范](#6-api-设计规范)
7. [测试与质量保证](#7-测试与质量保证)

---

## 1. 多租户架构实践

### 1.1 租户隔离策略

**配置示例**：

```yaml
# application.yml
continew-starter.tenant:
  enabled: true
  # 隔离级别：LINE（行级）
  isolation-level: LINE
  # 请求头中租户标识
  tenant-id-header: X-Tenant-Id
  tenant-code-header: X-Tenant-Code
  # 默认租户 ID（超级管理员）
  default-tenant-id: 0
  # 忽略表（不拼接租户条件）
  ignore-tables:
    - tenant                    # 租户表
    - tenant_package            # 租户套餐表
    - sys_menu                  # 菜单表
    - sys_dict                  # 字典表
    - remote_meta_info          # 远程元信息表
  # 忽略菜单 ID（租户不能使用的菜单）
  ignore-menus:
    - 1050  # 菜单管理
    - 1130  # 字典管理
    - 3000  # 租户管理
```

### 1.2 租户上下文管理

**拦截器实现**：

```java
@Slf4j
public class SaExtensionInterceptor extends SaInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        boolean flag = super.preHandle(request, response, handler);
        if (!flag || !StpUtil.isLogin()) {
            return flag;
        }
        
        // 设置用户上下文
        UserContext userContext = UserContextHolder.getContext();
        
        // 检查用户租户权限
        if (TenantContextHolder.isTenantEnabled()) {
            Long userTenantId = userContext.getTenantId();
            Long tenantId = TenantContextHolder.getTenantId();
            if (!userTenantId.equals(tenantId)) {
                R r = R.fail("您当前没有访问该租户的权限");
                ServletUtils.writeJSON(response, JSONUtils.toJsonStr(r));
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception e) throws Exception {
        try {
            super.afterCompletion(request, response, handler, e);
        } finally {
            // 清除上下文
            UserContextHolder.clearContext();
        }
    }
}
```

**最佳实践**：
- ✅ 使用 ThreadLocal 存储租户上下文
- ✅ 请求结束后及时清理上下文，避免内存泄漏
- ✅ 在拦截器中统一校验租户权限
- ✅ 超级管理员租户 ID 为 0，不受租户隔离限制

### 1.3 前端租户处理

```typescript
// src/utils/http.ts
http.interceptors.request.use((config: AxiosRequestConfig) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  
  // 添加租户 ID 到请求头
  const tenantStore = useTenantStore()
  if (tenantStore.tenantEnabled && tenantStore.tenantId) {
    config.headers['X-Tenant-Id'] = tenantStore.tenantId
  }
  
  return config
})
```

**最佳实践**：
- ✅ 租户 ID 通过请求头传递
- ✅ 使用 Pinia Store 管理租户状态
- ✅ 租户切换时清空缓存数据

---

## 2. 权限控制体系

### 2.1 后端权限注解

```java
@Tag(name = "用户管理 API")
@RestController
@RequestMapping("/system/user")
public class UserController {

    @Operation(summary = "查询用户列表")
    @SaCheckPermission("system:user:list")
    @GetMapping
    public PageResp<UserResp> page(UserQuery query, PageQuery pageQuery) {
        return userService.page(query, pageQuery);
    }

    @Operation(summary = "新增用户")
    @SaCheckPermission("system:user:create")
    @PostMapping
    public void add(@Valid @RequestBody UserReq req) {
        userService.add(req);
    }

    @Operation(summary = "删除用户")
    @SaCheckPermission("system:user:delete")
    @DeleteMapping
    public void delete(@RequestBody List<Long> ids) {
        userService.delete(ids);
    }
}
```

### 2.2 前端权限指令

```vue
<template>
  <a-button v-permission="['system:user:create']" type="primary" @click="onAdd">
    <template #icon><icon-plus /></template>
    新增
  </a-button>
  
  <a-button v-permission="['system:user:export']" @click="onExport">
    <template #icon><icon-download /></template>
    导出
  </a-button>
  
  <a-link v-permission="['system:user:update']" @click="onUpdate(record)">
    修改
  </a-link>
</template>

<script setup lang="ts">
// 使用组合式函数检查权限
import { has } from '@/utils/has'

const hasDeletePermission = has.hasPerm('system:user:delete')
</script>
```

**权限指令实现**：

```typescript
// src/directives/permission/index.ts
export default {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const permissions = useUserStore().permissions
    
    if (value && value instanceof Array && value.length > 0) {
      const hasPermission = permissions.some((permission: string) => {
        return value.includes(permission)
      })
      
      if (!hasPermission) {
        el.style.display = 'none'
      }
    }
  }
}
```

**最佳实践**：
- ✅ 后端使用 `@SaCheckPermission` 注解控制接口权限
- ✅ 前端使用 `v-permission` 指令控制按钮显示
- ✅ 权限标识采用 `模块:功能:操作` 格式
- ✅ 前后端权限标识保持一致

---

## 3. 数据同步方案

### 3.1 Canal + Kafka + Elasticsearch 架构

**技术选型**：
- **Canal Server**: 监听 MySQL binlog，捕获数据变更
- **Kafka**: 消息队列，解耦 Canal 和应用
- **Elasticsearch**: 搜索引擎，提供全文检索能力

**数据流转流程**：

```
MySQL Binlog 变更
    ↓
Canal Server 解析
    ↓
Kafka 消息队列
    ↓
Spring Boot 消费者
    ↓
Elasticsearch 同步
```

### 3.2 消息消费者实现

```java
@Slf4j
@Component
public class CanalKafkaMessageConsumer {

    @Autowired
    private EsBulkSyncService esBulkSyncService;
    
    @Autowired
    private MessageIdempotencyService idempotencyService;

    @KafkaListener(
        topics = "data-change",
        groupId = "codestyle-search",
        containerFactory = "canalKafkaListenerContainerFactory"
    )
    public void consumeBatchCanalMessages(
        List<ConsumerRecord<String, String>> records,
        Acknowledgment acknowledgment
    ) {
        log.info("Kafka监听器被调用，收到 {} 条消息", records.size());
        
        List<DataChangeMessage> messages = new ArrayList<>();
        
        for (ConsumerRecord<String, String> record : records) {
            try {
                // 1. 解析 Canal JSON 消息
                CanalMessage canalMessage = JSONUtils.parseObject(
                    record.value(), 
                    CanalMessage.class
                );
                
                // 2. 转换为业务消息
                DataChangeMessage message = converter.convert(canalMessage);
                
                // 3. 幂等性检查
                if (idempotencyService.isProcessed(message.getMessageId())) {
                    log.debug("消息已处理，跳过: {}", message.getMessageId());
                    continue;
                }
                
                messages.add(message);
                
                // 4. 标记消息已处理
                idempotencyService.markProcessed(message.getMessageId());
                
            } catch (Exception e) {
                log.error("消息处理失败: {}", record.value(), e);
            }
        }
        
        // 5. 批量同步到 ES
        if (!messages.isEmpty()) {
            esBulkSyncService.syncBatch(messages);
        }
        
        // 6. 手动提交 offset
        acknowledgment.acknowledge();
    }
}
```

**最佳实践**：
- ✅ 批量消费消息（提高吞吐量）
- ✅ 使用 Redis 实现消息幂等性
- ✅ 手动提交 offset（确保消息处理成功）
- ✅ 异常消息记录到死信队列

### 3.3 全量同步实现

```java
@Component
public class FullSyncRunner implements ApplicationRunner {

    @Autowired
    private SyncService syncService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始执行全量数据同步...");
        syncService.fullSync();
        log.info("全量数据同步完成");
    }
}
```

**最佳实践**：
- ✅ 应用启动时自动执行全量同步
- ✅ 先删除 ES 中的旧数据，再批量写入
- ✅ 使用 Bulk API 提高写入性能

---

## 4. 前端组件化开发

### 4.1 通用表格组件 (GiTable)

**组件特性**：
- 支持分页、排序、筛选
- 支持列设置（显示/隐藏、拖拽排序）
- 支持全屏显示
- 支持表格尺寸切换
- 支持自定义工具栏

**使用示例**：

```vue
<template>
  <GiTable
    row-key="id"
    :data="dataList"
    :columns="columns"
    :loading="loading"
    :pagination="pagination"
    :disabled-column-keys="['nickname']"
    @refresh="search"
  >
    <template #top>
      <GiForm 
        v-model="queryForm" 
        search 
        :columns="queryFormColumns" 
        @search="search" 
        @reset="reset"
      />
    </template>
    
    <template #toolbar-left>
      <a-button v-permission="['system:user:create']" type="primary" @click="onAdd">
        <template #icon><icon-plus /></template>
        新增
      </a-button>
    </template>
    
    <template #nickname="{ record }">
      <GiCellAvatar :avatar="record.avatar" :name="record.nickname" />
    </template>
    
    <template #status="{ record }">
      <GiCellStatus :status="record.status" />
    </template>
    
    <template #action="{ record }">
      <a-space>
        <a-link v-permission="['system:user:update']" @click="onUpdate(record)">
          修改
        </a-link>
        <a-link v-permission="['system:user:delete']" @click="onDelete(record)">
          删除
        </a-link>
      </a-space>
    </template>
  </GiTable>
</template>
```

**最佳实践**：
- ✅ 使用插槽自定义表格内容
- ✅ 使用 `GiCell*` 系列组件统一单元格样式
- ✅ 工具栏左侧放操作按钮，右侧放导出、刷新等功能
- ✅ 使用 `v-permission` 控制按钮权限

### 4.2 通用表单组件 (GiForm)

**组件特性**：
- 支持搜索表单和编辑表单
- 支持表单折叠/展开
- 支持自定义表单项
- 支持表单验证

**使用示例**：

```vue
<template>
  <GiForm
    v-model="formData"
    :columns="formColumns"
    :rules="formRules"
    @search="handleSearch"
    @reset="handleReset"
  />
</template>

<script setup lang="ts">
const formColumns = [
  {
    field: 'username',
    label: '用户名',
    type: 'input',
    props: {
      placeholder: '请输入用户名'
    }
  },
  {
    field: 'status',
    label: '状态',
    type: 'select',
    props: {
      options: [
        { label: '启用', value: 1 },
        { label: '禁用', value: 2 }
      ]
    }
  },
  {
    field: 'dateRange',
    label: '创建时间',
    type: 'range-picker'
  }
]
</script>
```

**最佳实践**：
- ✅ 使用配置化方式定义表单项
- ✅ 搜索表单使用 `inline` 布局
- ✅ 编辑表单使用 `horizontal` 布局
- ✅ 使用 `rules` 属性定义验证规则

---

## 5. 异常处理体系

### 5.1 全局异常处理器

```java
@Slf4j
@Order(99)
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public R handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        return R.fail(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), 
                     e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    public R handleBindException(BindException e, HttpServletRequest request) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        String errorMsg = e.getFieldErrors()
            .stream()
            .findFirst()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .orElse(StringConstants.EMPTY);
        return R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), errorMsg);
    }

    /**
     * 文件上传异常
     */
    @ExceptionHandler(MultipartException.class)
    public R handleMultipartException(MultipartException e, HttpServletRequest request) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        String msg = e.getMessage();
        
        // 解析文件大小限制
        String sizeLimit = parseSizeLimit(msg);
        return R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), 
                     "请上传小于 %s 的文件".formatted(FileUtil.readableFileSize(
                         Long.parseLong(sizeLimit))));
    }

    /**
     * 请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R handleHttpRequestMethodNotSupportedException(
        HttpRequestMethodNotSupportedException e,
        HttpServletRequest request
    ) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        return R.fail(String.valueOf(HttpStatus.METHOD_NOT_ALLOWED.value()), 
                     "请求方式 '%s' 不支持".formatted(e.getMethod()));
    }
}
```

**异常分类**：

| 异常类型 | HTTP 状态码 | 说明 |
|---------|-----------|------|
| `BusinessException` | 500 | 业务异常 |
| `BadRequestException` | 400 | 参数错误 |
| `BindException` | 400 | 参数校验失败 |
| `MultipartException` | 400 | 文件上传异常 |
| `NoHandlerFoundException` | 404 | 接口不存在 |
| `HttpRequestMethodNotSupportedException` | 405 | 请求方法不支持 |

**最佳实践**：
- ✅ 统一异常处理，避免重复代码
- ✅ 区分业务异常和系统异常
- ✅ 返回友好的错误提示
- ✅ 记录详细的错误日志

### 5.2 前端异常处理

```typescript
// src/utils/http.ts
http.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response
    const { success, code, msg } = data

    // 处理文件下载
    if (response.request.responseType === 'blob') {
      const contentType = data.type
      if (contentType.startsWith('application/json')) {
        // 下载失败，返回 JSON 错误信息
        const reader = new FileReader()
        reader.readAsText(data)
        reader.onload = () => {
          const { msg } = JSON.parse(reader.result as string)
          handleError(msg)
        }
        return Promise.reject(msg)
      }
      return response
    }

    if (success) {
      return response
    }

    // Token 失效
    if (code === '401') {
      modalErrorWrapper({
        title: '提示',
        content: msg,
        okText: '重新登录',
        async onOk() {
          const userStore = useUserStore()
          await userStore.logoutCallBack()
          await router.replace(`/login?redirect=${encodeURIComponent(currentPath)}`)
        },
      })
    } else {
      handleError(msg)
    }
    
    return Promise.reject(new Error(msg))
  },
  (error: AxiosError) => {
    if (!error.response) {
      handleError('网络连接失败，请检查您的网络')
      return Promise.reject(error)
    }
    
    const status = error.response?.status
    const errorMsg = StatusCodeMessage[status] || '服务器暂时未响应'
    handleError(errorMsg)
    return Promise.reject(error)
  }
)
```

**最佳实践**：
- ✅ 401 错误弹窗提示，引导用户重新登录
- ✅ 其他错误使用 Message 或 Notification 提示
- ✅ 网络错误单独处理
- ✅ 文件下载错误特殊处理

---

## 6. API 设计规范

### 6.1 RESTful API 设计

**资源命名**：
- 使用名词复数形式：`/users`、`/roles`、`/depts`
- 使用小写字母和连字符：`/user-profiles`

**HTTP 方法**：

| 方法 | 说明 | 示例 |
|------|------|------|
| GET | 查询资源 | `GET /users` - 查询用户列表<br>`GET /users/1` - 查询用户详情 |
| POST | 创建资源 | `POST /users` - 创建用户 |
| PUT | 完整更新资源 | `PUT /users/1` - 更新用户（全部字段） |
| PATCH | 部分更新资源 | `PATCH /users/1/avatar` - 更新用户头像 |
| DELETE | 删除资源 | `DELETE /users` - 批量删除用户 |

**API 示例**：

```typescript
// src/apis/system/user.ts
const BASE_URL = '/system/user'

/** 查询用户列表 */
export function listUser(query: UserPageQuery) {
  return http.get<PageRes<UserResp[]>>(`${BASE_URL}`, query)
}

/** 查询用户详情 */
export function getUser(id: string) {
  return http.get<UserDetailResp>(`${BASE_URL}/${id}`)
}

/** 新增用户 */
export function addUser(data: UserReq) {
  return http.post(`${BASE_URL}`, data)
}

/** 修改用户 */
export function updateUser(data: UserReq, id: string) {
  return http.put(`${BASE_URL}/${id}`, data)
}

/** 删除用户 */
export function deleteUser(id: string) {
  return http.del(`${BASE_URL}`, { ids: [id] })
}

/** 导出用户 */
export function exportUser(query: UserQuery) {
  return http.download(`${BASE_URL}/export`, query)
}
```

### 6.2 统一响应格式

```java
@Data
public class R<T> {
    /** 响应码 */
    private String code;
    
    /** 响应消息 */
    private String msg;
    
    /** 响应数据 */
    private T data;
    
    /** 是否成功 */
    private Boolean success;
    
    /** 时间戳 */
    private Long timestamp;
}
```

**成功响应**：

```json
{
  "code": "0",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "username": "admin"
  },
  "success": true,
  "timestamp": 1706518400000
}
```

**失败响应**：

```json
{
  "code": "400",
  "msg": "用户名不能为空",
  "data": null,
  "success": false,
  "timestamp": 1706518400000
}
```

**最佳实践**：
- ✅ 使用统一的响应格式
- ✅ `success` 字段表示业务是否成功
- ✅ `code` 字段表示业务状态码
- ✅ `msg` 字段返回友好的提示信息

---

## 7. 测试与质量保证

### 7.1 代码质量工具

**Maven 插件配置**：

```xml
<!-- Spotless 代码格式化 -->
<plugin>
    <groupId>com.diffplug.spotless</groupId>
    <artifactId>spotless-maven-plugin</artifactId>
    <executions>
        <execution>
            <phase>compile</phase>
            <goals>
                <goal>apply</goal>
            </goals>
        </execution>
    </executions>
</plugin>

<!-- SonarQube 代码质量分析 -->
<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
</plugin>
```

**前端 ESLint 配置**：

```javascript
// eslint.config.js
export default antfu({
  vue: {
    overrides: {
      'vue/block-order': ['error', {
        order: [['script', 'template'], 'style'],
      }],
    },
  },
  rules: {
    'no-console': 'error',  // 禁止使用 console
    'curly': ['off', 'all'],
  },
})
```

### 7.2 单元测试规范

```java
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testAddUser() {
        UserReq req = new UserReq();
        req.setUsername("test");
        req.setNickname("测试用户");
        
        assertDoesNotThrow(() -> userService.add(req));
    }

    @Test
    void testUpdateUser() {
        UserReq req = new UserReq();
        req.setNickname("新昵称");
        
        assertDoesNotThrow(() -> userService.update(req, 1L));
    }
}
```

**最佳实践**：
- ✅ 编译时自动格式化代码
- ✅ 使用 SonarQube 进行代码质量分析
- ✅ 关键业务逻辑编写单元测试
- ✅ 测试覆盖率达到 70% 以上

---

## 📚 总结

### 核心架构特点

1. **多租户架构**：行级隔离，支持 SaaS 模式
2. **权限控制**：前后端双重校验，细粒度权限控制
3. **数据同步**：Canal + Kafka + ES，实时数据同步
4. **组件化开发**：通用组件封装，提高开发效率
5. **异常处理**：统一异常处理，友好错误提示
6. **API 规范**：RESTful 风格，统一响应格式
7. **质量保证**：代码格式化、质量分析、单元测试

### 技术亮点

| 特性 | 技术方案 | 优势 |
|------|---------|------|
| 多租户 | 行级隔离 + 租户上下文 | 数据安全、灵活配置 |
| 权限控制 | Sa-Token + 自定义指令 | 细粒度控制、易于扩展 |
| 数据同步 | Canal + Kafka + ES | 实时同步、高性能 |
| 组件化 | GiTable + GiForm | 统一风格、快速开发 |
| 异常处理 | 全局异常处理器 | 统一处理、友好提示 |

---

**文档维护**: 开发团队  
**最后更新**: 2026-01-29

