# MCP 集成技能

快速生成 MCP Server 与 Open API 集成相关的代码和配置。

---

## 📋 技能说明

本技能提供 MCP (Model Context Protocol) Server 与 CodeStyle Open API 集成的可复用代码模板和最佳实践。

**适用场景**：
- ✅ 实现 Open API 签名认证
- ✅ 处理 ContiNew 响应格式
- ✅ 安全处理 JSONNull 类型
- ✅ 实现动态文件打包下载
- ✅ 正确使用 Hutool 工具类
- ✅ 处理 MCP 协议通知与请求

---

## 🚀 使用方法

### 1. 生成签名认证代码

```
@mcp-integration 生成 ContiNew Open API 签名认证代码
```

### 2. 生成响应处理代码

```
@mcp-integration 生成 ContiNew 响应格式兼容处理代码
```

### 3. 生成文件下载接口

```
@mcp-integration 生成动态 ZIP 打包下载接口
```

### 4. 生成 MCP 测试脚本

```
@mcp-integration 生成 MCP 端到端测试脚本
```

---

## 📦 代码模板

### 1. ContiNew Open API 签名认证

```java
/**
 * 生成 ContiNew Open API 签名
 * 
 * 签名算法：
 * 1. 将所有参数（除 sign）按 key 字典序排序
 * 2. 添加 key=secretKey 参数
 * 3. 拼接成 key1=value1&key2=value2 格式
 * 4. MD5 加密（32位小写）
 */
private static String generateSignature(Map<String, String> params, String secretKey) {
    // 1. 添加 key 参数（TreeMap 自动排序）
    Map<String, String> allParams = new TreeMap<>(params);
    allParams.put("key", secretKey);
    
    // 2. 字典序排序并拼接
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (Map.Entry<String, String> entry : allParams.entrySet()) {
        if (!"sign".equals(entry.getKey())) {
            if (!first) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
    }
    
    // 3. MD5 加密（32位小写）
    return DigestUtil.md5Hex(sb.toString());
}

// 使用示例
long timestamp = System.currentTimeMillis();
String nonce = UUID.randomUUID().toString().replace("-", "");

Map<String, String> params = new TreeMap<>();
params.put("query", "CRUD");
params.put("timestamp", String.valueOf(timestamp));
params.put("nonce", nonce);
params.put("accessKey", accessKey);

String sign = generateSignature(params, secretKey);
params.put("sign", sign);
```

---

### 2. ContiNew 响应格式兼容处理

```java
// 解析响应
Map<String, Object> result = JSONUtil.toBean(body, Map.class);

String code = String.valueOf(result.get("code"));
Boolean success = (Boolean) result.get("success");

// 优先判断 success 字段，兼容 code="0" 和 code="200"
if ((success != null && !success) || (!"0".equals(code) && !"200".equals(code))) {
    throw new RemoteSearchException(
        ErrorCode.SERVER_ERROR,
        "检索失败: " + result.get("msg")
    );
}

// 获取数据
List<Map<String, Object>> dataList = (List<Map<String, Object>>) result.get("data");
```

---

### 3. JSONNull 类型安全处理

```java
/**
 * 安全获取字符串值，处理 null 和 JSONNull
 */
private static String getStringValue(Map<String, Object> map, String key) {
    Object value = map.get(key);
    
    // 处理 null
    if (value == null || "null".equals(String.valueOf(value))) {
        return null;
    }
    
    // 处理 JSONNull 类型
    if (value.getClass().getSimpleName().equals("JSONNull")) {
        return null;
    }
    
    return String.valueOf(value);
}

// 使用示例
result.setTitle(getStringValue(data, "title"));
result.setSnippet(getStringValue(data, "snippet"));
result.setContent(getStringValue(data, "content"));
```

---

### 4. 动态 ZIP 打包下载接口

```java
@Tag(name = "模板文件管理")
@RestController
@RequestMapping("/open-api/template")
public class TemplateFileController {
    
    @Operation(summary = "下载模板")
    @GetMapping("/download")
    public void download(
        @Schema(description = "组织ID") @RequestParam String groupId,
        @Schema(description = "项目ID") @RequestParam String artifactId,
        @Schema(description = "版本号") @RequestParam String version,
        HttpServletResponse response
    ) throws IOException {
        
        // 1. 验证模板存在性
        String templateDir = templateBasePath + File.separator + 
                            groupId + File.separator + 
                            artifactId + File.separator + version;
        CheckUtils.throwIf(!new File(templateDir).exists(), 
            "模板不存在: {}/{}/{}", groupId, artifactId, version);
        
        // 2. 创建临时 ZIP 文件
        File zipFile = FileUtil.createTempFile("template-", ".zip", true);
        
        try {
            // 3. 打包模板
            File versionDir = new File(templateDir);
            String metaJsonPath = templateBasePath + File.separator + 
                                 groupId + File.separator + 
                                 artifactId + File.separator + "meta.json";
            
            ZipUtil.zip(zipFile, false, versionDir, new File(metaJsonPath));
            
            // 4. 设置响应头
            String filename = groupId + "-" + artifactId + "-" + version + ".zip";
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", 
                "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
            response.setContentLengthLong(zipFile.length());
            
            // 5. 流式传输
            IoUtil.copy(FileUtil.getInputStream(zipFile), 
                       response.getOutputStream());
            
        } finally {
            // 6. 清理临时文件
            if (zipFile.exists()) {
                FileUtil.del(zipFile);
            }
        }
    }
}
```

