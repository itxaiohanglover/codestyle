# 文档生成助手

基于 CodeStyle 项目规范的文档生成工具。

---

## 📋 使用方法

告诉 AI 你要生成什么类型的文档，AI 会根据项目规范自动生成。

**示例提示**：
```
@doc-write 生成 API 文档：
- 模块：用户管理
- 接口：POST /api/user/create
- 功能：创建新用户
```

或

```
@doc-write 生成 README：
- 项目名称：CodeStyle Search
- 功能：代码模板搜索系统
```

---

## 📝 支持的文档类型

### 1. API 文档
- Swagger/OpenAPI 注解
- 接口说明文档
- 请求/响应示例

### 2. 项目文档
- README.md
- CHANGELOG.md
- CONTRIBUTING.md

### 3. 技术文档
- 架构设计文档
- 数据库设计文档
- 部署文档

### 4. 开发文档
- 开发指南
- 最佳实践
- 故障排查

---

## 🔧 文档模板

### 模板 1：API 接口文档

```java
/**
 * {{模块名称}} API
 */
@Tag(name = "{{模块名称}} API", description = "{{模块描述}}")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/{{模块路径}}")
public class {{模块}}Controller {

    private final {{模块}}Service {{模块小写}}Service;

    /**
     * 查询{{实体}}列表
     *
     * @param query 查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Operation(summary = "查询{{实体}}列表", description = "支持分页和条件查询")
    @Parameters({
        @Parameter(name = "query", description = "查询条件", required = false),
        @Parameter(name = "pageQuery", description = "分页参数", required = true)
    })
    @SaCheckPermission("{{模块}}:{{实体}}:list")
    @GetMapping
    public PageResp<{{实体}}Resp> page(
        @Valid {{实体}}Query query,
        @Valid PageQuery pageQuery
    ) {
        return {{模块小写}}Service.page(query, pageQuery);
    }

    /**
     * 创建{{实体}}
     *
     * @param req 创建请求
     * @return 创建结果
     */
    @Operation(summary = "创建{{实体}}", description = "创建新的{{实体}}记录")
    @SaCheckPermission("{{模块}}:{{实体}}:create")
    @PostMapping
    public Long create(@Valid @RequestBody {{实体}}Req req) {
        return {{模块小写}}Service.create(req);
    }

    /**
     * 更新{{实体}}
     *
     * @param id 主键
     * @param req 更新请求
     */
    @Operation(summary = "更新{{实体}}", description = "根据ID更新{{实体}}信息")
    @SaCheckPermission("{{模块}}:{{实体}}:update")
    @PutMapping("/{id}")
    public void update(
        @PathVariable Long id,
        @Valid @RequestBody {{实体}}Req req
    ) {
        {{模块小写}}Service.update(id, req);
    }

    /**
     * 删除{{实体}}
     *
     * @param ids 主键列表
     */
    @Operation(summary = "删除{{实体}}", description = "根据ID列表批量删除{{实体}}")
    @SaCheckPermission("{{模块}}:{{实体}}:delete")
    @DeleteMapping
    public void delete(@RequestBody List<Long> ids) {
        {{模块小写}}Service.deleteBatch(ids);
    }
}
```

### 模板 2：README.md

```markdown
# {{项目名称}}

{{项目简介}}

## ✨ 特性

- 🚀 **高性能**：{{性能特点}}
- 🔒 **安全可靠**：{{安全特点}}
- 🎨 **现代化 UI**：{{UI特点}}
- 📦 **开箱即用**：{{易用性特点}}

## 🏗️ 技术栈

### 后端
- Spring Boot 3.2
- MyBatis-Plus 3.5
- Sa-Token 1.37
- MySQL 8.0
- Redis 5.0
- Elasticsearch 8.x

### 前端
- Vue 3.5
- TypeScript 5.x
- Arco Design 2.x
- Vite 5.x
- Pinia 2.x

## 📦 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 5.0+
- Elasticsearch 8.x

### 安装步骤

1. **克隆项目**

```bash
git clone {{仓库地址}}
cd {{项目目录}}
```

2. **配置数据库**

```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE {{数据库名}} DEFAULT CHARACTER SET utf8mb4;

