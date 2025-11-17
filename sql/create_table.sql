# 数据库初始化

-- 创建库
create database if not exists codestyle_cloud;

-- 切换库
use codestyle_cloud;

-- 用户表
-- 以下是建表语句

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 模板表
create table if not exists template
(
    id           bigint auto_increment comment 'id' primary key,
    appName      varchar(256)                       null comment '应用名称',
    cover        varchar(512)                       null comment '应用封面',
    initPrompt   text                               null comment '应用初始化的 prompt',
    codeGenType  varchar(64)                        null comment '代码生成类型（枚举）',
    deployKey    varchar(64)                        null comment '部署标识',
    deployedTime datetime                           null comment '部署时间',
    priority     int      default 0                 not null comment '优先级',
    userId       bigint                             not null comment '创建用户id',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    UNIQUE KEY uk_deployKey (deployKey), -- 确保部署标识唯一
    INDEX idx_appName (appName),         -- 提升基于应用名称的查询性能
    INDEX idx_userId (userId)            -- 提升基于用户 ID 的查询性能
) comment '模板' collate = utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS `sys_storage` (
                                             `id`          bigint(20)   AUTO_INCREMENT              COMMENT 'ID',
                                             `name`        varchar(100) NOT NULL                    COMMENT '名称',
                                             `code`        varchar(30)  NOT NULL                    COMMENT '编码',
                                             `type`        tinyint(1)   UNSIGNED NOT NULL DEFAULT 1 COMMENT '类型（1：本地存储；2：对象存储）',
                                             `access_key`  varchar(255) DEFAULT NULL                COMMENT 'Access Key',
                                             `secret_key`  varchar(255) DEFAULT NULL                COMMENT 'Secret Key',
                                             `endpoint`    varchar(255) DEFAULT NULL                COMMENT 'Endpoint',
                                             `bucket_name` varchar(255) NOT NULL                    COMMENT 'Bucket',
                                             `domain`      varchar(255) DEFAULT NULL                COMMENT '域名',
                                             `description` varchar(200) DEFAULT NULL                COMMENT '描述',
                                             `is_default`  bit(1)       NOT NULL DEFAULT b'0'       COMMENT '是否为默认存储',
                                             `sort`        int          NOT NULL DEFAULT 999        COMMENT '排序',
                                             `status`      tinyint(1)   UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（1：启用；2：禁用）',
                                             `create_user` bigint(20)   NOT NULL                    COMMENT '创建人',
                                             `create_time` datetime     NOT NULL                    COMMENT '创建时间',
                                             `update_user` bigint(20)   DEFAULT NULL                COMMENT '修改人',
                                             `update_time` datetime     DEFAULT NULL                COMMENT '修改时间',
                                             `deleted`     bigint(20)   NOT NULL DEFAULT 0          COMMENT '是否已删除（0：否；id：是）',
                                             PRIMARY KEY (`id`),
                                             UNIQUE INDEX `uk_code`(`code`, `deleted`),
                                             INDEX `idx_create_user`(`create_user`),
                                             INDEX `idx_update_user`(`update_user`),
                                             INDEX `idx_deleted`(`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储表';

CREATE TABLE IF NOT EXISTS `sys_file` (
                                          `id`                 bigint(20)    AUTO_INCREMENT              COMMENT 'ID',
                                          `name`               varchar(255)  NOT NULL                    COMMENT '名称',
                                          `original_name`      varchar(255)  NOT NULL                    COMMENT '原始名称',
                                          `size`               bigint(20)    DEFAULT NULL                COMMENT '大小（字节）',
                                          `parent_path`        varchar(512)  NOT NULL DEFAULT '/'        COMMENT '上级目录',
                                          `path`               varchar(512)  NOT NULL                    COMMENT '路径',
                                          `extension`          varchar(32)   DEFAULT NULL                COMMENT '扩展名',
                                          `content_type`       varchar(255)  DEFAULT NULL                COMMENT '内容类型',
                                          `type`               tinyint(1)    UNSIGNED NOT NULL DEFAULT 1 COMMENT '类型（0: 目录；1：其他；2：图片；3：文档；4：视频；5：音频）',
                                          `sha256`             varchar(256)  DEFAULT NULL                COMMENT 'SHA256值',
                                          `metadata`           text          DEFAULT NULL                COMMENT '元数据',
                                          `thumbnail_name`     varchar(255)  DEFAULT NULL                COMMENT '缩略图名称',
                                          `thumbnail_size`     bigint(20)    DEFAULT NULL                COMMENT '缩略图大小（字节)',
                                          `thumbnail_metadata` text          DEFAULT NULL                COMMENT '缩略图元数据',
                                          `storage_id`         bigint(20)    NOT NULL                    COMMENT '存储ID',
                                          `create_user`        bigint(20)    NOT NULL                    COMMENT '创建人',
                                          `create_time`        datetime      NOT NULL                    COMMENT '创建时间',
                                          `update_user`        bigint(20)    DEFAULT NULL                COMMENT '修改人',
                                          `update_time`        datetime      DEFAULT NULL                COMMENT '修改时间',
                                          `deleted`            bigint(20)    NOT NULL DEFAULT 0          COMMENT '是否已删除（0：否；id：是）',
                                          PRIMARY KEY (`id`),
                                          INDEX `idx_type`(`type`),
                                          INDEX `idx_sha256`(`sha256`),
                                          INDEX `idx_storage_id`(`storage_id`),
                                          INDEX `idx_create_user`(`create_user`),
                                          INDEX `idx_update_user`(`update_user`),
                                          INDEX `idx_deleted`(`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

-- 初始化默认存储
INSERT INTO `sys_storage`
(`id`, `name`, `code`, `type`, `access_key`, `secret_key`, `endpoint`, `bucket_name`, `domain`, `description`, `is_default`, `sort`, `status`, `create_user`, `create_time`)
VALUES
    (1, '本地windows存储', 'local_dev', 1, NULL, NULL, NULL, 'E:/Java项目/code/codestyle-cloud/data/templates', 'http://localhost:8000/file/', '本地存储', b'1', 1, 1, 1, NOW()),
    (2, '生产环境', 'local_prod', 1, NULL, NULL, NULL, '../data/file/', 'http://api.continew.top/file/', '本地存储', b'0', 2, 2, 1, NOW());

