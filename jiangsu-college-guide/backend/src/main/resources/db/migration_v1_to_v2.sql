-- ============================================================
-- Migration: V1 → V2 (可重复执行，幂等)
-- 适用条件: 已有 V1 schema.sql 建的表且已有数据
-- 执行前请备份: mysqldump --single-transaction > backup_v1.sql
-- ============================================================
-- MySQL 5.7 用户注意:
--   ADD COLUMN IF NOT EXISTS / ADD INDEX IF NOT EXISTS
--   是 MySQL 8.0.1+ 语法。如果在 5.7 上执行，请逐条手工运行
--   去掉 IF NOT EXISTS。
-- ============================================================

-- ── 1. app_user: 邮箱唯一约束 + 验证字段 ──
-- 先检查是否有重复 email:
-- SELECT email, COUNT(*) AS cnt FROM app_user WHERE email IS NOT NULL GROUP BY email HAVING cnt > 1;
-- 如果有重复，先手动处理后再加唯一索引

ALTER TABLE `app_user`
    ADD COLUMN IF NOT EXISTS `email_verified` TINYINT NOT NULL DEFAULT 0 AFTER `email`;

ALTER TABLE `app_user`
    ADD COLUMN IF NOT EXISTS `email_verified_at` DATETIME DEFAULT NULL AFTER `email_verified`;

-- H2 兼容写法(如果 MySQL 5.7 不支持 IF NOT EXISTS):
-- ALTER TABLE `app_user` ADD COLUMN `email_verified` TINYINT NOT NULL DEFAULT 0;
-- ALTER TABLE `app_user` ADD COLUMN `email_verified_at` DATETIME DEFAULT NULL;

ALTER TABLE `app_user` ADD UNIQUE KEY IF NOT EXISTS `uk_email` (`email`);

-- ── 2. school: type 索引 ──
ALTER TABLE `school` ADD INDEX IF NOT EXISTS `idx_school_type` (`type`);

-- ── 3. comment: 来源追踪 + 复合索引 ──
ALTER TABLE `comment`
    ADD COLUMN IF NOT EXISTS `source_type` VARCHAR(20) DEFAULT 'DIRECT' AFTER `like_count`;

ALTER TABLE `comment`
    ADD COLUMN IF NOT EXISTS `submission_id` BIGINT DEFAULT NULL AFTER `source_type`;

ALTER TABLE `comment` ADD INDEX IF NOT EXISTS `idx_comment_school_status` (`school_id`, `status`);

-- ── 4. submission: 转化追踪 + type 索引 ──
ALTER TABLE `submission`
    ADD COLUMN IF NOT EXISTS `converted_type` VARCHAR(20) DEFAULT NULL AFTER `reviewed_at`;

ALTER TABLE `submission`
    ADD COLUMN IF NOT EXISTS `converted_id` BIGINT DEFAULT NULL AFTER `converted_type`;

ALTER TABLE `submission` ADD INDEX IF NOT EXISTS `idx_sub_type` (`type`);

-- ── 5. experience: 状态 + 来源追踪 ──
ALTER TABLE `experience`
    ADD COLUMN IF NOT EXISTS `source_type` VARCHAR(20) DEFAULT 'OFFICIAL' AFTER `tags`;

ALTER TABLE `experience`
    ADD COLUMN IF NOT EXISTS `submission_id` BIGINT DEFAULT NULL AFTER `source_type`;

ALTER TABLE `experience`
    ADD COLUMN IF NOT EXISTS `status` VARCHAR(20) NOT NULL DEFAULT 'APPROVED' AFTER `submission_id`;

ALTER TABLE `experience` ADD INDEX IF NOT EXISTS `idx_exp_status` (`status`);

-- 将已有 experience 数据标为 APPROVED (如果有 NULL):
-- UPDATE `experience` SET `source_type` = 'OFFICIAL', `status` = 'APPROVED' WHERE `status` IS NULL OR `status` = '';

-- ── 6. qa_entry: 状态 + 来源追踪 ──
ALTER TABLE `qa_entry`
    ADD COLUMN IF NOT EXISTS `source_type` VARCHAR(20) DEFAULT 'OFFICIAL' AFTER `like_count`;

ALTER TABLE `qa_entry`
    ADD COLUMN IF NOT EXISTS `submission_id` BIGINT DEFAULT NULL AFTER `source_type`;

ALTER TABLE `qa_entry`
    ADD COLUMN IF NOT EXISTS `status` VARCHAR(20) NOT NULL DEFAULT 'APPROVED' AFTER `submission_id`;

ALTER TABLE `qa_entry` ADD INDEX IF NOT EXISTS `idx_qa_status` (`status`);

-- 将已有 qa_entry 数据标为 APPROVED:
-- UPDATE `qa_entry` SET `source_type` = 'OFFICIAL', `status` = 'APPROVED' WHERE `status` IS NULL OR `status` = '';

-- ── 7. school_life_info: 投稿来源追踪 ──
ALTER TABLE `school_life_info`
    ADD COLUMN IF NOT EXISTS `submission_id` BIGINT DEFAULT NULL AFTER `source_type`;

-- ── 8. 创建 user_like 表 (V2 核心新增) ──
CREATE TABLE IF NOT EXISTS `user_like` (
    `id`          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    `user_id`     BIGINT      NOT NULL               COMMENT '用户ID',
    `target_type` VARCHAR(20) NOT NULL               COMMENT '点赞目标类型: COMMENT/EXPERIENCE/QA',
    `target_id`   BIGINT      NOT NULL               COMMENT '点赞目标ID',
    `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_like (`user_id`, `target_type`, `target_id`),
    INDEX idx_like_target (`target_type`, `target_id`),
    CONSTRAINT fk_like_user FOREIGN KEY (`user_id`) REFERENCES `app_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户点赞表';

-- ── 9. 数据校准 (可选) ──
-- 将 school.favorite_count 与 favorite 表实际数量对齐:
-- UPDATE school s SET s.favorite_count = (
--     SELECT COUNT(*) FROM favorite f WHERE f.school_id = s.id
-- );

-- ── 完成 ──
-- 验证: SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() ORDER BY TABLE_NAME;
