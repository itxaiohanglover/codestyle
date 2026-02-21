# API 测试脚本生成器

快速生成 CodeStyle 项目的 API 测试脚本。

---

## 🚀 测试前准备（AI 自动化测试）

### 启动后端服务

**推荐方式：使用 IDE 启动**

由于 Maven 命令启动可能存在进程管理问题，强烈建议使用 IDE 启动：

1. 在 IDE 中打开项目
2. 找到启动类：`codestyle-admin/codestyle-server/src/main/java/top/codestyle/admin/CodestyleApplication.java`
3. 右键 → Run 'CodestyleApplication'
4. 等待启动完成（约 30 秒）

**启动成功标志**：

在控制台看到以下日志：

```
--------------------------------------------------------
ContiNew Admin server started successfully.
ContiNew Starter: v2.14.0 (Spring Boot: v3.3.12)
当前版本: v4.1.0 (Profile: dev)
服务地址: http://192.168.148.1:8000
接口文档: http://192.168.148.1:8000/doc.html
--------------------------------------------------------
```

---

### 验证服务状态

**验证服务启动成功**：

查看终端输出，确认以下关键信息：
- ✅ `Started CodestyleApplication in XX seconds`
- ✅ `Tomcat started on port 8000`
- ✅ `服务地址: http://192.168.148.1:8000`

看到以上日志即表示服务启动成功，可以开始测试。

---

### 依赖服务检查

**在启动后端之前，确保以下服务已启动**：

| 服务 | 端口 | 检查命令 | 必需性 |
|------|------|---------|--------|
| **MySQL** | 3306 | `mysql -h127.0.0.1 -P3306 -uroot -p -e "SELECT 1"` | 必需 ✅ |
| **Redis** | 6379 | `redis-cli ping` | 必需 ✅ |
| **Elasticsearch** | 9200 | `curl http://localhost:9200` | 可选 ⚠️ |

**依赖服务未启动的影响**：
- ❌ MySQL 未启动 → 后端无法启动
- ❌ Redis 未启动 → 后端无法启动
- ⚠️ Elasticsearch 未启动 → 搜索功能不可用，其他功能正常

---

## 📋 使用方法

告诉 AI 你要测试的接口信息，AI 会自动生成测试脚本。

**示例提示**：
```
@api-test 生成测试脚本：
- 接口：POST /api/user/create
- 需要登录：是
- 参数：username, email, password
```

---

## 🔧 模板 1：内部 API 测试（需要登录）

```javascript
/**
 * {{API_NAME}} 测试脚本
 * 接口：{{METHOD}} {{ENDPOINT}}
 */

const crypto = require('crypto');
const http = require('http');

// ==================== 配置 ====================
const CONFIG = {
  baseUrl: 'http://localhost:8000',
  publicKey: `-----BEGIN PUBLIC KEY-----
MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAM51dgYtMyF+tTQt80sfFOpSV27a7t9u
aUVeFrdGiVxscuizE7H8SMntYqfn9lp8a5GH5P1/GGehVjUD2gF/4kcCAwEAAQ==
-----END PUBLIC KEY-----`,
  username: 'admin',
  password: 'admin123',
  clientId: 'ef51c9a3e9046c4f2ea45142c8a8344a',
  authType: 'ACCOUNT'
};

// ==================== 工具函数 ====================

// RSA 加密密码
function encryptPassword(password) {
  const buffer = Buffer.from(password, 'utf8');
  const encrypted = crypto.publicEncrypt(
    {
      key: CONFIG.publicKey,
      padding: crypto.constants.RSA_PKCS1_PADDING
    },
    buffer
  );
  return encrypted.toString('base64');
}

// HTTP 请求封装
function request(method, path, data = null, headers = {}) {
  return new Promise((resolve, reject) => {
    const url = new URL(path, CONFIG.baseUrl);
    const options = {
      hostname: url.hostname,
      port: url.port || 8000,
      path: url.pathname,
      method: method,
      headers: {
        'Content-Type': 'application/json',
        ...headers
      }
    };

    const req = http.request(options, (res) => {
      let body = '';
      res.on('data', (chunk) => body += chunk);
      res.on('end', () => {
        try {
          resolve(JSON.parse(body));
        } catch (e) {
          reject(new Error('解析响应失败: ' + body));
        }
      });
    });

    req.on('error', reject);
    
    if (data) {
      req.write(JSON.stringify(data));
    }
    
    req.end();
  });
}

// ==================== 登录 ====================
async function login() {
  console.log('[1/2] 正在登录...');
  
  const encryptedPassword = encryptPassword(CONFIG.password);
  const loginData = {
    username: CONFIG.username,
    password: encryptedPassword,
    clientId: CONFIG.clientId,
    authType: CONFIG.authType
  };

  const response = await request('POST', '/auth/login', loginData);
  
  if (response.success) {
    console.log('✓ 登录成功');
    console.log(`Token: ${response.data.token.substring(0, 30)}...\n`);
    return response.data.token;
  } else {
    throw new Error('登录失败: ' + response.msg);
  }
}

// ==================== 测试接口 ====================
async function testAPI(token) {
  console.log('[2/2] 测试接口...');
  
  // TODO: 修改为实际的请求数据
  const requestData = {
    {{REQUEST_PARAMS}}
  };

  const response = await request('{{METHOD}}', '{{ENDPOINT}}', requestData, {
    'Authorization': `Bearer ${token}`
  });
  
  console.log('\n========== 响应结果 ==========');
  console.log(JSON.stringify(response, null, 2));
  console.log('==============================\n');
  
  if (response.success) {
    console.log('✓ 测试成功');
    return response.data;
  } else {
    throw new Error('测试失败: ' + response.msg);
  }
}

// ==================== 主函数 ====================
async function main() {
  try {
    console.log('\n========================================');
    console.log('  {{API_NAME}} 测试');
    console.log('========================================\n');
    
    const token = await login();
    const result = await testAPI(token);
    
    console.log('========================================');
    console.log('  测试完成');
    console.log('========================================\n');
    
    process.exit(0);
  } catch (error) {
    console.error('\n❌ 测试失败:', error.message);
    process.exit(1);
  }
}

main();
```

**使用方法**：
```bash
node test-{{api-name}}.js
```

---

## 🔐 模板 2：OpenAPI 测试（签名认证）

```javascript
/**
 * {{API_NAME}} OpenAPI 测试脚本
 * 接口：{{METHOD}} {{ENDPOINT}}
 */

const crypto = require('crypto');

// ==================== 配置 ====================
const CONFIG = {
  baseUrl: 'http://localhost:8000',
  accessKey: 'MDYyZDYzZWEwMWQyNDE4MjhhMjUyMT',
  secretKey: 'NzBmNmE4NGZkZDJlNGRhZGE5MjU0OWUzZWQ3MGYzNDc='
};

// ==================== 签名算法 ====================
function generateSign(params, secretKey) {
  // 1. 参数按 key 字典序排序
  const sortedKeys = Object.keys(params).sort();
  
  // 2. 拼接参数字符串
  const paramStr = sortedKeys
    .map(key => `${key}=${params[key]}`)
    .join('&');
  
  // 3. 拼接 secretKey
  const signStr = paramStr + '&key=' + secretKey;
  
  // 4. MD5 加密
  return crypto.createHash('md5').update(signStr).digest('hex');
}

// ==================== 调用 OpenAPI ====================
async function callOpenAPI(businessParams) {
  // 添加认证参数
  const params = {
    ...businessParams,
    accessKey: CONFIG.accessKey,
    timestamp: Date.now().toString(),
    nonce: crypto.randomUUID()
  };
  
  // 生成签名
  params.sign = generateSign(params, CONFIG.secretKey);
  
  // 构建 URL
  const url = `${CONFIG.baseUrl}{{ENDPOINT}}?${new URLSearchParams(params)}`;
  
  console.log('========== 请求信息 ==========');
  console.log('URL:', url);
  console.log('参数:', params);
  console.log('==============================\n');
  
  // 发起请求
  const response = await fetch(url);
  const data = await response.json();
  
  console.log('========== 响应结果 ==========');
  console.log(JSON.stringify(data, null, 2));
  console.log('==============================\n');
  
  return data;
}

// ==================== 主函数 ====================
async function main() {
  try {
    console.log('\n========================================');
    console.log('  {{API_NAME}} OpenAPI 测试');
    console.log('========================================\n');
    
    // TODO: 修改为实际的业务参数
    const result = await callOpenAPI({
      {{REQUEST_PARAMS}}
    });
    
    if (result.success) {
      console.log('✓ 测试成功');
      console.log('结果数量:', result.data?.length || 0);
    } else {
      console.log('✗ 测试失败:', result.msg);
    }
    
    console.log('\n========================================');
    console.log('  测试完成');
    console.log('========================================\n');
    
  } catch (error) {
    console.error('\n❌ 测试失败:', error.message);
  }
}

main();
```

