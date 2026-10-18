ALTER TABLE `moderation_audit`
    DROP FOREIGN KEY `fk_moderation_audit_comment`,
    DROP CHECK `chk_moderation_audit_target`;

ALTER TABLE `moderation_audit`
    ADD CONSTRAINT `fk_moderation_audit_comment`
        FOREIGN KEY (`target_comment_id`) REFERENCES `comment` (`comment_id`) ON DELETE SET NULL;
