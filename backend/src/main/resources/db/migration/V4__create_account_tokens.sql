CREATE TABLE `account_token` (
    `account_token_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `purpose` VARCHAR(30) NOT NULL,
    `token_hash` VARCHAR(64) NOT NULL,
    `expires_at` DATETIME(6) NOT NULL,
    `used_at` DATETIME(6) NULL,
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`account_token_id`),
    CONSTRAINT `uk_account_token_hash` UNIQUE (`token_hash`),
    CONSTRAINT `fk_account_token_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
    CONSTRAINT `chk_account_token_purpose`
        CHECK (`purpose` IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET')),
    INDEX `ix_account_token_user_purpose` (`user_id`, `purpose`, `created_at`),
    INDEX `ix_account_token_cleanup` (`expires_at`, `used_at`)
) ENGINE=InnoDB;