**使用方法**：
```bash
node test-{{api-name}}-openapi.js
```

---

## 🚀 模板 3：简单测试（无需登录）

```javascript
/**
 * {{API_NAME}} 简单测试
 * 接口：{{METHOD}} {{ENDPOINT}}
 */

const http = require('http');

const CONFIG = {
  baseUrl: 'http://localhost:8000',
  endpoint: '{{ENDPOINT}}'
};

async function request(method, path, data = null) {
  return new Promise((resolve, reject) => {
    const url = new URL(path, CONFIG.baseUrl);
    const options = {
      hostname: url.hostname,
      port: url.port || 8000,
      path: url.pathname,
      method: method,
      headers: {
        'Content-Type': 'application/json'
      }
    };

    const req = http.request(options, (res) => {
      let body = '';
      res.on('data', (chunk) => body += chunk);
      res.on('end', () => {
        try {
          resolve(JSON.parse(body));
        } catch (e) {
          reject(new Error('解析失败: ' + body));
        }
      });
    });

    req.on('error', reject);
    
    if (data) {
      req.write(JSON.stringify(data));
    }
    
    req.end();
  });
}

async function main() {
  try {
    console.log('\n测试接口:', CONFIG.endpoint);
    
    // TODO: 修改为实际的请求数据
    const data = {
      {{REQUEST_PARAMS}}
    };
    
    const response = await request('{{METHOD}}', CONFIG.endpoint, data);
    
    console.log('\n响应结果:');
    console.log(JSON.stringify(response, null, 2));
    
    if (response.success) {
      console.log('\n✓ 测试成功');
    } else {
      console.log('\n✗ 测试失败:', response.msg);
    }
    
  } catch (error) {
    console.error('\n❌ 错误:', error.message);
  }
}

main();
```

---

## 📝 PowerShell 版本（Windows 快速测试）

```powershell
# {{API_NAME}} 快速测试
# 接口：{{METHOD}} {{ENDPOINT}}

$baseUrl = "http://localhost:8000"

# 1. 登录（如果需要）
Write-Host "登录中..." -ForegroundColor Yellow
$loginBody = @{
    clientId = "ef51c9a3e9046c4f2ea45142c8a8344a"
    authType = "ACCOUNT"
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

$loginResp = Invoke-RestMethod -Uri "$baseUrl/auth/login" `
    -Method Post `
    -ContentType "application/json" `
    -Body $loginBody

