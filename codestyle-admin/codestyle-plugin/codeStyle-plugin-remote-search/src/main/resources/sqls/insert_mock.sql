-- MySQL插入脚本 for tb_remote_meta_info表
-- 生成时间: 2025-12-04
-- 符合表结构: id BIGINT NOT NULL, meta_json JSON NOT NULL, create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP, update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, deleted TINYINT NOT NULL DEFAULT 0, group_id VARCHAR(255) GENERATED ALWAYS AS (meta_json->>'$.groupId') VIRTUAL
-- 注意：group_id是虚拟列，由数据库自动从meta_json中提取，不需要手动插入
-- 注意：create_time、update_time和deleted字段有默认值，不需要手动插入

-- 插入数据1: Python数据清洗模板
INSERT INTO tb_remote_meta_info (id, meta_json) VALUES 
(1, 
'{"id": 1, "groupId": "DataScience", "artifactId": "PyData-Cleaning", "description": "Python template for cleaning and transforming data with Pandas and NumPy. 支持 CSV 和 JSON 数据的快速清洗与转换。", "config": {"version": "1.0.0", "files": [{"filename": "data_cleaning.py.ftl", "sha256": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0", "filePath": "src/data_cleaning", "description": "数据清洗脚本模板", "inputVariables": [{"variableType": "String", "variableName": "inputFilePath", "variableComment": "输入文件路径", "example": "data.csv"}, {"variableType": "String", "variableName": "outputFilePath", "variableComment": "输出文件路径", "example": "cleaned_data.csv"}]}]}}');

-- 插入数据2: Java Spring Boot API模板
INSERT INTO tb_remote_meta_info (id, meta_json) VALUES 
(2, 
'{"id": 2, "groupId": "JavaDevelopment", "artifactId": "SpringBoot-API", "description": "Java Spring Boot RESTful API template with JPA and Swagger documentation. 快速构建RESTful API服务。", "config": {"version": "2.0.0", "files": [{"filename": "Application.java.ftl", "sha256": "b1c2d3e4f5g6h7i8j9k0l1m2n3o4p5q6r7s8t9u0", "filePath": "src/main/java/com/example", "description": "Spring Boot应用主类模板", "inputVariables": [{"variableType": "String", "variableName": "packageName", "variableComment": "包名", "example": "com.example.api"}]}, {"filename": "RestController.java.ftl", "sha256": "c1d2e3f4g5h6i7j9k0l1m2n3o4p5q6r7s8t9u0v1", "filePath": "src/main/java/com/example/controller", "description": "REST控制器模板", "inputVariables": [{"variableType": "String", "variableName": "controllerName", "variableComment": "控制器名称", "example": "UserController"}]}]}}');

-- 插入数据3: React前端模板
INSERT INTO tb_remote_meta_info (id, meta_json) VALUES 
(3, 
'{"id": 3, "groupId": "FrontendDevelopment", "artifactId": "React-App", "description": "React.js frontend template with Vite, TypeScript and Tailwind CSS. 现代React应用开发模板。", "config": {"version": "3.0.0", "files": [{"filename": "App.tsx.ftl", "sha256": "d1e2f3g4h5i6j7k8l9m0n1o2p3q4r5s6t7u8v9w0", "filePath": "src", "description": "React应用主组件模板", "inputVariables": [{"variableType": "String", "variableName": "appTitle", "variableComment": "应用标题", "example": "MyReactApp"}]}, {"filename": "main.tsx.ftl", "sha256": "e1f2g3h4i5j6k7l8m9n0o1p2q3r4s5t6u7v8w9x0", "filePath": "src", "description": "React应用入口文件模板", "inputVariables": []}]}}');

-- 插入数据4: Docker配置模板
INSERT INTO tb_remote_meta_info (id, meta_json) VALUES 
(4, 
'{"id": 4, "groupId": "DevOps", "artifactId": "Docker-Config", "description": "Docker configuration templates for various application types. 包含Dockerfile和docker-compose.yml模板。", "config": {"version": "1.5.0", "files": [{"filename": "Dockerfile.ftl", "sha256": "f1g2h3i4j5k6l7m8n9o0p1q2r3s4t5u6v7w8x9y0", "filePath": ".", "description": "Dockerfile模板", "inputVariables": [{"variableType": "String", "variableName": "baseImage", "variableComment": "基础镜像", "example": "openjdk:17"}, {"variableType": "String", "variableName": "appPort", "variableComment": "应用端口", "example": "8080"}]}, {"filename": "docker-compose.yml.ftl", "sha256": "g1h2i3j4k5l6m7n8o9p0q1r2s3t4u5v6w8x9y0z1", "filePath": ".", "description": "docker-compose配置模板", "inputVariables": [{"variableType": "String", "variableName": "serviceName", "variableComment": "服务名称", "example": "myapp"}]}]}}');

