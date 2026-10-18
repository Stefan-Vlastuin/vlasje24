package nl.vlasje24.security;

import nl.vlasje24.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthRateLimiterTest {

    @Test
    void login_blocksEleventhAttemptWithinWindow() {
        AuthRateLimiter limiter = new AuthRateLimiter();
        for (int attempt = 0; attempt < 10; attempt++) {
            limiter.checkLogin("127.0.0.1");
        }

        assertThatThrownBy(() -> limiter.checkLogin("127.0.0.1"))
                .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    void registration_blocksSixthAttemptWithinWindow() {
        AuthRateLimiter limiter = new AuthRateLimiter();
        for (int attempt = 0; attempt < 5; attempt++) {
            limiter.checkRegistration("127.0.0.1");
        }

        assertThatThrownBy(() -> limiter.checkRegistration("127.0.0.1"))
                .isInstanceOf(TooManyRequestsException.class);
    }
}