if ($loginResp.success) {
    Write-Host "✓ 登录成功" -ForegroundColor Green
    $token = $loginResp.data.tokenValue
    
    # 2. 测试接口
    Write-Host "`n测试接口..." -ForegroundColor Yellow
    
    # TODO: 修改为实际的请求数据
    $requestBody = @{
        {{REQUEST_PARAMS}}
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$baseUrl{{ENDPOINT}}" `
        -Method {{METHOD}} `
        -ContentType "application/json" `
        -Headers @{"Authorization"=$token} `
        -Body $requestBody
    
    Write-Host "`n响应结果:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 10
    
    if ($response.success) {
        Write-Host "`n✓ 测试成功" -ForegroundColor Green
    } else {
        Write-Host "`n✗ 测试失败: $($response.msg)" -ForegroundColor Red
    }
} else {
    Write-Host "✗ 登录失败: $($loginResp.msg)" -ForegroundColor Red
}
```

---

## 🎯 快速参考

### 常用配置

```javascript
// 服务地址（确保后端已启动）
baseUrl: 'http://localhost:8000'

// 登录配置（默认管理员账号）
username: 'admin'
password: 'admin123'
clientId: 'ef51c9a3e9046c4f2ea45142c8a8344a'  // 固定值，无需修改
authType: 'ACCOUNT'  // 账号密码登录

// OpenAPI 配置（测试用密钥，已预先申请）
accessKey: 'MDYyZDYzZWEwMWQyNDE4MjhhMjUyMT'
secretKey: 'NzBmNmE4NGZkZDJlNGRhZGE5MjU0OWUzZWQ3MGYzNDc='

// 注意：如需使用其他密钥，可从数据库查询
// SQL: SELECT access_key, secret_key FROM sys_open_api WHERE status = 1;
```

### 环境检查清单

测试前请确认：

- ✅ **后端服务已启动**：终端显示 `Started CodestyleApplication` 和 `Tomcat started on port 8000`
- ✅ **MySQL 已启动**：端口 3306 可访问
- ✅ **Redis 已启动**：端口 6379 可访问
- ✅ **Elasticsearch 已启动**：端口 9200 可访问（搜索功能需要）
- ✅ **数据库已初始化**：存在 sys_user 等基础表
- ✅ **默认账号可用**：admin/admin123 可以登录

### 快速启动命令

**推荐：使用 IDE 启动**

```
1. 打开 IDE
2. 找到 CodestyleApplication.java
3. 右键 → Run
4. 等待启动完成（约 30 秒）
5. 验证：查看终端日志，确认显示 "Started CodestyleApplication" 和 "Tomcat started on port 8000"
6. 运行测试：node test-xxx.js
```

**注意**：
- ⚠️ 强烈建议使用 IDE 启动（最稳定）
- ⚠️ Maven 命令启动可能存在进程管理问题
- ⚠️ 启动前确保 MySQL 和 Redis 已启动

### RSA 公钥（固定）

```
-----BEGIN PUBLIC KEY-----
MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAM51dgYtMyF+tTQt80sfFOpSV27a7t9u
aUVeFrdGiVxscuizE7H8SMntYqfn9lp8a5GH5P1/GGehVjUD2gF/4kcCAwEAAQ==
-----END PUBLIC KEY-----
```

### 签名算法（OpenAPI）

```javascript
// 1. 参数排序（字典序）
const sortedKeys = Object.keys(params).sort();

// 2. 拼接字符串
const paramStr = sortedKeys.map(k => `${k}=${params[k]}`).join('&');

// 3. 追加密钥
const signStr = paramStr + '&key=' + secretKey;

// 4. MD5 加密
const sign = crypto.createHash('md5').update(signStr).digest('hex');
```

### 常见请求头

```javascript
// 内部 API
headers: {
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${token}`
}

// OpenAPI（签名在 URL 参数中）
headers: {
  'Content-Type': 'application/json'
}
```

---

## 💡 使用技巧

### 1. 快速生成测试脚本

```
@api-test 生成测试：
- 接口：POST /api/template/create
- 需要登录：是
- 参数：title, content, category
```

### 2. 批量测试多个场景

```javascript
const testCases = [
  { name: '正常创建', data: { title: 'test', content: 'xxx' } },
  { name: '缺少标题', data: { content: 'xxx' } },
  { name: '空内容', data: { title: 'test', content: '' } }
];

for (const testCase of testCases) {
  console.log(`\n测试: ${testCase.name}`);
  await testAPI(token, testCase.data);
}
```

### 3. 添加断言验证

```javascript
if (response.success) {
  // 验证返回数据
  assert(response.data.id, '应该返回 ID');
  assert(response.data.title === requestData.title, '标题应该匹配');
  console.log('✓ 所有断言通过');
}
```

### 4. 调试技巧

**查看详细日志**：
```javascript
// 在请求前添加
console.log('请求数据:', JSON.stringify(requestData, null, 2));

// 在响应后添加
console.log('响应头:', response.headers);
console.log('响应体:', JSON.stringify(response.data, null, 2));
```

**处理登录失败**：
```javascript
// 如果提示需要验证码，检查数据库配置
// SQL: SELECT * FROM sys_option WHERE code = 'LOGIN_CAPTCHA_ENABLED';
// 如果 value = 1，改为 0 关闭验证码
// SQL: UPDATE sys_option SET value = '0' WHERE code = 'LOGIN_CAPTCHA_ENABLED';
```

**处理 Token 过期**：
```javascript
// Token 默认有效期 2 小时
// 如果测试时间较长，需要重新登录获取新 Token
if (response.code === 401) {
  console.log('Token 已过期，重新登录...');
  token = await login();
}
```

---

## ⚠️ 常见问题

### 1. 连接被拒绝 (ECONNREFUSED)

**问题**：`Error: connect ECONNREFUSED 127.0.0.1:8000`

**原因**：后端服务未启动或端口不正确

**解决**：

**使用 IDE 启动（推荐）**
```
1. 打开 IDE
2. 找到 CodestyleApplication.java
3. 右键 → Run 'CodestyleApplication'
4. 等待启动完成（约 30 秒）
5. 验证：查看终端日志，确认显示 "Started CodestyleApplication" 和 "Tomcat started on port 8000"
```

**检查依赖服务**
```bash
# 检查 MySQL
mysql -h127.0.0.1 -P3306 -uroot -p -e "SELECT 1"

# 检查 Redis
redis-cli ping

# 如果依赖服务未启动，先启动它们
```

### 2. 登录失败：需要验证码

**问题**：`{"success":false,"msg":"请输入验证码"}`

**原因**：系统开启了登录验证码

**解决**：
```sql
-- 方式一：关闭验证码（推荐用于测试环境）
UPDATE sys_option SET value = '0' WHERE code = 'LOGIN_CAPTCHA_ENABLED';

-- 方式二：重启应用（配置会从数据库重新加载）
```

### 3. Token 无效或过期

**问题**：`{"success":false,"code":401,"msg":"未授权"}`

**原因**：Token 过期（默认 2 小时）或格式错误

**解决**：
```javascript
// 检查 Token 格式
console.log('Token:', token);  // 应该是长字符串，不包含 "Bearer "

// 重新登录获取新 Token
const token = await login();
```

### 4. 数据库连接失败

**问题**：后端启动失败，提示数据库连接错误

**原因**：MySQL 未启动或配置错误

**解决**：
```bash
# 检查 MySQL 是否启动
mysql -h127.0.0.1 -P3306 -uroot -p

# 检查配置文件
# codestyle-admin/codestyle-server/src/main/resources/application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/codestyle?...
    username: root
    password: your_password
```

### 5. OpenAPI 签名错误

**问题**：`{"success":false,"msg":"签名验证失败"}`

**原因**：accessKey 或 secretKey 错误，或签名算法不正确

**解决**：
```javascript
// 使用测试密钥（已预先申请）
const CONFIG = {
  accessKey: 'MDYyZDYzZWEwMWQyNDE4MjhhMjUyMT',
  secretKey: 'NzBmNmE4NGZkZDJlNGRhZGE5MjU0OWUzZWQ3MGYzNDc='
};

// 如需使用其他密钥，从数据库查询
// SQL: SELECT access_key, secret_key FROM sys_open_api WHERE status = 1;

// 确认签名算法：参数排序 → 拼接字符串 → 追加 secretKey → MD5 加密
```

### 6. Elasticsearch 连接失败

**问题**：搜索接口报错，提示 ES 连接失败

**原因**：Elasticsearch 未启动

**解决**：
```bash
# 检查 ES 是否启动
curl http://localhost:9200

# 如果未启动，启动 ES
# Windows: elasticsearch.bat
# Linux: ./bin/elasticsearch
```

---

## 📚 相关文档

- [API 测试指南](../../archive/v1.0.0/api-testing/API测试指南.md)
- [OpenAPI 认证机制](../../archive/v1.0.0/api-testing/OpenAPI认证机制.md)
- [测试脚本示例](../../archive/scripts/testing/)

---

## 🔄 版本历史

- **v1.0.5** (2026-02-21): 简化为 AI 自动化测试专用，推荐 IDE 启动
- **v1.0.4** (2026-02-21): 重构启动方案，推荐 IDE 启动，提供三种启动方式
- **v1.0.3** (2026-02-21): 添加编译步骤，验证启动命令可行性
- **v1.0.2** (2026-02-21): 优化为面向 AI 的文档，简化启动流程，添加测试密钥
- **v1.0.1** (2026-02-21): 新增后端启动指南、环境检查、常见问题
- **v1.0.0** (2026-02-21): 初始版本，包含 3 种测试模板

