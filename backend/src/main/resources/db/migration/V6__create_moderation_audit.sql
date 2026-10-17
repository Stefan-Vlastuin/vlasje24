CREATE TABLE `moderation_audit` (
    `moderation_audit_id` BIGINT NOT NULL AUTO_INCREMENT,
    `admin_user_id` INT NOT NULL,
    `action` VARCHAR(30) NOT NULL,
    `target_comment_id` BIGINT NULL,
    `target_user_id` INT NOT NULL,
    `reason` VARCHAR(500) NULL,
    `created_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`moderation_audit_id`),
    CONSTRAINT `fk_moderation_audit_admin`
        FOREIGN KEY (`admin_user_id`) REFERENCES `user` (`user_id`),
    CONSTRAINT `fk_moderation_audit_comment`
        FOREIGN KEY (`target_comment_id`) REFERENCES `comment` (`comment_id`),
    CONSTRAINT `fk_moderation_audit_target_user`
        FOREIGN KEY (`target_user_id`) REFERENCES `user` (`user_id`),
    CONSTRAINT `chk_moderation_audit_action`
        CHECK (`action` IN ('COMMENT_DELETED', 'USER_SUSPENDED', 'USER_REACTIVATED')),
    CONSTRAINT `chk_moderation_audit_target`
        CHECK (
            (`action` = 'COMMENT_DELETED' AND `target_comment_id` IS NOT NULL)
            OR
            (`action` IN ('USER_SUSPENDED', 'USER_REACTIVATED') AND `target_comment_id` IS NULL)
        ),
    INDEX `idx_moderation_audit_created` (`created_at`, `moderation_audit_id`),
    INDEX `idx_moderation_audit_comment` (`target_comment_id`),
    INDEX `idx_moderation_audit_user` (`target_user_id`)
);