# 导入数据
mysql -u root -p {{数据库名}} < sql/init.sql
```

3. **配置文件**

修改 `application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/{{数据库名}}
    username: root
    password: your_password
```

4. **启动后端**

```bash
cd codestyle-admin
mvn clean install
mvn spring-boot:run
```

5. **启动前端**

```bash
cd codestyle-ui
npm install
npm run dev
```

6. **访问系统**

- 前端地址：http://localhost:5173
- 后端地址：http://localhost:8000
- API 文档：http://localhost:8000/doc.html

默认账号：admin / admin123

## 📖 文档

- [开发指南](./docs/开发指南.md)
- [API 文档](./docs/API文档.md)
- [部署文档](./docs/部署文档.md)
- [最佳实践](./docs/最佳实践.md)

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

[MIT License](./LICENSE)

## 👥 团队

- 项目负责人：{{负责人}}
- 技术支持：{{支持邮箱}}
```

### 模板 3：数据库设计文档

```markdown
# {{模块}}数据库设计

## 表结构

### {{表名}}

**表名**：`{{表名}}`  
**说明**：{{表说明}}

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 说明 |
|--------|------|------|--------|--------|------|
| id | bigint | - | NO | - | 主键 |
| {{字段1}} | {{类型}} | {{长度}} | {{是否}} | {{默认}} | {{说明}} |
| {{字段2}} | {{类型}} | {{长度}} | {{是否}} | {{默认}} | {{说明}} |
| create_time | datetime | - | NO | CURRENT_TIMESTAMP | 创建时间 |
| update_time | datetime | - | YES | NULL | 修改时间 |
| deleted | bigint | - | NO | 0 | 逻辑删除 |

**索引**：
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_{{字段}}` (`{{字段}}`, `deleted`)
- INDEX: `idx_{{字段}}` (`{{字段}}`)

**建表语句**：

