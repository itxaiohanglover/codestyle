# CodeStyle Wiki 插件 - API 文档

> REST API 接口文档
> 
> **版本**: 1.0.0  
> **文档日期**: 2026-01-29

---

## 📋 目录

1. [项目管理 API](#1-项目管理-api)
2. [教程管理 API](#2-教程管理-api)
3. [生成任务 API](#3-生成任务-api)
4. [章节管理 API](#4-章节管理-api)

---

## 1. 项目管理 API

### 1.1 查询项目列表

**接口**: `GET /api/wiki/project`

**权限**: `wiki:project:list`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 否 | 项目名称（模糊查询） |
| sourceType | Integer | 否 | 代码源类型（1:GitHub 2:本地） |
| status | Integer | 否 | 状态（1:启用 2:禁用） |
| page | Integer | 否 | 页码（默认1） |
| size | Integer | 否 | 每页数量（默认10） |

**响应示例**:

```json
{
  "code": "0",
  "msg": "操作成功",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "FastAPI",
        "description": "现代化的 Web API 框架",
        "sourceType": 1,
        "sourceUrl": "https://github.com/tiangolo/fastapi",
        "language": "zh-CN",
        "status": 1,
        "createTime": "2026-01-29 10:00:00"
      }
    ],
    "total": 1
  },
  "success": true
}
```

### 1.2 新增项目

**接口**: `POST /api/wiki/project`

**权限**: `wiki:project:create`

**请求体**:

```json
{
  "name": "FastAPI",
  "description": "现代化的 Web API 框架",
  "sourceType": 1,
  "sourceUrl": "https://github.com/tiangolo/fastapi",
  "language": "zh-CN"
}
```

**响应示例**:

```json
{
  "code": "0",
  "msg": "操作成功",
  "data": 1,
  "success": true
}
```

### 1.3 修改项目

**接口**: `PUT /api/wiki/project/{id}`

**权限**: `wiki:project:update`

**请求体**:

```json
{
  "name": "FastAPI",
  "description": "更新后的描述",
  "language": "en-US"
}
```

### 1.4 删除项目

**接口**: `DELETE /api/wiki/project`

**权限**: `wiki:project:delete`

**请求体**:

```json
{
  "ids": [1, 2, 3]
}
```

---

## 2. 教程管理 API

### 2.1 查询教程列表

**接口**: `GET /api/wiki/tutorial`

**权限**: `wiki:tutorial:list`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| projectId | Long | 否 | 项目ID |
| status | Integer | 否 | 状态（1:草稿 2:已发布） |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |

**响应示例**:

```json
{
  "code": "0",
  "msg": "操作成功",
  "data": {
    "list": [
      {
        "id": 1,
        "projectId": 1,
        "projectName": "FastAPI",
        "version": "v1.0.0",
        "summary": "FastAPI 是一个现代化的 Web 框架...",
        "status": 2,
        "publishTime": "2026-01-29 12:00:00",
        "createTime": "2026-01-29 10:00:00"
      }
    ],
    "total": 1
  },
  "success": true
}
```

### 2.2 查询教程详情

**接口**: `GET /api/wiki/tutorial/{id}`

**权限**: `wiki:tutorial:query`

**响应示例**:

```json
{
  "code": "0",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "projectId": 1,
    "projectName": "FastAPI",
    "version": "v1.0.0",
    "summary": "FastAPI 是一个现代化的 Web 框架...",
    "mermaidDiagram": "flowchart TD\n  A[FastAPI] --> B[Pydantic]",
    "status": 2,
    "chapters": [
      {
        "id": 1,
        "title": "FastAPI 应用",
        "chapterOrder": 1,
        "content": "# FastAPI 应用\n\n..."
      }
    ],
    "abstractions": [
      {
        "id": 1,
        "name": "FastAPI",
        "description": "核心应用类"
      }
    ]
  },
  "success": true
}
```

### 2.3 发布教程

**接口**: `POST /api/wiki/tutorial/{id}/publish`

**权限**: `wiki:tutorial:publish`

**响应示例**:

```json
{
  "code": "0",
  "msg": "发布成功",
  "success": true
}
```

### 2.4 导出教程

**接口**: `GET /api/wiki/tutorial/{id}/export`

**权限**: `wiki:tutorial:export`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| format | String | 否 | 导出格式（markdown, pdf, html）默认 markdown |

**响应**: 文件下载

---

## 3. 生成任务 API

### 3.1 创建生成任务

**接口**: `POST /api/wiki/generate`

**权限**: `wiki:generate:create`

**请求体**:

```json
{
  "projectId": 1,
  "maxAbstractions": 10,
  "maxFileSize": 100000,
  "includePatterns": ["*.py", "*.java"],
  "excludePatterns": ["*/tests/*", "*/test/*"]
}
```

**响应示例**:

```json
{
  "code": "0",
  "msg": "任务创建成功",
  "data": 1,
  "success": true
}
```

### 3.2 查询任务状态

**接口**: `GET /api/wiki/generate/{taskId}`

**权限**: `wiki:generate:query`

**响应示例**:

```json
{
  "code": "0",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "projectId": 1,
    "status": 2,
    "progress": 65,
    "currentStep": "生成章节内容",
    "createTime": "2026-01-29 10:00:00",
    "updateTime": "2026-01-29 10:05:00"
  },
  "success": true
}
```

**状态说明**:

| 状态值 | 说明 |
|--------|------|
| 1 | 待执行 |
| 2 | 执行中 |
| 3 | 成功 |
| 4 | 失败 |

### 3.3 取消任务

**接口**: `POST /api/wiki/generate/{taskId}/cancel`

**权限**: `wiki:generate:cancel`

**响应示例**:

```json
{
  "code": "0",
  "msg": "任务已取消",
  "success": true
}
```

### 3.4 查询任务列表

**接口**: `GET /api/wiki/generate`

**权限**: `wiki:generate:list`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| projectId | Long | 否 | 项目ID |
| status | Integer | 否 | 状态 |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |

---

## 4. 章节管理 API

### 4.1 查询章节列表

**接口**: `GET /api/wiki/chapter`

**权限**: `wiki:chapter:list`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| tutorialId | Long | 是 | 教程ID |

**响应示例**:

```json
{
  "code": "0",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "tutorialId": 1,
      "title": "FastAPI 应用",
      "chapterOrder": 1,
      "createTime": "2026-01-29 10:00:00"
    }
  ],
  "success": true
}
```

### 4.2 查询章节详情

**接口**: `GET /api/wiki/chapter/{id}`

**权限**: `wiki:chapter:query`

**响应示例**:

```json
{
  "code": "0",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "tutorialId": 1,
    "abstractionId": 1,
    "title": "FastAPI 应用",
    "chapterOrder": 1,
    "content": "# FastAPI 应用\n\n...",
    "createTime": "2026-01-29 10:00:00"
  },
  "success": true
}
```

### 4.3 修改章节

**接口**: `PUT /api/wiki/chapter/{id}`

**权限**: `wiki:chapter:update`

**请求体**:

```json
{
  "title": "FastAPI 应用（更新）",
  "content": "# FastAPI 应用\n\n更新后的内容..."
}
```

---

## 📝 通用说明

### 统一响应格式

```json
{
  "code": "0",
  "msg": "操作成功",
  "data": {},
  "success": true,
  "timestamp": 1706518400000
}
```

### 错误码

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 400 | 参数错误 |
| 401 | 未授权 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

### 分页参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | Integer | 1 | 页码 |
| size | Integer | 10 | 每页数量 |

### 分页响应

```json
{
  "list": [],
  "total": 100,
  "page": 1,
  "size": 10
}
```

---

**文档维护**: CodeStyle Team  
**最后更新**: 2026-01-29

