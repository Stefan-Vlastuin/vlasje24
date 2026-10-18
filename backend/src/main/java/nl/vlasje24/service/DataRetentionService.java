package nl.vlasje24.service;

import nl.vlasje24.repository.CommentRepository;
import nl.vlasje24.repository.ModerationAuditRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class DataRetentionService {

    private final CommentRepository commentRepository;
    private final ModerationAuditRepository auditRepository;
    private final Clock clock;
    private final Duration deletedCommentRetention;
    private final Duration auditRetention;

    public DataRetentionService(
            CommentRepository commentRepository,
            ModerationAuditRepository auditRepository,
            Clock clock,
            @Value("${app.data-retention.deleted-comments:90d}") Duration deletedCommentRetention,
            @Value("${app.data-retention.moderation-audit:365d}") Duration auditRetention) {
        this.commentRepository = commentRepository;
        this.auditRepository = auditRepository;
        this.clock = clock;
        this.deletedCommentRetention = deletedCommentRetention;
        this.auditRetention = auditRetention;
    }

    @Transactional
    @Scheduled(cron = "${app.data-retention.cleanup-cron:0 30 3 * * *}")
    public void cleanup() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        auditRepository.deleteCreatedBefore(now.minus(auditRetention));
        commentRepository.deleteSoftDeletedBefore(now.minus(deletedCommentRetention));
    }
}