```sql
CREATE TABLE `{{表名}}` (
  `id` bigint NOT NULL COMMENT '主键',
  `{{字段1}}` {{类型}}({{长度}}) NOT NULL COMMENT '{{说明}}',
  `{{字段2}}` {{类型}}({{长度}}) DEFAULT NULL COMMENT '{{说明}}',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` bigint NOT NULL DEFAULT '0' COMMENT '逻辑删除（0否 ID是）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_{{字段}}` (`{{字段}}`,`deleted`),
  KEY `idx_{{字段}}` (`{{字段}}`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='{{表说明}}';
```

## ER 图

```
{{表1}} 1---N {{表2}}
{{表2}} N---1 {{表3}}
```

## 数据字典

### {{字段名}}

| 值 | 说明 |
|----|------|
| 0 | {{说明}} |
| 1 | {{说明}} |
| 2 | {{说明}} |
```

### 模板 4：部署文档

```markdown
# {{项目名称}} 部署文档

## 环境准备

### 服务器要求

- 操作系统：CentOS 7+ / Ubuntu 20.04+
- CPU：2核+
- 内存：4GB+
- 磁盘：50GB+

### 软件依赖

- JDK 17
- MySQL 8.0
- Redis 5.0
- Nginx 1.20
- Elasticsearch 8.x

## 部署步骤

### 1. 安装 JDK

```bash
# 下载 JDK
wget https://download.oracle.com/java/17/latest/jdk-17_linux-x64_bin.tar.gz

# 解压
tar -zxvf jdk-17_linux-x64_bin.tar.gz -C /usr/local/

# 配置环境变量
echo 'export JAVA_HOME=/usr/local/jdk-17' >> /etc/profile
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /etc/profile
source /etc/profile

# 验证
java -version
```

### 2. 安装 MySQL

```bash
# 安装
yum install mysql-server

# 启动
systemctl start mysqld
systemctl enable mysqld

# 初始化
mysql_secure_installation
```

### 3. 部署后端

```bash
# 上传 jar 包
scp target/{{项目名}}.jar root@server:/opt/app/

# 创建启动脚本
cat > /opt/app/start.sh << 'EOF'
#!/bin/bash
nohup java -jar {{项目名}}.jar \
  --spring.profiles.active=prod \
  > app.log 2>&1 &
echo $! > app.pid
EOF

chmod +x /opt/app/start.sh

# 启动
cd /opt/app
./start.sh
```

### 4. 部署前端

```bash
# 构建
npm run build

# 上传
scp -r dist/* root@server:/usr/share/nginx/html/

# 配置 Nginx
cat > /etc/nginx/conf.d/{{项目名}}.conf << 'EOF'
server {
    listen 80;
    server_name {{域名}};
    
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
EOF

# 重启 Nginx
nginx -t
systemctl reload nginx
```

## 监控与维护

### 日志查看

```bash
# 应用日志
tail -f /opt/app/app.log

# Nginx 日志
tail -f /var/log/nginx/access.log
```

### 健康检查

```bash
# 检查服务状态
curl http://localhost:8000/actuator/health

# 检查进程
ps aux | grep java
```

### 备份策略

```bash
# 数据库备份
mysqldump -u root -p {{数据库名}} > backup_$(date +%Y%m%d).sql

# 定时备份（每天凌晨2点）
crontab -e
0 2 * * * /usr/bin/mysqldump -u root -p{{密码}} {{数据库名}} > /backup/db_$(date +\%Y\%m\%d).sql
```
```

---

## 🎯 文档规范

### 1. Markdown 格式

- 使用标准 Markdown 语法
- 代码块指定语言
- 表格对齐
- 链接有效

### 2. 文档结构

```
# 一级标题（文档标题）
## 二级标题（章节）
### 三级标题（小节）
#### 四级标题（细节）
```

### 3. 代码示例

- 提供完整可运行的代码
- 添加必要的注释
- 使用占位符标记需要修改的部分

### 4. 图表使用

- 架构图使用 Mermaid
- 流程图使用 Mermaid
- ER 图使用文本描述

---

## 💡 使用技巧

### 1. 生成 API 文档

```
@doc-write 生成 API 文档：
- 模块：用户管理
- 功能：CRUD 操作
- 权限：system:user
```

### 2. 生成 README

```
@doc-write 生成 README：
- 项目：CodeStyle Search
- 技术栈：Spring Boot + Vue 3
- 特性：RAG 搜索、多租户
```

### 3. 生成数据库文档

```
@doc-write 生成数据库文档：
- 表名：sys_user
- 字段：id, username, nickname, password
```

### 4. 生成部署文档

```
@doc-write 生成部署文档：
- 环境：生产环境
- 服务器：CentOS 7
- 组件：MySQL, Redis, Nginx
```

---

## 📚 参考资源

### ContiNew Admin 文档规范

1. **API 文档**
   - 使用 Swagger/OpenAPI 注解
   - 完整的请求/响应示例
   - 错误码说明

2. **项目文档**
   - README 包含快速开始
   - CHANGELOG 记录版本变更
   - CONTRIBUTING 说明贡献流程

3. **技术文档**
   - 架构图清晰
   - 技术选型有理由
   - 部署步骤详细

---

## 🔗 相关文档

- [CodeStyle 最佳实践](../../archive/v1.0.0/best-practices/CodeStyle最佳实践.md)
- [Markdown 语法指南](https://www.markdownguide.org/)
- [Mermaid 图表语法](https://mermaid.js.org/)

---

## 🔄 版本历史

- **v1.0.0** (2026-02-21): 初始版本，支持 4 种文档类型

