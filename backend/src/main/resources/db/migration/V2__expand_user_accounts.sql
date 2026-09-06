ALTER TABLE `user`
    ADD COLUMN `email` VARCHAR(254) NULL,
    ADD COLUMN `email_verified_at` DATETIME(6) NULL,
    ADD COLUMN `role` VARCHAR(20) NULL,
    ADD COLUMN `status` VARCHAR(20) NULL,
    ADD COLUMN `created_at` DATETIME(6) NULL,
    ADD COLUMN `updated_at` DATETIME(6) NULL;

-- Every account that predates public registration is an existing administrator.
UPDATE `user`
SET `role` = 'ADMIN',
    `status` = 'ACTIVE',
    `created_at` = UTC_TIMESTAMP(6),
    `updated_at` = UTC_TIMESTAMP(6);

ALTER TABLE `user`
    MODIFY COLUMN `role` VARCHAR(20) NOT NULL DEFAULT 'USER',
    MODIFY COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    MODIFY COLUMN `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    MODIFY COLUMN `updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    ADD CONSTRAINT `uk_user_email` UNIQUE (`email`),
    ADD CONSTRAINT `chk_user_role` CHECK (`role` IN ('USER', 'ADMIN')),
    ADD CONSTRAINT `chk_user_status` CHECK (`status` IN ('ACTIVE', 'SUSPENDED', 'DELETED'));
