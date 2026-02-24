-- liquibase formatted sql

-- changeset codestyle:1
-- comment 初始化模板插件数据表
CREATE TABLE IF NOT EXISTS `template` (
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `group_id`       varchar(100) DEFAULT NULL            COMMENT 'Maven groupId',
    `artifact_id`    varchar(100) DEFAULT NULL            COMMENT 'Maven artifactId',
    `name`           varchar(200) NOT NULL                COMMENT '模板名称',
    `icon`           varchar(10)  NOT NULL                COMMENT '图标文字缩写',
    `author`         varchar(100) NOT NULL                COMMENT '作者',
    `description`    text         NOT NULL                COMMENT '模板描述',
    `version`        varchar(20)  DEFAULT 'v1.0.0'       COMMENT '版本号',
    `download_count` int          DEFAULT 0               COMMENT '下载次数',
    `rating`         decimal(2,1) DEFAULT 0.0             COMMENT '评分',
    `download_url`   varchar(500) DEFAULT NULL            COMMENT '下载地址',
    `search_ref_id`  varchar(100) DEFAULT NULL            COMMENT 'search模块模板ID',
    `create_user`    bigint       NOT NULL                COMMENT '创建人',
    `create_time`    datetime     NOT NULL                COMMENT '创建时间',
    `update_user`    bigint       DEFAULT NULL            COMMENT '修改人',
    `update_time`    datetime     DEFAULT NULL            COMMENT '修改时间',
    `deleted`        bigint       NOT NULL DEFAULT 0      COMMENT '是否已删除（0：否；id：是）',
    PRIMARY KEY (`id`),
    INDEX `idx_group_artifact` (`group_id`, `artifact_id`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码模板表';

CREATE TABLE IF NOT EXISTS `template_tag` (
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `template_id` bigint      NOT NULL                COMMENT '模板ID',
    `label`       varchar(50) NOT NULL                COMMENT '标签文字',
    `color`       varchar(20) NOT NULL                COMMENT '标签颜色',
    PRIMARY KEY (`id`),
    INDEX `idx_template_id` (`template_id`),
    FOREIGN KEY (`template_id`) REFERENCES `template`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板标签表';

CREATE TABLE IF NOT EXISTS `template_favorite` (
    `id`          bigint   NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `template_id` bigint   NOT NULL                COMMENT '模板ID',
    `user_id`     bigint   NOT NULL                COMMENT '用户ID',
    `create_user` bigint   DEFAULT NULL             COMMENT '创建人',
    `create_time` datetime NOT NULL                COMMENT '创建时间',
    `update_user` bigint   DEFAULT NULL             COMMENT '修改人',
    `update_time` datetime DEFAULT NULL             COMMENT '修改时间',
    `deleted`     bigint   NOT NULL DEFAULT 0       COMMENT '是否已删除（0：否；id：是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_user` (`template_id`, `user_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板收藏表';

CREATE TABLE IF NOT EXISTS `chat_session` (
    `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`          bigint       NOT NULL                COMMENT '所属用户ID',
    `title`            varchar(200) NOT NULL DEFAULT '新对话' COMMENT '会话标题',
    `preview`          varchar(200) DEFAULT ''               COMMENT '最后消息预览',
    `research_task_id` varchar(64)  DEFAULT NULL             COMMENT '关联的research深度研究任务ID',
    `create_user`      bigint       DEFAULT NULL             COMMENT '创建人',
    `create_time`      datetime     NOT NULL                 COMMENT '创建时间',
    `update_user`      bigint       DEFAULT NULL             COMMENT '修改人',
    `update_time`      datetime     DEFAULT NULL             COMMENT '修改时间',
    `last_time`        datetime     NOT NULL                 COMMENT '最后活跃时间',
    `deleted`          bigint       NOT NULL DEFAULT 0       COMMENT '是否已删除（0：否；id：是）',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_research_task` (`research_task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话会话表';

CREATE TABLE IF NOT EXISTS `chat_message` (
    `id`          bigint      NOT NULL AUTO_INCREMENT      COMMENT 'ID',
    `session_id`  bigint      NOT NULL                     COMMENT '所属会话ID',
    `role`        varchar(20) NOT NULL DEFAULT 'user'      COMMENT '消息角色: user/assistant/system',
    `content`     text        NOT NULL                     COMMENT '消息内容',
    `create_user` bigint      DEFAULT NULL                 COMMENT '创建人',
    `create_time` datetime    NOT NULL                     COMMENT '创建时间',
    `update_user` bigint      DEFAULT NULL                 COMMENT '修改人',
    `update_time` datetime    DEFAULT NULL                 COMMENT '修改时间',
    `deleted`     bigint      NOT NULL DEFAULT 0           COMMENT '是否已删除（0：否；id：是）',
    PRIMARY KEY (`id`),
    INDEX `idx_session_id` (`session_id`),
    FOREIGN KEY (`session_id`) REFERENCES `chat_session`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话消息表';

CREATE TABLE IF NOT EXISTS `code_snippet` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `session_id`  bigint       NOT NULL                COMMENT '所属会话ID',
    `message_id`  bigint       DEFAULT NULL            COMMENT '来源消息ID',
    `code`        text         NOT NULL                COMMENT '代码内容',
    `language`    varchar(30)  NOT NULL DEFAULT 'text' COMMENT '编程语言',
    `context`     varchar(500) DEFAULT NULL            COMMENT '触发上下文',
    `create_user` bigint       DEFAULT NULL            COMMENT '创建人',
    `create_time` datetime     NOT NULL                COMMENT '创建时间',
    `update_user` bigint       DEFAULT NULL            COMMENT '修改人',
    `update_time` datetime     DEFAULT NULL            COMMENT '修改时间',
    `deleted`     bigint       NOT NULL DEFAULT 0      COMMENT '是否已删除（0：否；id：是）',
    PRIMARY KEY (`id`),
    INDEX `idx_session_id` (`session_id`),
    FOREIGN KEY (`session_id`) REFERENCES `chat_session`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码片段表';

-- changeset codestyle:2
-- comment 初始化模板插件菜单
INSERT INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `redirect`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`)
VALUES
(4000, '模板中心', 0, 1, '/template', 'Template', 'Layout', '/template/list', 'apps', b'0', b'0', b'0', NULL, 4, 1, 1, NOW()),
(4010, '模板列表', 4000, 2, '/template/list', 'TemplateList', 'template/list/index', NULL, 'list', b'0', b'0', b'0', NULL, 1, 1, 1, NOW()),
(4011, '列表', 4010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:list', 1, 1, 1, NOW()),
(4012, '详情', 4010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:detail', 2, 1, 1, NOW()),
(4013, '收藏', 4010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:favorite', 3, 1, 1, NOW()),
(4014, '下载', 4010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:download', 4, 1, 1, NOW()),
(4020, '代码生成', 4000, 2, '/template/generate', 'TemplateGenerate', 'template/generate/index', NULL, 'code', b'0', b'0', b'0', NULL, 2, 1, 1, NOW()),
(4021, '对话', 4020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:generate:chat', 1, 1, 1, NOW()),
(4022, '会话管理', 4020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:generate:session', 2, 1, 1, NOW()),
(4023, '代码片段', 4020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:generate:snippet', 3, 1, 1, NOW()),
(4024, '深度研究', 4020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:generate:research', 4, 1, 1, NOW());

-- changeset codestyle:3
-- comment 初始化模板插件示例数据
INSERT INTO `template`
(`group_id`, `artifact_id`, `name`, `icon`, `author`, `description`, `version`, `download_count`, `rating`, `create_user`, `create_time`)
VALUES
('top.codestyle', 'spring-boot-crud', 'Spring Boot CRUD', 'SB', 'Charles7c', '标准的 Spring Boot 增删改查模板，包含 Controller、Service、Repository 完整代码结构，支持分页和高级查询。', 'v3.2.0', 1892, 4.8, 1, NOW()),
('top.codestyle', 'vue3-component', 'Vue3 组件模板', 'V3', '官方团队', 'Vue 3 Composition API 组件模板，包含 TypeScript 类型定义、Props、Emits、生命周期钩子等完整结构。', 'v2.1.0', 1543, 4.9, 1, NOW()),
('top.codestyle', 'mybatis-mapper', 'MyBatis Mapper', 'MB', '张三', 'MyBatis Mapper 映射文件模板，包含常用的 SQL 语句、动态查询、结果映射等配置，支持 MyBatis-Plus。', 'v1.5.0', 987, 4.7, 1, NOW()),
('top.codestyle', 'restful-api', 'RESTful API', 'API', '李四', '标准的 RESTful API 接口模板，包含统一响应格式、异常处理、参数校验、Swagger 文档注解等。', 'v2.0.0', 1234, 4.6, 1, NOW()),
('top.codestyle', 'form-page', '表单页面', 'FM', '王五', '基于 Arco Design 的表单页面模板，包含表单验证、动态表单、级联选择、文件上传等常用功能。', 'v1.3.0', 1765, 4.8, 1, NOW()),
('top.codestyle', 'jwt-auth', 'JWT 认证', 'JWT', '赵六', 'JWT Token 认证鉴权模板，包含用户登录、Token 生成与验证、权限控制、刷新机制等完整实现。', 'v2.2.0', 1456, 4.9, 1, NOW()),
('top.codestyle', 'data-report', '数据报表', 'RPT', '孙七', '数据报表展示模板，集成 ECharts 图表库，包含折线图、柱状图、饼图等常用图表及数据导出功能。', 'v1.1.0', 1123, 4.7, 1, NOW()),
('top.codestyle', 'scheduled-task', '定时任务', 'TSK', '周八', 'Spring Boot 定时任务模板，支持 Cron 表达式配置、任务动态管理、执行记录查询、失败重试等功能。', 'v1.0.0', 987, 4.5, 1, NOW()),
('top.codestyle', 'message-push', '消息推送', 'WS', '官方团队', '实时消息推送模板，支持 WebSocket 长连接、消息队列、多端同步、离线消息存储等功能。', 'v1.2.0', 1543, 4.8, 1, NOW());

INSERT INTO `template_tag` (`template_id`, `label`, `color`) VALUES
(1, 'Java', 'blue'), (1, 'Spring Boot', 'green'), (1, '后端', 'purple'),
(2, 'Vue3', 'green'), (2, 'TypeScript', 'blue'), (2, '前端', 'purple'),
(3, 'MyBatis', 'blue'), (3, '数据库', 'green'),
(4, 'API', 'blue'), (4, 'Spring Boot', 'green'), (4, '后端', 'purple'),
(5, 'Vue3', 'green'), (5, 'Arco Design', 'blue'), (5, '前端', 'purple'),
(6, 'Security', 'blue'), (6, 'JWT', 'green'), (6, '后端', 'purple'),
(7, 'Vue3', 'green'), (7, 'ECharts', 'blue'), (7, '可视化', 'purple'),
(8, 'Spring Boot', 'blue'), (8, 'Task', 'green'), (8, '后端', 'purple'),
(9, 'WebSocket', 'blue'), (9, 'Redis', 'green'), (9, '实时通信', 'purple');

