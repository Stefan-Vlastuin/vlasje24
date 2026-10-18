package nl.vlasje24.security;

import nl.vlasje24.exception.TooManyRequestsException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class CommentRateLimiter {

    private static final int MINUTE_LIMIT = 5;
    private static final int HOUR_LIMIT = 30;
    private static final Duration MINUTE_WINDOW = Duration.ofMinutes(1);
    private static final Duration HOUR_WINDOW = Duration.ofHours(1);

    private final ConcurrentMap<Integer, UserAttempts> attemptsByUser = new ConcurrentHashMap<>();
    private final Clock clock;

    public CommentRateLimiter() {
        this(Clock.systemUTC());
    }

    CommentRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public void check(Integer userId) {
        Instant now = clock.instant();
        UserAttempts attempts = attemptsByUser.computeIfAbsent(userId, ignored -> new UserAttempts());

        synchronized (attempts) {
            removeExpired(attempts.minute, now.minus(MINUTE_WINDOW));
            removeExpired(attempts.hour, now.minus(HOUR_WINDOW));
            if (attempts.minute.size() >= MINUTE_LIMIT || attempts.hour.size() >= HOUR_LIMIT) {
                throw new TooManyRequestsException();
            }
            attempts.minute.addLast(now);
            attempts.hour.addLast(now);
        }
    }

    private void removeExpired(Deque<Instant> attempts, Instant cutoff) {
        while (!attempts.isEmpty() && !attempts.peekFirst().isAfter(cutoff)) {
            attempts.removeFirst();
        }
    }

    private static final class UserAttempts {
        private final Deque<Instant> minute = new ArrayDeque<>();
        private final Deque<Instant> hour = new ArrayDeque<>();
    }
}
