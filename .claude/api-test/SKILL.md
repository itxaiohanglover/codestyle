# API 测试脚本生成器

快速生成 CodeStyle 项目的 API 测试脚本。

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
  accessKey: 'YOUR_ACCESS_KEY',
  secretKey: 'YOUR_SECRET_KEY'
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
// 服务地址
baseUrl: 'http://localhost:8000'

// 登录配置
username: 'admin'
password: 'admin123'
clientId: 'ef51c9a3e9046c4f2ea45142c8a8344a'

// OpenAPI 配置（从数据库获取）
accessKey: 'YOUR_ACCESS_KEY'
secretKey: 'YOUR_SECRET_KEY'
```

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

---

## 📚 相关文档

- [API 测试指南](../archive/v1.0.0/api-testing/API测试指南.md)
- [OpenAPI 认证机制](../archive/v1.0.0/api-testing/OpenAPI认证机制.md)
- [测试脚本示例](../archive/scripts/testing/)

---

## 🔄 版本历史

- **v1.0.0** (2026-02-21): 初始版本，包含 3 种测试模板

