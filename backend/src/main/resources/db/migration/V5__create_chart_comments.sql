CREATE TABLE `comment` (
    `comment_id` BIGINT NOT NULL AUTO_INCREMENT,
    `chart_week_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    `body` VARCHAR(1000) NOT NULL,
    `status` VARCHAR(24) NOT NULL DEFAULT 'VISIBLE',
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    `deleted_at` DATETIME(6) NULL,
    PRIMARY KEY (`comment_id`),
    CONSTRAINT `fk_comment_chart_week`
        FOREIGN KEY (`chart_week_id`) REFERENCES `date` (`week_id`),
    CONSTRAINT `fk_comment_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
    CONSTRAINT `chk_comment_status`
        CHECK (`status` IN ('VISIBLE', 'DELETED_BY_AUTHOR', 'DELETED_BY_MODERATOR')),
    INDEX `idx_comment_chart_status_created`
        (`chart_week_id`, `status`, `created_at`, `comment_id`),
    INDEX `idx_comment_user` (`user_id`)
);
