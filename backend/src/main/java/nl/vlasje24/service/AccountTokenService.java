package nl.vlasje24.service;

import nl.vlasje24.domain.AccountToken;
import nl.vlasje24.domain.AccountTokenPurpose;
import nl.vlasje24.domain.User;
import nl.vlasje24.dto.ResetPasswordRequestDto;
import nl.vlasje24.exception.InvalidAccountDataException;
import nl.vlasje24.exception.InvalidAccountTokenException;
import nl.vlasje24.repository.AccountTokenRepository;
import nl.vlasje24.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class AccountTokenService {

    private static final int TOKEN_BYTES = 32;
    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AccountTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;
    private final Duration verificationValidity;
    private final Duration resetValidity;
    private final Duration requestCooldown;
    private final Duration cleanupRetention;

    public AccountTokenService(
            AccountTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            FindByIndexNameSessionRepository<? extends Session> sessionRepository,
            ApplicationEventPublisher eventPublisher,
            Clock clock,
            @Value("${app.account-token.verification-validity:24h}") Duration verificationValidity,
            @Value("${app.account-token.reset-validity:60m}") Duration resetValidity,
            @Value("${app.account-token.request-cooldown:1m}") Duration requestCooldown,
            @Value("${app.account-token.cleanup-retention:7d}") Duration cleanupRetention) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionRepository = sessionRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        this.verificationValidity = verificationValidity;
        this.resetValidity = resetValidity;
        this.requestCooldown = requestCooldown;
        this.cleanupRetention = cleanupRetention;
    }

    public void issueInitialVerification(User user) {
        issue(user, AccountTokenPurpose.EMAIL_VERIFICATION, verificationValidity);
    }

    @Transactional
    public void resendVerification(String email) {
        userRepository.findByEmailIgnoreCase(email.trim())
                .filter(User::isActive)
                .filter(user -> !user.isEmailVerified())
                .filter(user -> !isCoolingDown(user, AccountTokenPurpose.EMAIL_VERIFICATION))
                .ifPresent(user -> issue(
                        user, AccountTokenPurpose.EMAIL_VERIFICATION, verificationValidity));
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        LocalDateTime now = now();
        AccountToken token = requireUsable(
                rawToken, AccountTokenPurpose.EMAIL_VERIFICATION, now);
        token.getUser().verifyEmail(now);
        token.markUsed(now);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmailIgnoreCase(email.trim())
                .filter(User::isActive)
                .filter(user -> !isCoolingDown(user, AccountTokenPurpose.PASSWORD_RESET))
                .ifPresent(user -> issue(user, AccountTokenPurpose.PASSWORD_RESET, resetValidity));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        validatePasswordLength(request.password());

        LocalDateTime now = now();
        AccountToken token = requireUsable(
                request.token(), AccountTokenPurpose.PASSWORD_RESET, now);
        User user = token.getUser();
        user.changePassword(passwordEncoder.encode(request.password()));
        token.markUsed(now);
        deleteSessions(user.getUsername());
    }

    @Transactional
    @Scheduled(cron = "${app.account-token.cleanup-cron:0 0 3 * * *}")
    public void cleanupOldTokens() {
        tokenRepository.deleteExpiredOrUsedBefore(now().minus(cleanupRetention));
    }

    private void issue(User user, AccountTokenPurpose purpose, Duration validity) {
        LocalDateTime now = now();
        tokenRepository.invalidateUnused(user, purpose, now);

        String rawToken = generateRawToken();
        tokenRepository.save(new AccountToken(
                user, purpose, hash(rawToken), now.plus(validity), now));
        eventPublisher.publishEvent(new AccountMailEvent(
                purpose, user.getEmail(), user.getUsername(), rawToken));
    }

    private AccountToken requireUsable(
            String rawToken, AccountTokenPurpose purpose, LocalDateTime now) {
        AccountToken token = tokenRepository.findByTokenHashAndPurpose(hash(rawToken), purpose)
                .orElseThrow(InvalidAccountTokenException::invalidOrExpired);
        if (token.isUsed()) {
            throw InvalidAccountTokenException.alreadyUsed();
        }
        if (token.isExpiredAt(now) || !token.getUser().isActive()) {
            throw InvalidAccountTokenException.invalidOrExpired();
        }
        return token;
    }

    private boolean isCoolingDown(User user, AccountTokenPurpose purpose) {
        LocalDateTime cutoff = now().minus(requestCooldown);
        return tokenRepository.findFirstByUserAndPurposeOrderByCreatedAtDesc(user, purpose)
                .map(AccountToken::getCreatedAt)
                .filter(createdAt -> createdAt.isAfter(cutoff))
                .isPresent();
    }

    private void deleteSessions(String username) {
        sessionRepository.findByPrincipalName(username)
                .keySet()
                .forEach(sessionRepository::deleteById);
    }

    private void validatePasswordLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES) {
            throw new InvalidAccountDataException("Wachtwoord mag maximaal 72 bytes bevatten");
        }
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is niet beschikbaar", exception);
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }
}
