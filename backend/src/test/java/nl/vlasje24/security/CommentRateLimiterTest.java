package nl.vlasje24.security;

import nl.vlasje24.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommentRateLimiterTest {

    @Test
    void sixthCommentWithinMinuteIsRejected() {
        MutableClock clock = new MutableClock();
        CommentRateLimiter limiter = new CommentRateLimiter(clock);

        for (int i = 0; i < 5; i++) {
            limiter.check(42);
        }

        assertThatThrownBy(() -> limiter.check(42))
                .isInstanceOf(TooManyRequestsException.class);
        assertThatCode(() -> limiter.check(43)).doesNotThrowAnyException();
    }

    @Test
    void thirtyFirstCommentWithinHourIsRejected() {
        MutableClock clock = new MutableClock();
        CommentRateLimiter limiter = new CommentRateLimiter(clock);

        for (int batch = 0; batch < 6; batch++) {
            for (int i = 0; i < 5; i++) {
                limiter.check(42);
            }
            clock.advance(Duration.ofMinutes(2));
        }

        assertThatThrownBy(() -> limiter.check(42))
                .isInstanceOf(TooManyRequestsException.class);
    }

    private static final class MutableClock extends Clock {
        private Instant instant = Instant.parse("2026-10-17T10:00:00Z");

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