---

### 5. Hutool 工具类正确使用

```java
// CheckUtils 参数验证
CheckUtils.throwIf(!file.exists(), "文件不存在: {}", filePath);
CheckUtils.throwIf(StrUtil.isBlank(param), "参数不能为空");
CheckUtils.throwIf(list.isEmpty(), "列表不能为空");

// ZipUtil 压缩文件
ZipUtil.zip(
    zipFile,           // 目标 ZIP 文件
    false,             // 是否包含源目录名
    sourceFile1,       // 源文件1
    sourceFile2        // 源文件2（可变参数）
);

// FileUtil 文件操作
File tempFile = FileUtil.createTempFile("prefix-", ".zip", true);
String content = FileUtil.readUtf8String(file);
FileUtil.writeUtf8String(content, file);
FileUtil.del(file);
FileUtil.mkdir(dir);
```

---

### 6. MCP 协议通知与请求处理

```javascript
// 通知：没有 id 字段，不等待响应
const notification = {
    jsonrpc: '2.0',
    method: 'notifications/initialized',
    params: {}
};
mcpServer.stdin.write(JSON.stringify(notification) + '\n');

// 请求：有 id 字段，需要等待响应
const request = {
    jsonrpc: '2.0',
    id: ++requestId,
    method: 'tools/list',
    params: {}
};
mcpServer.stdin.write(JSON.stringify(request) + '\n');

// 测试步骤配置
const testSteps = [
    {
        name: '发送 initialized 通知',
        request: {
            method: 'notifications/initialized',
            params: {}
        },
        isNotification: true,  // 标记为通知
        validate: () => true
    },
    {
        name: '列出可用工具',
        request: {
            method: 'tools/list',
            params: {}
        },
        isNotification: false,  // 标记为请求
        validate: (response) => response.result?.tools?.length > 0
    }
];

// 执行步骤
if (step.isNotification) {
    // 通知：不等待响应
    mcpServer.stdin.write(JSON.stringify(request) + '\n');
    executeNextStep();
} else {
    // 请求：等待响应
    mcpServer.stdin.write(JSON.stringify(request) + '\n');
}
```

---

## 🔧 常见问题与解决方案

### 问题 1：响应码判断错误

**现象**：`检索失败: code=0`

**解决方案**：
```java
// 优先判断 success 字段，兼容两种格式
Boolean success = (Boolean) result.get("success");
if ((success != null && !success) || (!"0".equals(code) && !"200".equals(code))) {
    throw new Exception("检索失败");
}
```

---

### 问题 2：JSONNull 类型转换异常

**现象**：`ClassCastException: cn.hutool.json.JSONNull cannot be cast to java.lang.String`

**解决方案**：使用 `getStringValue()` 方法安全处理

---

### 问题 3：CheckUtils 方法不存在

**现象**：`Cannot resolve method 'throwIfNotExists'`

**解决方案**：
```java
// 使用 throwIf + 条件判断
CheckUtils.throwIf(!file.exists(), "文件不存在: {}", filePath);
```

---

### 问题 4：ZipUtil 参数错误

**现象**：`The method zip(File, boolean, File...) is not applicable`

**解决方案**：
```java
// 正确的方法签名
ZipUtil.zip(zipFile, false, sourceFile1, sourceFile2);
```

---

### 问题 5：MCP 通知卡住

**现象**：测试脚本在发送 `initialized` 通知后卡住

**解决方案**：区分通知和请求，通知不等待响应

---

## 📚 最佳实践

### 1. 接口设计
- ✅ 使用 `/open-api/` 前缀统一接口地址
- ✅ 使用 `@Tag` 和 `@Operation` 注解描述接口
- ✅ 使用 `@Schema` 描述参数和返回值
- ✅ 使用 `CheckUtils` 进行参数验证

### 2. 错误处理
- ✅ 设计完善的异常体系（ErrorCode 枚举）
- ✅ 提供清晰的错误信息
- ✅ 记录详细的日志（INFO、DEBUG、ERROR）
- ✅ 区分不同的错误类型

### 3. 性能优化
- ✅ 实现二级缓存（L1 本地 + L2 Redis）
- ✅ 使用异步检索（CompletableFuture）
- ✅ 使用流式传输避免内存溢出
- ✅ 使用临时文件避免并发冲突

### 4. 安全认证
- ✅ 使用 MD5 签名防篡改
- ✅ 使用时间戳防重放攻击
- ✅ 使用随机数防签名重用
- ✅ 使用 HTTPS 传输敏感数据

---

## 🔗 相关文档

- [MCP-OpenAPI集成实战总结.md](../../archive/v1.0.0/mcp-integration/MCP-OpenAPI集成实战总结.md)
- [项目开发总结.md](../../项目开发总结.md)
- [测试验证报告.md](../../测试验证报告.md)

---

## 📊 性能基准

| 指标 | 目标 | 实际 | 评价 |
|------|------|------|------|
| 接口成功率 | ≥99% | 100% | ⭐⭐⭐⭐⭐ |
| 搜索响应 | ≤500ms | 276ms | ⭐⭐⭐⭐⭐ |
| 缓存响应 | ≤10ms | 2ms | ⭐⭐⭐⭐⭐ |
| 下载响应 | ≤100ms | 30ms | ⭐⭐⭐⭐⭐ |

---

**版本**: v1.0.0  
**最后更新**: 2026-02-22  
**维护者**: CodeStyle Team

