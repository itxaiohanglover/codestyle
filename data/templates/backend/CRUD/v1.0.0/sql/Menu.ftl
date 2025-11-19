-- ${businessName}管理菜单
INSERT INTO `sys_menu`
(`title`, `parent_id`, `type`, `path`, `name`, `component`, `redirect`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`, `update_user`, `update_time`)
VALUES
('${businessName}管理', 1000, 2, '/${apiModuleName}/${apiName}', '${classNamePrefix}', '${apiModuleName}/${apiName}/index', NULL, NULL, b'0', b'0', b'0', NULL, 1, 1, 1, NOW(), NULL, NULL);

SET @parentId = LAST_INSERT_ID();

-- ${businessName}管理按钮
INSERT INTO `sys_menu`
(`title`, `parent_id`, `type`, `path`, `name`, `component`, `redirect`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`, `update_user`, `update_time`)
VALUES
('列表', @parentId, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '${apiModuleName}:${apiName}:list', 1, 1, 1, NOW(), NULL, NULL),
('详情', @parentId, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '${apiModuleName}:${apiName}:detail', 2, 1, 1, NOW(), NULL, NULL),
('新增', @parentId, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '${apiModuleName}:${apiName}:add', 3, 1, 1, NOW(), NULL, NULL),
('修改', @parentId, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '${apiModuleName}:${apiName}:update', 4, 1, 1, NOW(), NULL, NULL),
('删除', @parentId, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '${apiModuleName}:${apiName}:delete', 5, 1, 1, NOW(), NULL, NULL),
('导出', @parentId, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '${apiModuleName}:${apiName}:export', 6, 1, 1, NOW(), NULL, NULL);

<#--
-- PostgreSQL
-- ${businessName}管理菜单
INSERT INTO "sys_menu"
("id", "title", "parent_id", "type", "path", "name", "component", "redirect", "icon", "is_external", "is_cache", "is_hidden", "permission", "sort", "status", "create_user", "create_time", "update_user", "update_time")
VALUES
('${businessName}管理', 1000, 2, '/${apiModuleName}/${apiName}', '${classNamePrefix}', '${apiModuleName}/${apiName}/index', NULL, NULL, false, false, false, NULL, 1, 1, 1, NOW(), NULL, NULL) RETURNING id INTO parentId;

-- ${businessName}管理按钮
INSERT INTO "sys_menu"
("id", "title", "parent_id", "type", "path", "name", "component", "redirect", "icon", "is_external", "is_cache", "is_hidden", "permission", "sort", "status", "create_user", "create_time", "update_user", "update_time")
VALUES
('列表', parentId, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '${apiModuleName}:${apiName}:list', 1, 1, 1, NOW(), NULL, NULL),
('详情', parentId, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '${apiModuleName}:${apiName}:detail', 2, 1, 1, NOW(), NULL, NULL),
('新增', parentId, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '${apiModuleName}:${apiName}:add', 3, 1, 1, NOW(), NULL, NULL),
('修改', parentId, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '${apiModuleName}:${apiName}:update', 4, 1, 1, NOW(), NULL, NULL),
('删除', parentId, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '${apiModuleName}:${apiName}:delete', 5, 1, 1, NOW(), NULL, NULL),
('导出', parentId, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '${apiModuleName}:${apiName}:export', 6, 1, 1, NOW(), NULL, NULL);-->
