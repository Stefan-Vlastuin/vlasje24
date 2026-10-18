package nl.vlasje24.service;

import nl.vlasje24.domain.AccountToken;
import nl.vlasje24.domain.AccountTokenPurpose;
import nl.vlasje24.domain.User;
import nl.vlasje24.dto.ResetPasswordRequestDto;
import nl.vlasje24.exception.InvalidAccountTokenException;
import nl.vlasje24.repository.AccountTokenRepository;
import nl.vlasje24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-07T12:00:00Z");

    @Mock AccountTokenRepository tokenRepository;
    @Mock UserRepository userRepository;
    @Mock FindByIndexNameSessionRepository<Session> sessionRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AccountTokenService service;

    @BeforeEach
    void setUp() {
        service = new AccountTokenService(
                tokenRepository,
                userRepository,
                passwordEncoder,
                sessionRepository,
                eventPublisher,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofHours(24),
                Duration.ofMinutes(60),
                Duration.ofMinutes(1),
                Duration.ofDays(7));
    }

    @Test
    void issueVerification_storesOnlyHashAndPublishesRawTokenForMail() {
        User user = new User("member", "member@example.nl", "hash");

        service.issueInitialVerification(user);

        ArgumentCaptor<AccountToken> tokenCaptor = ArgumentCaptor.forClass(AccountToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        verify(tokenRepository).invalidateUnused(
                eq(user), eq(AccountTokenPurpose.EMAIL_VERIFICATION), any(LocalDateTime.class));
        AccountToken saved = tokenCaptor.getValue();
        assertThat(saved.getPurpose()).isEqualTo(AccountTokenPurpose.EMAIL_VERIFICATION);
        assertThat(saved.getTokenHash()).hasSize(64);
        assertThat(saved.getExpiresAt()).isEqualTo(LocalDateTime.ofInstant(
                NOW.plus(Duration.ofHours(24)), ZoneOffset.UTC));

        ArgumentCaptor<AccountMailEvent> eventCaptor = ArgumentCaptor.forClass(AccountMailEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        AccountMailEvent event = eventCaptor.getValue();
        assertThat(event.rawToken()).hasSize(43);
        assertThat(saved.getTokenHash()).isEqualTo(hash(event.rawToken()));
        assertThat(saved.getTokenHash()).doesNotContain(event.rawToken());
    }

    @Test
    void verifyEmail_marksTokenUsedAndUserVerified() {
        User user = new User("member", "member@example.nl", "hash");
        AccountToken token = token(user, AccountTokenPurpose.EMAIL_VERIFICATION,
                NOW.plusSeconds(60));
        when(tokenRepository.findByTokenHashAndPurpose(
                hash("raw-token"), AccountTokenPurpose.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(token));

        service.verifyEmail("raw-token");

        assertThat(user.isEmailVerified()).isTrue();
        assertThat(token.isUsed()).isTrue();
    }

    @Test
    void verifyEmail_rejectsExpiredAndUsedTokens() {
        User user = new User("member", "member@example.nl", "hash");
        AccountToken expired = token(user, AccountTokenPurpose.EMAIL_VERIFICATION,
                NOW.minusSeconds(1));
        when(tokenRepository.findByTokenHashAndPurpose(anyString(), any()))
                .thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.verifyEmail("expired"))
                .isInstanceOf(InvalidAccountTokenException.class)
                .hasMessage("De link is ongeldig of verlopen");

        AccountToken used = token(user, AccountTokenPurpose.EMAIL_VERIFICATION,
                NOW.plusSeconds(60));
        used.markUsed(LocalDateTime.ofInstant(NOW.minusSeconds(1), ZoneOffset.UTC));
        when(tokenRepository.findByTokenHashAndPurpose(anyString(), any()))
                .thenReturn(Optional.of(used));

        assertThatThrownBy(() -> service.verifyEmail("used"))
                .isInstanceOf(InvalidAccountTokenException.class)
                .hasMessage("De link is al gebruikt");
    }

    @Test
    void forgotPassword_unknownEmailHasNoObservableSideEffect() {
        when(userRepository.findByEmailIgnoreCase("unknown@example.nl"))
                .thenReturn(Optional.empty());

        service.requestPasswordReset("unknown@example.nl");

        verifyNoInteractions(tokenRepository, eventPublisher);
    }

    @Test
    void requestPasswordReset_replacesPreviousUnusedToken() {
        User user = new User("member", "member@example.nl", "hash");
        AccountToken previousToken = new AccountToken(
                user,
                AccountTokenPurpose.PASSWORD_RESET,
                "b".repeat(64),
                LocalDateTime.ofInstant(NOW.plus(Duration.ofMinutes(30)), ZoneOffset.UTC),
                LocalDateTime.ofInstant(NOW.minus(Duration.ofMinutes(2)), ZoneOffset.UTC));
        when(userRepository.findByEmailIgnoreCase("member@example.nl"))
                .thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserAndPurposeOrderByCreatedAtDesc(
                user, AccountTokenPurpose.PASSWORD_RESET))
                .thenReturn(Optional.of(previousToken));

        service.requestPasswordReset("member@example.nl");

        verify(tokenRepository).invalidateUnused(
                eq(user), eq(AccountTokenPurpose.PASSWORD_RESET), any(LocalDateTime.class));
        ArgumentCaptor<AccountToken> tokenCaptor = ArgumentCaptor.forClass(AccountToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getPurpose()).isEqualTo(AccountTokenPurpose.PASSWORD_RESET);
        verify(eventPublisher).publishEvent(any(AccountMailEvent.class));
    }

    @Test
    void resendVerification_duringCooldownDoesNotCreateOrSendAnotherToken() {
        User user = new User("member", "member@example.nl", "hash");
        AccountToken recent = token(user, AccountTokenPurpose.EMAIL_VERIFICATION,
                NOW.plus(Duration.ofHours(24)));
        when(userRepository.findByEmailIgnoreCase("member@example.nl"))
                .thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserAndPurposeOrderByCreatedAtDesc(
                user, AccountTokenPurpose.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(recent));

        service.resendVerification("member@example.nl");

        verify(tokenRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void resetPassword_changesHashUsesTokenAndDeletesEverySession() {
        User user = new User("member", "member@example.nl", "old-hash");
        AccountToken token = token(user, AccountTokenPurpose.PASSWORD_RESET,
                NOW.plusSeconds(60));
        when(tokenRepository.findByTokenHashAndPurpose(
                hash("reset-token"), AccountTokenPurpose.PASSWORD_RESET))
                .thenReturn(Optional.of(token));
        when(sessionRepository.findByPrincipalName("member"))
                .thenReturn(Map.of("session-1", mock(Session.class), "session-2", mock(Session.class)));

        service.resetPassword(new ResetPasswordRequestDto(
                "reset-token", "a-new-long-password"));

        assertThat(passwordEncoder.matches("a-new-long-password", user.getPassword())).isTrue();
        assertThat(token.isUsed()).isTrue();
        verify(sessionRepository).deleteById("session-1");
        verify(sessionRepository).deleteById("session-2");
    }

    @Test
    void resetPassword_rejectsExpiredAndUsedTokens() {
        User user = new User("member", "member@example.nl", "old-hash");
        AccountToken expired = token(user, AccountTokenPurpose.PASSWORD_RESET,
                NOW.minusSeconds(1));
        when(tokenRepository.findByTokenHashAndPurpose(
                hash("expired-reset"), AccountTokenPurpose.PASSWORD_RESET))
                .thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.resetPassword(new ResetPasswordRequestDto(
                "expired-reset", "a-new-long-password")))
                .isInstanceOf(InvalidAccountTokenException.class)
                .hasMessage("De link is ongeldig of verlopen");

        AccountToken used = token(user, AccountTokenPurpose.PASSWORD_RESET,
                NOW.plusSeconds(60));
        used.markUsed(LocalDateTime.ofInstant(NOW.minusSeconds(1), ZoneOffset.UTC));
        when(tokenRepository.findByTokenHashAndPurpose(
                hash("used-reset"), AccountTokenPurpose.PASSWORD_RESET))
                .thenReturn(Optional.of(used));

        assertThatThrownBy(() -> service.resetPassword(new ResetPasswordRequestDto(
                "used-reset", "a-new-long-password")))
                .isInstanceOf(InvalidAccountTokenException.class)
                .hasMessage("De link is al gebruikt");
        assertThat(user.getPassword()).isEqualTo("old-hash");
        verifyNoInteractions(sessionRepository);
    }

    private AccountToken token(User user, AccountTokenPurpose purpose, Instant expiresAt) {
        return new AccountToken(
                user,
                purpose,
                "a".repeat(64),
                LocalDateTime.ofInstant(expiresAt, ZoneOffset.UTC),
                LocalDateTime.ofInstant(NOW.minusSeconds(10), ZoneOffset.UTC));
    }

    private static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }
}