-- 插入数据5: Python机器学习模板
INSERT INTO tb_remote_meta_info (id, meta_json) VALUES 
(5, 
'{"id": 5, "groupId": "MachineLearning", "artifactId": "ML-Classification", "description": "Python machine learning classification template with scikit-learn. 支持多种分类算法和模型评估。", "config": {"version": "2.1.0", "files": [{"filename": "classification.py.ftl", "sha256": "h1i2j3k4l5m6n7o8p9q0r1s2t3u4v5w6x7y8z9a0", "filePath": "src/ml", "description": "机器学习分类脚本模板", "inputVariables": [{"variableType": "String", "variableName": "dataPath", "variableComment": "数据集路径", "example": "train.csv"}, {"variableType": "String", "variableName": "targetColumn", "variableComment": "目标列名称", "example": "label"}]}]}}');

-- 插入数据6: Vue.js前端模板
INSERT INTO tb_remote_meta_info (id, meta_json) VALUES 
(6, 
'{"id": 6, "groupId": "FrontendDevelopment", "artifactId": "Vue3-App", "description": "Vue.js 3 frontend template with Vite and TypeScript. 现代化Vue应用开发模板。", "config": {"version": "1.2.0", "files": [{"filename": "App.vue.ftl", "sha256": "i1j2k3l4m5n6o7p8q9r0s1t2u3v4w5x6y7z8a9b0", "filePath": "src", "description": "Vue应用主组件模板", "inputVariables": [{"variableType": "String", "variableName": "appName", "variableComment": "应用名称", "example": "MyVueApp"}]}, {"filename": "main.ts.ftl", "sha256": "j1k2l3m4n5o6p7q8r9s0t1u2v3w4x5y6z7a8b9c0", "filePath": "src", "description": "Vue应用入口文件模板", "inputVariables": []}]}}');

-- 插入数据7: Go语言Web模板
INSERT INTO tb_remote_meta_info (id, meta_json) VALUES 
(7, 
'{"id": 7, "groupId": "GoDevelopment", "artifactId": "Go-Web", "description": "Go language web application template with Gin framework. 高性能Go Web应用开发模板。", "config": {"version": "1.1.0", "files": [{"filename": "main.go.ftl", "sha256": "k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0", "filePath": ".", "description": "Go Web应用主程序模板", "inputVariables": [{"variableType": "String", "variableName": "port", "variableComment": "服务端口", "example": "8080"}]}]}}');

-- 插入数据8: 数据分析报告模板
INSERT INTO tb_remote_meta_info (id, meta_json) VALUES 
(8, 
'{"id": 8, "groupId": "DataScience", "artifactId": "Data-Analysis-Report", "description": "Data analysis report template with Jupyter Notebook. 数据科学分析报告模板。", "config": {"version": "1.3.0", "files": [{"filename": "analysis_report.ipynb.ftl", "sha256": "l1m2n3o4p5q6r7s8t9u0v1w2x3y4z5a6b7c8d9e0", "filePath": "notebooks", "description": "数据分析报告模板", "inputVariables": [{"variableType": "String", "variableName": "reportTitle", "variableComment": "报告标题", "example": "销售数据分析报告"}]}]}}');

-- 插入数据9: CI/CD流水线模板
INSERT INTO tb_remote_meta_info (id, meta_json) VALUES 
(9, 
'{"id": 9, "groupId": "DevOps", "artifactId": "CI-CD-Pipeline", "description": "CI/CD pipeline templates for GitHub Actions. 自动化部署流水线模板。", "config": {"version": "1.0.0", "files": [{"filename": ".github/workflows/ci-cd.yml.ftl", "sha256": "m1n2o3p4q5r6s7t8u0v1w2x3y4z5a6b7c8d9e0f1", "filePath": ".", "description": "GitHub Actions CI/CD流水线模板", "inputVariables": [{"variableType": "String", "variableName": "language", "variableComment": "项目语言", "example": "java"}, {"variableType": "String", "variableName": "buildCommand", "variableComment": "构建命令", "example": "mvn clean package"}]}]}}');

-- 插入数据10: Python自动化测试模板
INSERT INTO tb_remote_meta_info (id, meta_json) VALUES 
(10, 
'{"id": 10, "groupId": "QualityAssurance", "artifactId": "Py-Automation-Test", "description": "Python automation testing template with Selenium. 自动化Web测试模板。", "config": {"version": "1.4.0", "files": [{"filename": "test_suite.py.ftl", "sha256": "n1o2p3q4r5s6t7u8v9w0x1y2z3a4b5c6d7e8f9g0", "filePath": "tests", "description": "自动化测试套件模板", "inputVariables": [{"variableType": "String", "variableName": "baseUrl", "variableComment": "测试基础URL", "example": "https://example.com"}]}]}}');