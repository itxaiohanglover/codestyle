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
