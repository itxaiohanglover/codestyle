-- MySQL建表语句脚本
-- 生成时间: 2025-12-04
-- 符合表结构: id BIGINT NOT NULL, meta_json JSON NOT NULL, create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP, update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, deleted TINYINT NOT NULL DEFAULT 0, group_id VARCHAR(255) GENERATED ALWAYS AS (meta_json->>'$.groupId') VIRTUAL
-- 注意：group_id是虚拟列，由数据库自动从meta_json中提取，不需要手动插入
-- 注意：create_time、update_time和deleted字段有默认值，不需要手动插入
DROP TABLE IF EXISTS tb_remote_meta_info;
CREATE TABLE tb_remote_meta_info (
    id BIGINT NOT NULL,
    meta_json JSON NOT NULL,
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    group_id VARCHAR(255) GENERATED ALWAYS AS (meta_json->>'$.groupId') VIRTUAL,
    PRIMARY KEY (id),
    INDEX idx_deleted (deleted),
    INDEX idx_group_id (group_id),
    INDEX idx_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
