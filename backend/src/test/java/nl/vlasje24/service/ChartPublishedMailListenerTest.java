package nl.vlasje24.service;

import nl.vlasje24.domain.User;
import nl.vlasje24.domain.UserStatus;
import nl.vlasje24.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChartPublishedMailListenerTest {

    @Mock UserRepository userRepository;
    @Mock MailService mailService;

    @Test
    void sendsEveryEligibleRecipientAndContinuesAfterFailure() {
        User first = subscribedUser(1, "first", "first@example.nl");
        User second = subscribedUser(2, "second", "second@example.nl");
        when(userRepository
                .findAllByStatusAndEmailVerifiedAtIsNotNullAndChartNotificationsEnabledTrue(
                        UserStatus.ACTIVE))
                .thenReturn(List.of(first, second));
        doThrow(new IllegalStateException("SMTP unavailable"))
                .when(mailService).sendChartPublished(
                        "first@example.nl", "first", 12, LocalDate.of(2026, 10, 18));

        new ChartPublishedMailListener(userRepository, mailService)
                .send(new ChartPublishedEvent(12, LocalDate.of(2026, 10, 18)));

        verify(mailService).sendChartPublished(
                "first@example.nl", "first", 12, LocalDate.of(2026, 10, 18));
        verify(mailService).sendChartPublished(
                "second@example.nl", "second", 12, LocalDate.of(2026, 10, 18));
    }

    private static User subscribedUser(int id, String username, String email) {
        User user = new User(username, email, "hash", true);
        user.verifyEmail(LocalDateTime.now());
        ReflectionTestUtils.setField(user, "userId", id);
        return user;
    }
}
