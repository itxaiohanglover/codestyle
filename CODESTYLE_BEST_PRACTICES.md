# CodeStyle 项目开发最佳实践

> 基于 ContiNew Admin 框架的企业级中后台管理系统开发规范与最佳实践
> 
> **项目版本**: 4.1.0  
> **文档日期**: 2026-01-29  
> **技术栈**: Spring Boot 3.2 + Vue 3 + TypeScript

---

## 📋 目录

1. [项目架构设计](#1-项目架构设计)
2. [后端开发规范](#2-后端开发规范)
3. [前端开发规范](#3-前端开发规范)
4. [数据库设计规范](#4-数据库设计规范)
5. [安全最佳实践](#5-安全最佳实践)
6. [性能优化实践](#6-性能优化实践)
7. [部署运维规范](#7-部署运维规范)

---

## 1. 项目架构设计

### 1.1 模块化分层架构

```
codestyle-admin/
├── codestyle-server/          # 启动模块（Controller层）
├── codestyle-system/          # 系统管理模块（业务实现）
├── codestyle-common/          # 公共模块（工具类、配置）
├── codestyle-plugin/          # 插件模块（可插拔功能）
│   ├── codestyle-plugin-generator/   # 代码生成器
│   ├── codestyle-plugin-search/      # 搜索服务（ES+Canal+Kafka）
│   ├── codestyle-plugin-tenant/      # 多租户
│   └── codestyle-plugin-schedule/    # 任务调度
└── codestyle-extension/       # 扩展模块
```

**最佳实践**：
- ✅ **单一职责**：每个模块职责明确，server 只负责接口暴露
- ✅ **依赖倒置**：common 模块定义接口，system 模块实现业务
- ✅ **插件化设计**：功能模块可独立部署，支持按需加载

### 1.2 分层架构设计

```
Controller (接口层)
    ↓
Service (业务层)
    ↓
Mapper (数据访问层)
    ↓
Database (数据库)
```

**核心原则**：
- Controller 只做参数校验和响应封装
- Service 实现业务逻辑，可调用多个 Mapper
- Mapper 只做数据库操作，不包含业务逻辑

---

## 2. 后端开发规范

### 2.1 代码风格规范

#### 使用 P3C 阿里巴巴编码规范

```xml
<!-- pom.xml 配置 Spotless 代码格式化插件 -->
<plugin>
    <groupId>com.diffplug.spotless</groupId>
    <artifactId>spotless-maven-plugin</artifactId>
    <configuration>
        <java>
            <removeUnusedImports/>
            <eclipse>
                <file>.style/p3c-codestyle.xml</file>
            </eclipse>
            <licenseHeader>
                <file>.style/license-header</file>
            </licenseHeader>
        </java>
    </configuration>
</plugin>
```

**关键配置**：
- 缩进：4 空格
- 大括号：end_of_line（K&R 风格）
- 自动移除未使用的导入
- 统一的 License Header

#### Lombok 配置规范

```properties
# lombok.config
config.stopBubbling=true
lombok.toString.callSuper=CALL
lombok.equalsAndHashCode.callSuper=CALL
lombok.val.flagUsage=ERROR
lombok.accessors.flagUsage=ERROR
```

**最佳实践**：
- ✅ 强制 `toString` 和 `equalsAndHashCode` 调用父类方法
- ✅ 禁用 `val` 和 `@Accessors`（提高代码可读性）

### 2.2 Controller 层规范

```java
@Tag(name = "个人信息 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/profile")
public class UserProfileController {

    private final UserService userService;

    @Operation(summary = "修改头像", description = "用户修改个人头像")
    @PatchMapping("/avatar")
    public AvatarResp updateAvatar(
        @NotNull(message = "头像不能为空") MultipartFile avatarFile
    ) throws IOException {
        ValidationUtils.throwIf(avatarFile::isEmpty, "头像不能为空");
        String newAvatar = userService.updateAvatar(
            avatarFile, 
            UserContextHolder.getUserId()
        );
        return AvatarResp.builder().avatar(newAvatar).build();
    }
}
```

**最佳实践**：
- ✅ 使用 `@Tag` 和 `@Operation` 完善 API 文档
- ✅ 使用 `@Validated` 开启参数校验
- ✅ 使用 `@RequiredArgsConstructor` 自动注入依赖
- ✅ RESTful 风格：GET 查询、POST 新增、PUT/PATCH 修改、DELETE 删除
- ✅ 从 `UserContextHolder` 获取当前用户信息，避免参数传递

### 2.3 Service 层规范

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl 
    extends BaseServiceImpl<UserMapper, UserDO, UserResp, UserDetailResp, UserQuery, UserReq>
    implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateAvatar(MultipartFile avatarFile, Long userId) {
        // 1. 上传文件
        FileInfo fileInfo = fileStorageService.of(avatarFile).upload();
        
        // 2. 更新用户头像
        UserDO user = super.getById(userId);
        user.setAvatar(fileInfo.getUrl());
        super.updateById(user);
        
        return fileInfo.getUrl();
    }
}
```

**最佳实践**：
- ✅ 继承 `BaseServiceImpl` 获得通用 CRUD 能力
- ✅ 使用 `@Transactional` 保证事务一致性
- ✅ 业务逻辑清晰，步骤明确
- ✅ 使用 `@Slf4j` 记录关键操作日志

### 2.4 参数校验规范

```java
@Data
@Schema(description = "用户基础信息修改请求")
public class UserBasicInfoUpdateReq {

    @Schema(description = "昵称", example = "张三")
    @NotBlank(message = "昵称不能为空")
    @Size(max = 30, message = "昵称长度不能超过 {max} 个字符")
    private String nickname;

    @Schema(description = "性别", example = "1")
    @NotNull(message = "性别不能为空")
    private Integer gender;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;
}
```

**最佳实践**：
- ✅ 使用 JSR-303 注解进行参数校验
- ✅ 自定义错误提示信息
- ✅ 使用 `@Schema` 完善接口文档

### 2.5 异常处理规范

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return R.fail(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return R.fail(message);
    }
}
```

**最佳实践**：
- ✅ 统一异常处理，避免重复代码
- ✅ 区分业务异常和系统异常
- ✅ 参数校验异常返回友好提示

### 2.6 缓存使用规范

```java
@Service
public class DictServiceImpl implements DictService {

    @Override
    @Cacheable(value = CacheConstants.DICT_KEY, key = "#dictCode")
    public List<DictItemResp> listByCode(String dictCode) {
        // 查询数据库
        return dictItemMapper.selectByDictCode(dictCode);
    }

    @Override
    @CacheInvalidate(value = CacheConstants.DICT_KEY, key = "#dictCode")
    public void update(String dictCode, DictReq req) {
        // 更新数据库
        dictMapper.updateById(dict);
    }
}
```

**最佳实践**：
- ✅ 使用 JetCache 注解简化缓存操作
- ✅ 统一缓存 Key 前缀管理
- ✅ 更新数据时及时清除缓存

---

## 3. 前端开发规范

### 3.1 项目结构规范

```
src/
├── apis/              # API 接口定义
├── assets/            # 静态资源
├── components/        # 公共组件
├── directives/        # 自定义指令
├── hooks/             # 组合式函数
├── layout/            # 布局组件
├── router/            # 路由配置
├── stores/            # 状态管理
├── styles/            # 全局样式
├── types/             # TypeScript 类型定义
├── utils/             # 工具函数
└── views/             # 页面组件
```

### 3.2 代码风格规范

```javascript
// eslint.config.js
export default antfu({
  vue: {
    overrides: {
      'vue/block-order': ['error', {
        order: [['script', 'template'], 'style'],
      }],
      'vue/define-macros-order': ['error', {
        order: ['defineOptions', 'defineModel', 'defineProps', 'defineEmits'],
        defineExposeLast: true,
      }],
    },
  },
})
```

**最佳实践**：
- ✅ 使用 @antfu/eslint-config 统一代码风格
- ✅ Vue 组件顺序：script → template → style
- ✅ 宏定义顺序：defineOptions → defineProps → defineEmits → defineExpose

### 3.3 API 请求规范

```typescript
// src/apis/system/user.ts
export function getUserList(params: UserQuery) {
  return http.get<PageResp<UserResp>>('/system/user', params)
}

export function addUser(data: UserReq) {
  return http.post('/system/user', data)
}

export function updateUser(data: UserReq, id: number) {
  return http.put(`/system/user/${id}`, data)
}
```

**最佳实践**：
- ✅ 统一使用 `http` 工具类
- ✅ 使用 TypeScript 类型定义
- ✅ RESTful 风格 API

### 3.4 状态管理规范

```typescript
// src/stores/modules/user.ts
export const useUserStore = defineStore('user', () => {
  const userInfo = reactive<UserInfo>({})
  const token = ref(getToken() || '')

  const login = async (req: LoginReq) => {
    const res = await loginApi(req)
    setToken(res.data.token)
    token.value = res.data.token
  }

  const logout = async () => {
    await logoutApi()
    clearToken()
    resetRouter()
  }

  return { userInfo, token, login, logout }
}, {
  persist: { 
    paths: ['token'], 
    storage: localStorage 
  },
})
```

**最佳实践**：
- ✅ 使用 Pinia 进行状态管理
- ✅ 使用 Setup Store 语法（更灵活）
- ✅ 使用 `pinia-plugin-persistedstate` 持久化关键数据

### 3.5 路由守卫规范

```typescript
// src/router/guard.ts
router.beforeEach(async (to, from, next) => {
  NProgress.start()
  
  if (getToken()) {
    if (!hasRouteFlag) {
      await userStore.getInfo()
      const accessRoutes = await routeStore.generateRoutes()
      accessRoutes.forEach(route => router.addRoute(route))
      hasRouteFlag = true
      next({ ...to, replace: true })
    } else {
      next()
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    }
  }
})
```

**最佳实践**：
- ✅ 使用 NProgress 显示加载进度
- ✅ 动态路由按需加载
- ✅ 白名单机制
- ✅ 登录重定向保留原路径

---

## 4. 数据库设计规范

### 4.1 表设计规范

```sql
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL COMMENT 'ID',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `nickname` varchar(30) NOT NULL COMMENT '昵称',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(11) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1启用 2禁用）',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` bigint NOT NULL DEFAULT '0' COMMENT '是否删除（0否 ID是）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`,`deleted`)
) ENGINE=InnoDB COMMENT='用户表';
```

**最佳实践**：
- ✅ 使用 `bigint` 作为主键类型
- ✅ 必备字段：create_time、update_time、deleted
- ✅ 逻辑删除使用 ID 值（解决唯一索引冲突）
- ✅ 所有字段添加 COMMENT 注释

### 4.2 索引设计规范

```sql
-- 唯一索引（包含逻辑删除字段）
UNIQUE KEY `uk_username` (`username`, `deleted`)

-- 普通索引
KEY `idx_dept_id` (`dept_id`)
KEY `idx_create_time` (`create_time`)

-- 联合索引（遵循最左前缀原则）
KEY `idx_dept_status` (`dept_id`, `status`)
```

**最佳实践**：
- ✅ 唯一索引包含 `deleted` 字段
- ✅ 外键字段建立索引
- ✅ 常用查询条件建立索引
- ✅ 联合索引遵循最左前缀原则

---

## 5. 安全最佳实践

### 5.1 认证授权

```yaml
# application.yml
sa-token:
  token-name: Authorization
  timeout: 86400              # Token 有效期 24 小时
  active-timeout: 1800        # 活跃超时 30 分钟
  is-concurrent: true         # 允许多端登录
  is-share: false             # 不共享 Token
  jwt-secret-key: your-secret-key
```

**最佳实践**：
- ✅ 使用 Sa-Token 进行认证授权
- ✅ Token 有效期和活跃超时分离
- ✅ 支持多端登录但不共享 Token
- ✅ 使用 JWT 增强安全性

### 5.2 密码加密

```java
// 使用 RSA 加密传输密码
String encryptedPassword = SecureUtils.encryptByRsaPublicKey(password);

// 后端解密
String password = SecureUtils.decryptByRsaPrivateKey(encryptedPassword);

// 使用 BCrypt 存储密码
String hashedPassword = passwordEncoder.encode(password);
```

**最佳实践**：
- ✅ 前端使用 RSA 公钥加密密码
- ✅ 后端使用 RSA 私钥解密
- ✅ 数据库使用 BCrypt 存储密码哈希

### 5.3 数据脱敏

```java
@EncryptField(type = EncryptType.PHONE)
private String phone;

@EncryptField(type = EncryptType.EMAIL)
private String email;
```

**最佳实践**：
- ✅ 使用 `@EncryptField` 注解自动脱敏
- ✅ 敏感数据加密存储
- ✅ 日志中不输出敏感信息

---

## 6. 性能优化实践

### 6.1 分页查询优化

```java
@Override
public PageResp<UserResp> page(UserQuery query, PageQuery pageQuery) {
    // 使用 MyBatis-Plus 分页插件
    Page<UserDO> page = baseMapper.selectPageByQuery(
        pageQuery.toPage(), 
        query
    );
    return PageResp.build(page, UserResp.class);
}
```

**最佳实践**：
- ✅ 使用 MyBatis-Plus 分页插件
- ✅ 避免深分页（limit 10000, 10）
- ✅ 大数据量使用游标分页

### 6.2 批量操作优化

```java
// 批量插入
saveBatch(userList, 1000);  // 每批 1000 条

// 批量更新
updateBatchById(userList, 1000);
```

**最佳实践**：
- ✅ 使用 MyBatis-Plus 批量操作方法
- ✅ 控制批次大小（建议 500-1000）
- ✅ 大数据量使用异步处理

### 6.3 缓存策略

```java
// 热点数据缓存
@Cacheable(value = "dict", key = "#code", expire = 3600)
public List<DictItem> listByCode(String code) {
    return dictItemMapper.selectByCode(code);
}

// 缓存预热
@PostConstruct
public void init() {
    // 应用启动时加载热点数据
    loadHotData();
}
```

**最佳实践**：
- ✅ 热点数据使用 Redis 缓存
- ✅ 设置合理的过期时间
- ✅ 应用启动时预热缓存

---

## 7. 部署运维规范

### 7.1 Docker 部署

```yaml
# docker-compose.yml
version: '3'
services:
  mysql:
    image: mysql:8.0.42
    environment:
      MYSQL_ROOT_PASSWORD: your_password
      MYSQL_DATABASE: continew_admin
    volumes:
      - /docker/mysql/data:/var/lib/mysql
    
  redis:
    image: redis:7.2.8
    command: redis-server --appendonly yes
    
  app:
    build: .
    ports:
      - "18000:18000"
    depends_on:
      - mysql
      - redis
```

**最佳实践**：
- ✅ 使用 Docker Compose 编排服务
- ✅ 数据持久化到宿主机
- ✅ 使用 depends_on 控制启动顺序

### 7.2 日志管理

```xml
<!-- logback-spring.xml -->
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/continew-admin.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>logs/continew-admin.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
        <maxHistory>30</maxHistory>
    </rollingPolicy>
</appender>
```

**最佳实践**：
- ✅ 按日期滚动日志文件
- ✅ 保留 30 天日志
- ✅ 区分不同级别日志

---

## 📚 总结

### 核心原则

1. **约定优于配置**：遵循框架约定，减少配置
2. **单一职责**：每个类、方法只做一件事
3. **开闭原则**：对扩展开放，对修改关闭
4. **依赖倒置**：依赖抽象而非具体实现
5. **安全第一**：所有输入都不可信，所有输出都需脱敏

### 关键技术选型

| 层级 | 技术 | 说明 |
|------|------|------|
| 后端框架 | Spring Boot 3.2 | 企业级应用框架 |
| ORM | MyBatis-Plus | 增强版 MyBatis |
| 认证授权 | Sa-Token | 轻量级权限框架 |
| 缓存 | Redis + JetCache | 分布式缓存 |
| 前端框架 | Vue 3 + TypeScript | 渐进式框架 |
| UI 组件 | Arco Design | 字节跳动企业级组件库 |
| 状态管理 | Pinia | Vue 官方推荐 |
| 构建工具 | Vite | 下一代前端构建工具 |

---

**文档维护**: 开发团队  
**最后更新**: 2026-01-29

