package nl.vlasje24.service;

import nl.vlasje24.repository.CommentRepository;
import nl.vlasje24.repository.ModerationAuditRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DataRetentionServiceTest {

    @Test
    void cleanup_appliesConfiguredRetentionPeriods() {
        CommentRepository comments = mock(CommentRepository.class);
        ModerationAuditRepository audit = mock(ModerationAuditRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-10-17T12:00:00Z"), ZoneOffset.UTC);
        var service = new DataRetentionService(
                comments, audit, clock, Duration.ofDays(90), Duration.ofDays(365));

        service.cleanup();

        LocalDateTime now = LocalDateTime.of(2026, 10, 17, 12, 0);
        verify(comments).deleteSoftDeletedBefore(now.minusDays(90));
        verify(audit).deleteCreatedBefore(now.minusDays(365));
    }
}
