# 代码审查助手

基于 ContiNew Admin 最佳实践的代码审查工具。

---

## 📋 使用方法

告诉 AI 你要审查的代码或文件，AI 会根据 CodeStyle 项目规范进行审查。

**示例提示**：
```
@code-review 审查以下代码：
[粘贴代码]
```

或

```
@code-review 审查文件：src/main/java/top/codestyle/controller/UserController.java
```

---

## 🔍 审查维度

### 1. 代码规范
- ✅ 阿里巴巴 P3C 编码规范
- ✅ 命名规范（驼峰、常量大写等）
- ✅ 注释完整性
- ✅ 代码格式化

### 2. 架构设计
- ✅ 分层架构合理性（Controller → Service → Mapper）
- ✅ 单一职责原则
- ✅ 依赖注入使用
- ✅ 模块化设计

### 3. 安全性
- ✅ SQL 注入防护
- ✅ XSS 防护
- ✅ 权限校验
- ✅ 敏感数据脱敏

### 4. 性能优化
- ✅ N+1 查询问题
- ✅ 缓存使用
- ✅ 批量操作
- ✅ 分页查询

### 5. 业务逻辑
- ✅ 事务管理
- ✅ 异常处理
- ✅ 参数校验
- ✅ 返回值规范

---

## 📝 审查清单

### Controller 层

```java
// ❌ 不推荐
@RestController
public class UserController {
    @Autowired
    private UserService userService;
    
    @GetMapping("/user/list")
    public Object list() {
        return userService.list();
    }
}

// ✅ 推荐
@Tag(name = "用户管理 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "查询用户列表")
    @SaCheckPermission("system:user:list")
    @GetMapping
    public PageResp<UserResp> page(UserQuery query, PageQuery pageQuery) {
        return userService.page(query, pageQuery);
    }
}
```

**检查点**：
- ✅ 使用 `@Tag` 和 `@Operation` 完善 API 文档
- ✅ 使用 `@RequiredArgsConstructor` 代替 `@Autowired`
- ✅ 使用 `@Validated` 开启参数校验
- ✅ 使用 `@SaCheckPermission` 进行权限控制
- ✅ RESTful 风格路径
- ✅ 返回统一响应对象

### Service 层

```java
// ❌ 不推荐
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    public void updateUser(UserReq req) {
        UserDO user = new UserDO();
        user.setId(req.getId());
        user.setNickname(req.getNickname());
        userMapper.updateById(user);
    }
}

// ✅ 推荐
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends BaseServiceImpl<UserMapper, UserDO> 
    implements UserService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserReq req) {
        // 1. 参数校验
        UserDO user = super.getById(req.getId());
        ExceptionUtils.exIfNull(user, "用户不存在");
        
        // 2. 更新用户
        user.setNickname(req.getNickname());
        user.setUpdateTime(LocalDateTime.now());
        super.updateById(user);
        
        // 3. 清除缓存
        cacheService.evict("user:" + user.getId());
        
        log.info("更新用户成功: {}", user.getId());
    }
}
```

**检查点**：
- ✅ 继承 `BaseServiceImpl` 获得通用能力
- ✅ 使用 `@Transactional` 保证事务一致性
- ✅ 使用 `@Slf4j` 记录日志
- ✅ 业务逻辑清晰，步骤明确
- ✅ 异常处理完善
- ✅ 缓存管理

### Mapper 层

```java
// ❌ 不推荐
@Mapper
public interface UserMapper {
    List<UserDO> selectAll();
}

// ✅ 推荐
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
    
    /**
     * 分页查询用户列表
     */
    Page<UserDO> selectPageByQuery(
        @Param("page") Page<UserDO> page,
        @Param("query") UserQuery query
    );
}
```

**检查点**：
- ✅ 继承 `BaseMapper` 获得通用 CRUD
- ✅ 方法命名清晰
- ✅ 添加方法注释
- ✅ 使用 `@Param` 注解

### 实体类

```java
// ❌ 不推荐
@Data
public class UserDO {
    private Long id;
    private String username;
    private String password;
}

// ✅ 推荐
@Data
@TableName("sys_user")
public class UserDO extends BaseDO {
    
    /**
     * 用户名
     */
    @TableField("username")
    private String username;
    
    /**
     * 密码
     */
    @JsonIgnore
    @TableField("password")
    private String password;
    
    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;
}
```

**检查点**：
- ✅ 继承 `BaseDO` 获得通用字段
- ✅ 使用 `@TableName` 指定表名
- ✅ 使用 `@TableField` 指定字段名
- ✅ 敏感字段使用 `@JsonIgnore`
- ✅ 所有字段添加注释

---

## 🎯 常见问题

