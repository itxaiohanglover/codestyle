-- liquibase formatted sql

-- changeset codestyle:1
-- comment 初始化检索插件数据表
CREATE TABLE IF NOT EXISTS `remote_meta_info`
(
    `id`          bigint       NOT NULL                                                    COMMENT '模板id',
    `meta_json`   json         NOT NULL                                                    COMMENT '模板json信息字段',
    `group_id`    varchar(255) GENERATED ALWAYS AS (meta_json->>'$.groupId') VIRTUAL        COMMENT '分组id由json中的虚拟列提供',
    `create_user` bigint       NOT NULL                                                    COMMENT '创建人',
    `create_time` datetime     NOT NULL                                                    COMMENT '创建时间',
    `update_user` bigint       DEFAULT NULL                                                COMMENT '修改人',
    `update_time` datetime     DEFAULT NULL                                                COMMENT '修改时间',
    `deleted`     bigint       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_deleted` (`deleted`),
    INDEX `idx_group_id` (`group_id`),
    INDEX `idx_update_time` (`update_time`),
    INDEX `idx_create_user` (`create_user`),
    INDEX `idx_update_user` (`update_user`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='模板元数据表';

-- changeset codestyle:2
-- comment 初始化检索插件菜单
INSERT INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `redirect`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`)
VALUES
(5000, '智能检索', 4000, 2, '/template/search', 'TemplateSearch', 'template/list/index', NULL, 'search', b'0', b'0', b'1', NULL, 3, 1, 1, NOW());

