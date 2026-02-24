-- liquibase formatted sql

-- changeset codestyle:1
-- comment 初始化表数据
-- 初始化默认菜单
 INSERT IGNORE INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `redirect`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`)
VALUES
(1000, '系统管理', 0, 1, '/system', 'System', 'Layout', '/system/user', 'settings', b'0', b'0', b'0', NULL, 1, 1, 1, NOW()),
(1010, '用户管理', 1000, 2, '/system/user', 'SystemUser', 'system/user/index', NULL, 'user', b'0', b'0', b'0', NULL, 1, 1, 1, NOW()),
(1011, '列表', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:list', 1, 1, 1, NOW()),
(1012, '详情', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:get', 2, 1, 1, NOW()),
(1013, '新增', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:create', 3, 1, 1, NOW()),
(1014, '修改', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:update', 4, 1, 1, NOW()),
(1015, '删除', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:delete', 5, 1, 1, NOW()),
(1016, '导出', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:export', 6, 1, 1, NOW()),
(1017, '导入', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:import', 7, 1, 1, NOW()),
(1018, '重置密码', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:resetPwd', 8, 1, 1, NOW()),
(1019, '分配角色', 1010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:user:updateRole', 9, 1, 1, NOW()),

(1030, '角色管理', 1000, 2, '/system/role', 'SystemRole', 'system/role/index', NULL, 'user-management', b'0', b'0', b'0', NULL, 2, 1, 1, NOW()),
(1031, '列表', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:list', 1, 1, 1, NOW()),
(1032, '详情', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:get', 2, 1, 1, NOW()),
(1033, '新增', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:create', 3, 1, 1, NOW()),
(1034, '修改', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:update', 4, 1, 1, NOW()),
(1035, '删除', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:delete', 5, 1, 1, NOW()),
(1036, '修改权限', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:updatePermission', 6, 1, 1, NOW()),
(1037, '分配', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:assign', 7, 1, 1, NOW()),
(1038, '取消分配', 1030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:role:unassign', 8, 1, 1, NOW()),

(1050, '菜单管理', 1000, 2, '/system/menu', 'SystemMenu', 'system/menu/index', NULL, 'menu', b'0', b'0', b'0', NULL, 3, 1, 1, NOW()),
(1051, '列表', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:list', 1, 1, 1, NOW()),
(1052, '详情', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:get', 2, 1, 1, NOW()),
(1053, '新增', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:create', 3, 1, 1, NOW()),
(1054, '修改', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:update', 4, 1, 1, NOW()),
(1055, '删除', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:delete', 5, 1, 1, NOW()),
(1056, '清除缓存', 1050, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:menu:clearCache', 6, 1, 1, NOW()),

(1070, '部门管理', 1000, 2, '/system/dept', 'SystemDept', 'system/dept/index', NULL, 'mind-mapping', b'0', b'0', b'0', NULL, 4, 1, 1, NOW()),
(1071, '列表', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:list', 1, 1, 1, NOW()),
(1072, '详情', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:get', 2, 1, 1, NOW()),
(1073, '新增', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:create', 3, 1, 1, NOW()),
(1074, '修改', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:update', 4, 1, 1, NOW()),
(1075, '删除', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:delete', 5, 1, 1, NOW()),
(1076, '导出', 1070, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dept:export', 6, 1, 1, NOW()),

(1090, '通知公告', 1000, 2, '/system/notice', 'SystemNotice', 'system/notice/index', NULL, 'notification', b'0', b'0', b'0', NULL, 5, 1, 1, NOW()),
(1091, '列表', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:list', 1, 1, 1, NOW()),
(1092, '详情', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:get', 2, 1, 1, NOW()),
(1093, '查看公告', 1090, 2, '/system/notice/view', 'SystemNoticeView', 'system/notice/view/index', NULL, NULL, b'0', b'0', b'1', 'system:notice:view', 3, 1, 1, NOW()),
(1094, '发布公告', 1090, 2, '/system/notice/add', 'SystemNoticeAdd', 'system/notice/add/index', NULL, NULL, b'0', b'0', b'1', 'system:notice:create', 4, 1, 1, NOW()),
(1095, '修改', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:update', 5, 1, 1, NOW()),
(1096, '删除', 1090, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:notice:delete', 6, 1, 1, NOW()),

(1110, '文件管理', 1000, 2, '/system/file', 'SystemFile', 'system/file/index', NULL, 'file', b'0', b'0', b'0', NULL, 6, 1, 1, NOW()),
(1111, '列表', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:fileInfo:list', 1, 1, 1, NOW()),
(1112, '详情', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:fileInfo:get', 2, 1, 1, NOW()),
(1113, '上传', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:fileInfo:upload', 3, 1, 1, NOW()),
(1114, '修改', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:fileInfo:update', 4, 1, 1, NOW()),
(1115, '删除', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:fileInfo:delete', 5, 1, 1, NOW()),
(1116, '下载', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:fileInfo:download', 6, 1, 1, NOW()),
(1117, '创建文件夹', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:fileInfo:createDir', 7, 1, 1, NOW()),
(1118, '计算文件夹大小', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:fileInfo:calcDirSize', 8, 1, 1, NOW()),
(1119, '回收站文件列表', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:fileRecycle:list', 9, 1, 1, NOW()),
(1120, '还原回收站文件', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:fileRecycle:restore', 10, 1, 1, NOW()),
(1121, '删除回收站文件', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:fileRecycle:delete', 11, 1, 1, NOW()),
(1122, '清空回收站', 1110, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:fileRecycle:clean', 12, 1, 1, NOW()),

(1130, '字典管理', 1000, 2, '/system/dict', 'SystemDict', 'system/dict/index', NULL, 'bookmark', b'0', b'0', b'0', NULL, 7, 1, 1, NOW()),
(1131, '列表', 1130, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:list', 1, 1, 1, NOW()),
(1132, '详情', 1130, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:get', 2, 1, 1, NOW()),
(1133, '新增', 1130, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:create', 3, 1, 1, NOW()),
(1134, '修改', 1130, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:update', 4, 1, 1, NOW()),
(1135, '删除', 1130, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:delete', 5, 1, 1, NOW()),
(1136, '清除缓存', 1130, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dict:clearCache', 6, 1, 1, NOW()),
(1140, '字典项管理', 1000, 2, '/system/dict/item', 'SystemDictItem', 'system/dict/item/index', NULL, 'bookmark', b'0', b'0', b'1', NULL, 8, 1, 1, NOW()),
(1141, '列表', 1140, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dictItem:list', 1, 1, 1, NOW()),
(1142, '详情', 1140, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dictItem:get', 2, 1, 1, NOW()),
(1143, '新增', 1140, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dictItem:create', 3, 1, 1, NOW()),
(1144, '修改', 1140, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dictItem:update', 4, 1, 1, NOW()),
(1145, '删除', 1140, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:dictItem:delete', 5, 1, 1, NOW()),

(1150, '系统配置', 1000, 2, '/system/config', 'SystemConfig', 'system/config/index', NULL, 'config', b'0', b'0', b'0', NULL, 999, 1, 1, NOW()),
(1160, '网站配置', 1150, 2, '/system/config?tab=site', 'SystemSiteConfig', 'system/config/site/index', NULL, 'apps', b'0', b'0', b'1', NULL, 1, 1, 1, NOW()),
(1161, '查询', 1160, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:siteConfig:get', 1, 1, 1, NOW()),
(1162, '修改', 1160, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:siteConfig:update', 2, 1, 1, NOW()),
(1170, '安全配置', 1150, 2, '/system/config?tab=security', 'SystemSecurityConfig', 'system/config/security/index', NULL, 'safe', b'0', b'0', b'1', NULL, 2, 1, 1, NOW()),
(1171, '查询', 1170, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:securityConfig:get', 1, 1, 1, NOW()),
(1172, '修改', 1170, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:securityConfig:update', 2, 1, 1, NOW()),
(1180, '登录配置', 1150, 2, '/system/config?tab=login', 'SystemLoginConfig', 'system/config/login/index', NULL, 'lock', b'0', b'0', b'1', NULL, 3, 1, 1, NOW()),
(1181, '查询', 1180, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:loginConfig:get', 1, 1, 1, NOW()),
(1182, '修改', 1180, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:loginConfig:update', 2, 1, 1, NOW()),
(1190, '邮件配置', 1150, 2, '/system/config?tab=mail', 'SystemMailConfig', 'system/config/mail/index', NULL, 'email', b'0', b'0', b'1', NULL, 4, 1, 1, NOW()),
(1191, '查询', 1190, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:mailConfig:get', 1, 1, 1, NOW()),
(1192, '修改', 1190, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:mailConfig:update', 2, 1, 1, NOW()),
(1210, '短信配置', 1150, 2, '/system/config?tab=sms', 'SystemSmsConfig', 'system/config/sms/index', NULL, 'message', b'0', b'0', b'1', NULL, 5, 1, 1, NOW()),
(1211, '列表', 1210, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsConfig:list', 1, 1, 1, NOW()),
(1212, '详情', 1210, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsConfig:get', 2, 1, 1, NOW()),
(1213, '新增', 1210, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsConfig:create', 3, 1, 1, NOW()),
(1214, '修改', 1210, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsConfig:update', 4, 1, 1, NOW()),
(1215, '删除', 1210, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsConfig:delete', 5, 1, 1, NOW()),
(1216, '导出', 1210, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsConfig:export', 6, 1, 1, NOW()),
(1217, '设为默认配置', 1210, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsConfig:setDefault', 7, 1, 1, NOW()),
(1230, '存储配置', 1150, 2, '/system/config?tab=storage', 'SystemStorage', 'system/config/storage/index', NULL, 'storage', b'0', b'0', b'1', NULL, 6, 1, 1, NOW()),
(1231, '列表', 1230, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:list', 1, 1, 1, NOW()),
(1232, '详情', 1230, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:get', 2, 1, 1, NOW()),
(1233, '新增', 1230, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:create', 3, 1, 1, NOW()),
(1234, '修改', 1230, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:update', 4, 1, 1, NOW()),
(1235, '删除', 1230, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:delete', 5, 1, 1, NOW()),
(1236, '修改状态', 1230, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:updateStatus', 6, 1, 1, NOW()),
(1237, '设为默认存储', 1230, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:storage:setDefault', 7, 1, 1, NOW()),
(1250, '客户端配置', 1150, 2, '/system/config?tab=client', 'SystemClient', 'system/config/client/index', NULL, 'mobile', b'0', b'0', b'1', NULL, 7, 1, 1, NOW()),
(1251, '列表', 1250, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:client:list', 1, 1, 1, NOW()),
(1252, '详情', 1250, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:client:get', 2, 1, 1, NOW()),
(1253, '新增', 1250, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:client:create', 3, 1, 1, NOW()),
(1254, '修改', 1250, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:client:update', 4, 1, 1, NOW()),
(1255, '删除', 1250, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'system:client:delete', 5, 1, 1, NOW()),

(2000, '系统监控', 0, 1, '/monitor', 'Monitor', 'Layout', '/monitor/online', 'computer', b'0', b'0', b'0', NULL, 2, 1, 1, NOW()),
(2010, '在线用户', 2000, 2, '/monitor/online', 'MonitorOnline', 'monitor/online/index', NULL, 'user', b'0', b'0', b'0', NULL, 1, 1, 1, NOW()),
(2011, '列表', 2010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:online:list', 1, 1, 1, NOW()),
(2012, '强退', 2010, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:online:kickout', 2, 1, 1, NOW()),

(2030, '系统日志', 2000, 2, '/monitor/log', 'MonitorLog', 'monitor/log/index', NULL, 'history', b'0', b'0', b'0', NULL, 2, 1, 1, NOW()),
(2031, '列表', 2030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:log:list', 1, 1, 1, NOW()),
(2032, '详情', 2030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:log:get', 2, 1, 1, NOW()),
(2033, '导出', 2030, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'monitor:log:export', 3, 1, 1, NOW()),

(2050, '短信日志', 2000, 2, '/system/sms/log', 'SystemSmsLog', 'monitor/sms/log/index', NULL, 'message', b'0', b'0', b'0', NULL, 3, 1, 1, NOW()),
(2051, '列表', 2050, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsLog:list', 1, 1, 1, NOW()),
(2052, '删除', 2050, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsLog:delete', 2, 1, 1, NOW()),
(2053, '导出', 2050, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'system:smsLog:export', 3, 1, 1, NOW());

-- 初始化默认部门
 INSERT IGNORE INTO `sys_dept`
(`id`, `name`, `parent_id`, `ancestors`, `description`, `sort`, `status`, `is_system`, `create_user`, `create_time`)
VALUES
(1, 'CodeStyle', 0, '0', '系统初始部门', 1, 1, b'1', 1, NOW()),
(2, '研发部', 1, '0,1', NULL, 1, 1, b'0', 1, NOW());

-- 初始化默认角色
 INSERT IGNORE INTO `sys_role`
(`id`, `name`, `code`, `data_scope`, `description`, `sort`, `is_system`, `create_user`, `create_time`)
VALUES
(1, '超级管理员', 'super_admin', 1, '系统初始角色', 0, b'1', 1, NOW()),
(2, '普通用户', 'general', 4, NULL, 1, b'0', 1, NOW());

-- 初始化默认用户：admin/admin123
 INSERT IGNORE INTO `sys_user`
(`id`, `username`, `nickname`, `password`, `gender`, `email`, `phone`, `avatar`, `description`, `status`, `is_system`, `pwd_reset_time`, `dept_id`, `create_user`, `create_time`)
VALUES
(1, 'admin', '超级管理员', '{bcrypt}$2a$10$4jGwK2BMJ7FgVR.mgwGodey8.xR8FLoU1XSXpxJ9nZQt.pufhasSa', 1, NULL, NULL, NULL, '系统初始用户', 1, b'1', NOW(), 1, 1, NOW());

-- 初始化默认参数
 INSERT IGNORE INTO `sys_option`
(`id`, `category`, `name`, `code`, `value`, `default_value`, `description`)
VALUES
(1, 'SITE', '系统名称', 'SITE_TITLE', NULL, 'CodeStyle Admin', '显示在浏览器标题栏和登录界面的系统名称'),
(2, 'SITE', '系统描述', 'SITE_DESCRIPTION', NULL, '高质量前后端分离中后台管理系统框架', '用于 SEO 的网站元描述'),
(3, 'SITE', '版权声明', 'SITE_COPYRIGHT', NULL, 'Copyright © 2024 CodeStyle Admin 版权所有', '显示在页面底部的版权声明文本'),
(4, 'SITE', '备案号', 'SITE_BEIAN', NULL, NULL, '工信部 ICP 备案编号（如：京ICP备12345678号）'),
(5, 'SITE', '系统图标', 'SITE_FAVICON', NULL, '/favicon.ico', '浏览器标签页显示的网站图标（建议 .ico 格式）'),
(6, 'SITE', '系统LOGO', 'SITE_LOGO', NULL, '/logo.svg', '显示在登录页面和系统导航栏的网站图标（建议 .svg 格式）'),
(10, 'PASSWORD', '密码错误锁定阈值', 'PASSWORD_ERROR_LOCK_COUNT', NULL, '5', '连续登录失败次数达到该值将锁定账号（0-10次，0表示禁用锁定）'),
(11, 'PASSWORD', '账号锁定时长（分钟）', 'PASSWORD_ERROR_LOCK_MINUTES', NULL, '5', '账号锁定后自动解锁的时间（1-1440分钟，即24小时）'),
(12, 'PASSWORD', '密码有效期（天）', 'PASSWORD_EXPIRATION_DAYS', NULL, '0', '密码强制修改周期（0-999天，0表示永不过期）'),
(13, 'PASSWORD', '密码到期提醒（天）', 'PASSWORD_EXPIRATION_WARNING_DAYS', NULL, '0', '密码过期前的提前提醒天数（0表示不提醒）'),
(14, 'PASSWORD', '历史密码重复校验次数', 'PASSWORD_REPETITION_TIMES', NULL, '3', '禁止使用最近 N 次的历史密码（3-32次）'),
(15, 'PASSWORD', '密码最小长度', 'PASSWORD_MIN_LENGTH', NULL, '8', '密码最小字符长度要求（8-32个字符）'),
(16, 'PASSWORD', '是否允许密码包含用户名', 'PASSWORD_ALLOW_CONTAIN_USERNAME', NULL, '1', '是否允许密码包含正序或倒序的用户名字符'),
(17, 'PASSWORD', '密码是否必须包含特殊字符', 'PASSWORD_REQUIRE_SYMBOLS', NULL, '0', '是否要求密码必须包含特殊字符（如：!@#$%）'),
(20, 'MAIL', '邮件协议', 'MAIL_PROTOCOL', NULL, 'smtp', '邮件发送协议类型'),
(21, 'MAIL', '服务器地址', 'MAIL_HOST', NULL, NULL, '邮件服务器地址'),
(22, 'MAIL', '服务器端口', 'MAIL_PORT', NULL, '465', '邮件服务器连接端口'),
(23, 'MAIL', '邮箱账号', 'MAIL_USERNAME', NULL, NULL, '发件人邮箱地址'),
(24, 'MAIL', '邮箱密码', 'MAIL_PASSWORD', NULL, NULL, '服务授权密码/客户端专用密码'),
(25, 'MAIL', '启用SSL加密', 'MAIL_SSL_ENABLED', NULL, '1', '是否启用SSL/TLS加密连接'),
(26, 'MAIL', 'SSL端口号', 'MAIL_SSL_PORT', NULL, '465', 'SSL加密连接的备用端口（通常与主端口一致）'),
(27, 'LOGIN', '是否启用验证码', 'LOGIN_CAPTCHA_ENABLED', NULL, '1', NULL);

-- 初始化默认字典
INSERT INTO `sys_dict`
(`id`, `name`, `code`, `description`, `is_system`, `create_user`, `create_time`)
VALUES
(1, '公告分类', 'notice_type', NULL, b'1', 1, NOW()),
(2, '客户端类型', 'client_type', NULL, b'1', 1, NOW()),
(3, '短信厂商', 'sms_supplier', NULL, b'1', 1, NOW());

INSERT INTO `sys_dict_item`
(`id`, `label`, `value`, `color`, `sort`, `description`, `status`, `dict_id`, `create_user`, `create_time`)
VALUES
(1, '产品新闻', '1', 'primary', 1, NULL, 1, 1, 1, NOW()),
(2, '企业动态', '2', 'success', 2, NULL, 1, 1, 1, NOW()),
(3, '桌面端', 'PC', 'primary', 1, NULL, 1, 2, 1, NOW()),
(4, '安卓', 'ANDROID', 'success', 2, NULL, 1, 2, 1, NOW()),
(5, '小程序', 'XCX', 'warning', 3, NULL, 1, 2, 1, NOW()),
(6, '阿里云', 'alibaba', 'warning', 1, NULL, 1, 3, 1, NOW()),
(7, '腾讯云', 'tencent', 'primary', 2, NULL, 1, 3, 1, NOW()),
(8, '容联云', 'cloopen', 'success', 3, NULL, 1, 3, 1, NOW());

-- 初始化默认用户和角色关联数据
 INSERT IGNORE INTO `sys_user_role`
(`id`, `user_id`, `role_id`)
VALUES
(1, 1, 1);

-- 初始化默认角色和菜单关联数据
 INSERT IGNORE INTO `sys_role_menu`
(`role_id`, `menu_id`)
VALUES
(1, 1000),
(1, 1010),
(1, 1011),
(1, 1012),
(1, 1013),
(1, 1014),
(1, 1015),
(1, 1016),
(1, 1017),
(1, 1018),
(1, 1019),
(1, 1030),
(1, 1031),
(1, 1032),
(1, 1033),
(1, 1034),
(1, 1035),
(1, 1036),
(1, 1037),
(1, 1038),
(1, 1050),
(1, 1051),
(1, 1052),
(1, 1053),
(1, 1054),
(1, 1055),
(1, 1056),
(1, 1070),
(1, 1071),
(1, 1072),
(1, 1073),
(1, 1074),
(1, 1075),
(1, 1076),
(1, 1090),
(1, 1091),
(1, 1092),
(1, 1093),
(1, 1094),
(1, 1095),
(1, 1096),
(1, 1110),
(1, 1111),
(1, 1112),
(1, 1113),
(1, 1114),
(1, 1115),
(1, 1116),
(1, 1117),
(1, 1118),
(1, 1119),
(1, 1120),
(1, 1121),
(1, 1122),
(1, 1130),
(1, 1131),
(1, 1132),
(1, 1133),
(1, 1134),
(1, 1135),
(1, 1136),
(1, 1140),
(1, 1141),
(1, 1142),
(1, 1143),
(1, 1144),
(1, 1145),
(1, 1150),
(1, 1160),
(1, 1161),
(1, 1162),
(1, 1170),
(1, 1171),
(1, 1172),
(1, 1180),
(1, 1181),
(1, 1182),
(1, 1190),
(1, 1191),
(1, 1192),
(1, 1210),
(1, 1211),
(1, 1212),
(1, 1213),
(1, 1214),
(1, 1215),
(1, 1216),
(1, 1217),
(1, 1230),
(1, 1231),
(1, 1232),
(1, 1233),
(1, 1234),
(1, 1235),
(1, 1236),
(1, 1237),
(1, 1250),
(1, 1251),
(1, 1252),
(1, 1253),
(1, 1254),
(1, 1255),
(1, 2000),
(1, 2010),
(1, 2011),
(1, 2012),
(1, 2030),
(1, 2031),
(1, 2032),
(1, 2033),
(1, 2050),
(1, 2051),
(1, 2052),
(1, 2053),
(2, 2000),
(2, 2010),
(2, 2011);

-- 初始化默认角色和部门关联数据

-- 初始化默认存储
 INSERT IGNORE INTO `sys_storage`
(`id`, `name`, `code`, `type`, `access_key`, `secret_key`, `endpoint`, `bucket_name`, `domain`, `recycle_bin_enabled`, `recycle_bin_path`, `description`, `is_default`, `sort`, `status`, `create_user`, `create_time`)
VALUES
(1, '本地存储', 'local', 1, NULL, NULL, NULL, 'C:/codestyle-admin/data/file/', 'http://localhost:8000/file/', b'1', '.RECYCLE.BIN/', '本地存储', b'1', 1, 1, 1, NOW());

-- 初始化客户端数据
 INSERT IGNORE INTO `sys_client`
(`id`, `client_id`, `client_type`, `auth_type`, `active_timeout`, `timeout`, `is_concurrent`, `replaced_range`, `max_login_count`, `overflow_logout_mode`, `status`, `create_user`, `create_time`)
VALUES
(1, 'ef51c9a3e9046c4f2ea45142c8a8344a', 'PC', '["ACCOUNT", "EMAIL", "PHONE", "SOCIAL"]', 1800, 86400, 0, 'ALL_DEVICE_TYPE', -1, 'KICKOUT', 1, 1, NOW());