### 1. SQL 注入风险

```java
// ❌ 危险
@Select("SELECT * FROM user WHERE username = '${username}'")
List<UserDO> selectByUsername(String username);

// ✅ 安全
@Select("SELECT * FROM user WHERE username = #{username}")
List<UserDO> selectByUsername(String username);
```

### 2. N+1 查询问题

```java
// ❌ N+1 查询
List<UserDO> users = userMapper.selectList(null);
for (UserDO user : users) {
    List<RoleDO> roles = roleMapper.selectByUserId(user.getId());
    user.setRoles(roles);
}

// ✅ 批量查询
List<UserDO> users = userMapper.selectList(null);
List<Long> userIds = users.stream().map(UserDO::getId).collect(Collectors.toList());
List<RoleDO> roles = roleMapper.selectByUserIds(userIds);
Map<Long, List<RoleDO>> roleMap = roles.stream()
    .collect(Collectors.groupingBy(RoleDO::getUserId));
users.forEach(user -> user.setRoles(roleMap.get(user.getId())));
```

### 3. 事务失效

```java
// ❌ 事务失效（内部调用）
@Service
public class UserService {
    
    public void updateUser(UserReq req) {
        this.doUpdate(req);  // 事务失效
    }
    
    @Transactional
    public void doUpdate(UserReq req) {
        // ...
    }
}

// ✅ 正确使用
@Service
public class UserService {
    
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserReq req) {
        // 直接在方法上加事务
    }
}
```

### 4. 缓存穿透

```java
// ❌ 缓存穿透
@Cacheable(value = "user", key = "#id")
public UserDO getById(Long id) {
    return userMapper.selectById(id);
}

// ✅ 防止穿透
@Cacheable(value = "user", key = "#id", unless = "#result == null")
public UserDO getById(Long id) {
    return userMapper.selectById(id);
}
```

---

## 📊 审查报告模板

```markdown
## 代码审查报告

### 基本信息
- 文件：UserController.java
- 审查时间：2026-02-21
- 审查人：AI Assistant

### 审查结果

#### ✅ 优点
1. 使用了 RESTful 风格 API
2. 添加了权限校验注解
3. 返回值使用统一响应对象

#### ⚠️ 需要改进
1. 缺少 API 文档注解（@Tag、@Operation）
2. 未使用 @Validated 开启参数校验
3. 依赖注入使用 @Autowired，建议改为构造器注入

#### ❌ 严重问题
1. 存在 SQL 注入风险（使用了 ${} 拼接）
2. 缺少事务管理
3. 敏感数据未脱敏

### 修改建议

[具体的代码修改建议]

### 评分
- 代码规范：7/10
- 架构设计：8/10
- 安全性：5/10
- 性能：7/10
- 总分：6.75/10
```

---

## 🔗 参考规范

### ContiNew Admin 最佳实践

1. **分层架构**
   - Controller：接口层，负责参数校验和权限控制
   - Service：业务层，负责业务逻辑实现
   - Mapper：数据访问层，负责数据库操作

2. **命名规范**
   - Controller：`XxxController`
   - Service：`XxxService` / `XxxServiceImpl`
   - Mapper：`XxxMapper`
   - DO：`XxxDO`（数据库实体）
   - DTO：`XxxReq` / `XxxResp`（请求/响应对象）

3. **注解使用**
   - `@Tag`：API 分组
   - `@Operation`：API 说明
   - `@SaCheckPermission`：权限校验
   - `@Validated`：参数校验
   - `@Transactional`：事务管理

4. **异常处理**
   - 使用 `ExceptionUtils.exIfNull()` 进行参数校验
   - 使用 `ExceptionUtils.exIfCondition()` 进行条件校验
   - 统一异常处理器捕获异常

---

## 💡 使用技巧

### 1. 快速审查单个文件

```
@code-review 审查文件：
src/main/java/top/codestyle/controller/UserController.java
```

### 2. 审查代码片段

```
@code-review 审查以下代码：

[粘贴代码]
```

### 3. 针对性审查

```
@code-review 重点审查安全性：

[粘贴代码]
```

### 4. 批量审查

```
@code-review 审查以下文件：
- UserController.java
- UserService.java
- UserMapper.java
```

---

## 📚 相关文档

- [CodeStyle 最佳实践](../../archive/v1.0.0/best-practices/CodeStyle最佳实践.md)
- [阿里巴巴 Java 开发手册](https://github.com/alibaba/p3c)
- [ContiNew Admin 文档](https://github.com/Charles7c/continew-admin)

---

## 🔄 版本历史

- **v1.0.0** (2026-02-21): 初始版本，基于 ContiNew Admin 最佳实践

