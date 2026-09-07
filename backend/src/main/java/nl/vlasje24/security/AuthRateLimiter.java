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
public class AuthRateLimiter {

    private static final int LOGIN_ATTEMPTS = 10;
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(1);
    private static final int REGISTRATION_ATTEMPTS = 5;
    private static final Duration REGISTRATION_WINDOW = Duration.ofHours(1);

    private final ConcurrentMap<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();
    private final Clock clock;

    public AuthRateLimiter() {
        this(Clock.systemUTC());
    }

    AuthRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public void checkLogin(String clientAddress) {
        check("login:" + clientAddress, LOGIN_ATTEMPTS, LOGIN_WINDOW);
    }

    public void checkRegistration(String clientAddress) {
        check("register:" + clientAddress, REGISTRATION_ATTEMPTS, REGISTRATION_WINDOW);
    }

    private void check(String key, int limit, Duration window) {
        Instant now = clock.instant();
        Instant cutoff = now.minus(window);
        Deque<Instant> timestamps = attempts.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && !timestamps.peekFirst().isAfter(cutoff)) {
                timestamps.removeFirst();
            }
            if (timestamps.size() >= limit) {
                throw new TooManyRequestsException();
            }
            timestamps.addLast(now);
        }
    }
}
