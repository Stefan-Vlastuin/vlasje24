package nl.vlasje24.service;

import nl.vlasje24.domain.Comment;
import nl.vlasje24.domain.User;
import nl.vlasje24.domain.UserStatus;
import nl.vlasje24.exception.InvalidCredentialsException;
import nl.vlasje24.repository.AccountTokenRepository;
import nl.vlasje24.repository.CommentRepository;
import nl.vlasje24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountDeletionServiceTest {

    @Mock UserRepository userRepository;
    @Mock CommentRepository commentRepository;
    @Mock AccountTokenRepository tokenRepository;
    @Mock FindByIndexNameSessionRepository<Session> sessionRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AccountDeletionService service;

    @BeforeEach
    void setUp() {
        service = new AccountDeletionService(
                userRepository, commentRepository, tokenRepository,
                passwordEncoder, sessionRepository);
    }

    @Test
    void deleteAccount_anonymizesContentAndEndsSessions() {
        User user = new User(
                "member", "member@example.nl", passwordEncoder.encode("a-long-password"), true);
        ReflectionTestUtils.setField(user, "userId", 42);
        Comment comment = mock(Comment.class);
        when(userRepository.findById(42)).thenReturn(Optional.of(user));
        when(commentRepository.findAllByAuthor(user)).thenReturn(List.of(comment));
        when(sessionRepository.findByPrincipalName("member")).thenReturn(Map.of("session-1", mock(Session.class)));

        service.deleteAccount(42, "a-long-password");

        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(user.getEmail()).isNull();
        assertThat(user.isChartNotificationsEnabled()).isFalse();
        assertThat(user.getUsername()).startsWith("verwijderd-42-");
        assertThat(passwordEncoder.matches("a-long-password", user.getPassword())).isFalse();
        verify(comment).removeForDeletedAccount();
        verify(tokenRepository).deleteAllByUser(user);
        verify(sessionRepository).deleteById("session-1");
    }

    @Test
    void deleteAccount_rejectsWrongPasswordWithoutChangingData() {
        User user = new User("member", "member@example.nl", passwordEncoder.encode("a-long-password"));
        when(userRepository.findById(42)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.deleteAccount(42, "wrong-password"))
                .isInstanceOf(InvalidCredentialsException.class);

        verifyNoInteractions(commentRepository, tokenRepository, sessionRepository);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
}
