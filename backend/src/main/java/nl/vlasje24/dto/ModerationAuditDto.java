package nl.vlasje24.dto;

import nl.vlasje24.domain.ModerationAction;
import nl.vlasje24.domain.ModerationAudit;

import java.time.Instant;
import java.time.ZoneOffset;

public record ModerationAuditDto(
        Long auditId,
        Integer adminUserId,
        String adminUsername,
        ModerationAction action,
        Long targetCommentId,
        Integer targetUserId,
        String targetUsername,
        String reason,
        Instant createdAt
) {
    public static ModerationAuditDto from(ModerationAudit audit) {
        return new ModerationAuditDto(
                audit.getModerationAuditId(),
                audit.getAdmin().getUserId(),
                audit.getAdmin().getUsername(),
                audit.getAction(),
                audit.getTargetComment() == null ? null : audit.getTargetComment().getCommentId(),
                audit.getTargetUser().getUserId(),
                audit.getTargetUser().getUsername(),
                audit.getReason(),
                audit.getCreatedAt().toInstant(ZoneOffset.UTC));
    }
}
