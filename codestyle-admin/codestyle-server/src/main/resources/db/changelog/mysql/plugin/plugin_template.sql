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
    `rating`         decimal(3,1) DEFAULT 0.0             COMMENT '评分',
    `download_url`   varchar(500) DEFAULT NULL            COMMENT '下载地址',
    `search_ref_id`  varchar(100) DEFAULT NULL            COMMENT 'search模块模板ID',
    `create_user`    bigint       NOT NULL                COMMENT '创建人',
    `create_time`    datetime     NOT NULL                COMMENT '创建时间',
    `update_user`    bigint       DEFAULT NULL            COMMENT '修改人',
    `update_time`    datetime     DEFAULT NULL            COMMENT '修改时间',
    `deleted`        bigint       NOT NULL DEFAULT 0      COMMENT '是否已删除（0：否；id：是）',
    PRIMARY KEY (`id`),
    INDEX `idx_group_artifact` (`group_id`, `artifact_id`),
    INDEX `idx_deleted` (`deleted`),
    UNIQUE INDEX `uk_group_artifact_version` (`group_id`, `artifact_id`, `version`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码模板表';

CREATE TABLE IF NOT EXISTS `template_tag` (
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `template_id` bigint      NOT NULL                COMMENT '模板ID',
    `label`       varchar(50) NOT NULL                COMMENT '标签文字',
    `color`       varchar(20) NOT NULL                COMMENT '标签颜色',
    PRIMARY KEY (`id`),
    INDEX `idx_template_id` (`template_id`)
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
    INDEX `idx_session_id` (`session_id`)
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
    INDEX `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码片段表';

-- changeset codestyle:2
-- comment 初始化模板插件菜单
INSERT INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `redirect`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`)
VALUES
(4000, '模板中心', 0, 1, '/template', 'Template', 'Layout', '/template/list', 'apps', b'0', b'0', b'0', NULL, 4, 1, 1, NOW()),
(4010, '模板列表', 4000, 2, '/template/list', 'TemplateList', 'template/list/index', NULL, 'list', b'0', b'0', b'0', NULL, 1, 1, 1, NOW()),
(4011, '列表', 4010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:library:list', 1, 1, 1, NOW()),
(4012, '详情', 4010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:library:get', 2, 1, 1, NOW()),
(4013, '收藏', 4010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:library:favorite', 3, 1, 1, NOW()),
(4014, '下载', 4010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:library:download', 4, 1, 1, NOW()),
(4015, '新增', 4010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:library:create', 5, 1, 1, NOW()),
(4016, '修改', 4010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:library:update', 6, 1, 1, NOW()),
(4017, '删除', 4010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:library:delete', 7, 1, 1, NOW()),
(4018, '上传', 4010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:library:upload', 8, 1, 1, NOW()),
(4020, '代码生成', 4000, 2, '/template/generate', 'TemplateGenerate', 'template/generate/index', NULL, 'code', b'0', b'0', b'0', NULL, 2, 1, 1, NOW()),
(4021, '对话', 4020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:generate:chat', 1, 1, 1, NOW()),
(4022, '会话管理', 4020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:generate:session', 2, 1, 1, NOW()),
(4023, '代码片段', 4020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:generate:snippet', 3, 1, 1, NOW()),
(4024, '深度研究', 4020, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'template:generate:research', 4, 1, 1, NOW());

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 4000), (1, 4010), (1, 4011), (1, 4012), (1, 4013), (1, 4014),
(1, 4015), (1, 4016), (1, 4017), (1, 4018), (1, 4020), (1, 4021),
(1, 4022), (1, 4023), (1, 4024);