-- changeset codestyle:3
-- comment 初始化检索插件示例数据
INSERT INTO `remote_meta_info` (`id`, `meta_json`, `create_user`, `create_time`) VALUES
(1, '{"id":1,"groupId":"DataScience","artifactId":"PyData-Cleaning","description":"Python template for cleaning and transforming data with Pandas and NumPy.","config":{"version":"1.0.0","files":[{"filename":"data_cleaning.py.ftl","sha256":"a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0","filePath":"src/data_cleaning","description":"数据清洗脚本模板","inputVariables":[{"variableType":"String","variableName":"inputFilePath","variableComment":"输入文件路径","example":"data.csv"},{"variableType":"String","variableName":"outputFilePath","variableComment":"输出文件路径","example":"cleaned_data.csv"}]}]}}', 0, NOW()),
(2, '{"id":2,"groupId":"JavaDevelopment","artifactId":"SpringBoot-API","description":"Java Spring Boot RESTful API template with JPA and Swagger documentation.","config":{"version":"2.0.0","files":[{"filename":"Application.java.ftl","sha256":"b1c2d3e4f5g6h7i8j9k0l1m2n3o4p5q6r7s8t9u0","filePath":"src/main/java/com/example","description":"Spring Boot应用主类模板","inputVariables":[{"variableType":"String","variableName":"packageName","variableComment":"包名","example":"com.example.api"}]},{"filename":"RestController.java.ftl","sha256":"c1d2e3f4g5h6i7j9k0l1m2n3o4p5q6r7s8t9u0v1","filePath":"src/main/java/com/example/controller","description":"REST控制器模板","inputVariables":[{"variableType":"String","variableName":"controllerName","variableComment":"控制器名称","example":"UserController"}]}]}}', 0, NOW()),
(3, '{"id":3,"groupId":"FrontendDevelopment","artifactId":"React-App","description":"React.js frontend template with Vite, TypeScript and Tailwind CSS.","config":{"version":"3.0.0","files":[{"filename":"App.tsx.ftl","sha256":"d1e2f3g4h5i6j7k8l9m0n1o2p3q4r5s6t7u8v9w0","filePath":"src","description":"React应用主组件模板","inputVariables":[{"variableType":"String","variableName":"appTitle","variableComment":"应用标题","example":"MyReactApp"}]},{"filename":"main.tsx.ftl","sha256":"e1f2g3h4i5j6k7l8m9n0o1p2q3r4s5t6u7v8w9x0","filePath":"src","description":"React应用入口文件模板","inputVariables":[]}]}}', 0, NOW()),
(4, '{"id":4,"groupId":"DevOps","artifactId":"Docker-Config","description":"Docker configuration templates for various application types.","config":{"version":"1.5.0","files":[{"filename":"Dockerfile.ftl","sha256":"f1g2h3i4j5k6l7m8n9o0p1q2r3s4t5u6v7w8x9y0","filePath":".","description":"Dockerfile模板","inputVariables":[{"variableType":"String","variableName":"baseImage","variableComment":"基础镜像","example":"openjdk:17"},{"variableType":"String","variableName":"appPort","variableComment":"应用端口","example":"8080"}]},{"filename":"docker-compose.yml.ftl","sha256":"g1h2i3j4k5l6m7n8o9p0q1r2s3t4u5v6w8x9y0z1","filePath":".","description":"docker-compose配置模板","inputVariables":[{"variableType":"String","variableName":"serviceName","variableComment":"服务名称","example":"myapp"}]}]}}', 0, NOW()),
(5, '{"id":5,"groupId":"MachineLearning","artifactId":"ML-Classification","description":"Python machine learning classification template with scikit-learn.","config":{"version":"2.1.0","files":[{"filename":"classification.py.ftl","sha256":"h1i2j3k4l5m6n7o8p9q0r1s2t3u4v5w6x7y8z9a0","filePath":"src/ml","description":"机器学习分类脚本模板","inputVariables":[{"variableType":"String","variableName":"dataPath","variableComment":"数据集路径","example":"train.csv"},{"variableType":"String","variableName":"targetColumn","variableComment":"目标列名称","example":"label"}]}]}}', 0, NOW()),
(6, '{"id":6,"groupId":"FrontendDevelopment","artifactId":"Vue3-App","description":"Vue.js 3 frontend template with Vite and TypeScript.","config":{"version":"1.2.0","files":[{"filename":"App.vue.ftl","sha256":"i1j2k3l4m5n6o7p8q9r0s1t2u3v4w5x6y7z8a9b0","filePath":"src","description":"Vue应用主组件模板","inputVariables":[{"variableType":"String","variableName":"appName","variableComment":"应用名称","example":"MyVueApp"}]},{"filename":"main.ts.ftl","sha256":"j1k2l3m4n5o6p7q8r9s0t1u2v3w4x5y6z7a8b9c0","filePath":"src","description":"Vue应用入口文件模板","inputVariables":[]}]}}', 0, NOW()),
(7, '{"id":7,"groupId":"GoDevelopment","artifactId":"Go-Web","description":"Go language web application template with Gin framework.","config":{"version":"1.1.0","files":[{"filename":"main.go.ftl","sha256":"k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0","filePath":".","description":"Go Web应用主程序模板","inputVariables":[{"variableType":"String","variableName":"port","variableComment":"服务端口","example":"8080"}]}]}}', 0, NOW()),
(8, '{"id":8,"groupId":"DataScience","artifactId":"Data-Analysis-Report","description":"Data analysis report template with Jupyter Notebook.","config":{"version":"1.3.0","files":[{"filename":"analysis_report.ipynb.ftl","sha256":"l1m2n3o4p5q6r7s8t9u0v1w2x3y4z5a6b7c8d9e0","filePath":"notebooks","description":"数据分析报告模板","inputVariables":[{"variableType":"String","variableName":"reportTitle","variableComment":"报告标题","example":"销售数据分析报告"}]}]}}', 0, NOW()),
(9, '{"id":9,"groupId":"DevOps","artifactId":"CI-CD-Pipeline","description":"CI/CD pipeline templates for GitHub Actions.","config":{"version":"1.0.0","files":[{"filename":".github/workflows/ci-cd.yml.ftl","sha256":"m1n2o3p4q5r6s7t8u0v1w2x3y4z5a6b7c8d9e0f1","filePath":".","description":"GitHub Actions CI/CD流水线模板","inputVariables":[{"variableType":"String","variableName":"language","variableComment":"项目语言","example":"java"},{"variableType":"String","variableName":"buildCommand","variableComment":"构建命令","example":"mvn clean package"}]}]}}', 0, NOW()),
(10, '{"id":10,"groupId":"QualityAssurance","artifactId":"Py-Automation-Test","description":"Python automation testing template with Selenium.","config":{"version":"1.4.0","files":[{"filename":"test_suite.py.ftl","sha256":"n1o2p3q4r5s6t7u8v9w0x1y2z3a4b5c6d7e8f9g0","filePath":"tests","description":"自动化测试套件模板","inputVariables":[{"variableType":"String","variableName":"baseUrl","variableComment":"测试基础URL","example":"https://example.com"}]}]}}', 0, NOW());
