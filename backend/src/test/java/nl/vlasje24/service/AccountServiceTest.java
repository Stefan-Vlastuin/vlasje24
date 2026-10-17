package nl.vlasje24.service;

import nl.vlasje24.domain.User;
import nl.vlasje24.domain.UserRole;
import nl.vlasje24.dto.RegisterRequestDto;
import nl.vlasje24.exception.AccountAlreadyExistsException;
import nl.vlasje24.exception.InvalidAccountDataException;
import nl.vlasje24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock UserRepository userRepository;
    @Mock AccountTokenService accountTokenService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(userRepository, passwordEncoder, accountTokenService);
    }

    @Test
    void register_hashesPasswordAndCreatesUnverifiedUser() {
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = accountService.register(new RegisterRequestDto(
                "  member  ", "  Member@Example.nl  ", "a-long-password"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("member");
        assertThat(saved.getEmail()).isEqualTo("member@example.nl");
        assertThat(passwordEncoder.matches("a-long-password", saved.getPassword())).isTrue();
        assertThat(saved.getRole()).isEqualTo(UserRole.USER);
        assertThat(result.emailVerified()).isFalse();
        verify(accountTokenService).issueInitialVerification(saved);
    }

    @Test
    void register_existingUsernameIsRejected() {
        when(userRepository.existsByUsername("member")).thenReturn(true);

        assertThatThrownBy(() -> accountService.register(new RegisterRequestDto(
                "member", "member@example.nl", "a-long-password")))
                .isInstanceOf(AccountAlreadyExistsException.class);

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void register_passwordOver72Utf8BytesIsRejected() {
        String password = "é".repeat(37);

        assertThatThrownBy(() -> accountService.register(new RegisterRequestDto(
                "member", "member@example.nl", password)))
                .isInstanceOf(InvalidAccountDataException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void getAccount_returnsFreshVerificationState() {
        User user = mock(User.class);
        when(user.isActive()).thenReturn(true);
        when(user.getUserId()).thenReturn(7);
        when(user.getUsername()).thenReturn("member");
        when(user.getRole()).thenReturn(UserRole.USER);
        when(user.isEmailVerified()).thenReturn(true);
        when(userRepository.findById(7)).thenReturn(java.util.Optional.of(user));

        var result = accountService.getAccount(7);

        assertThat(result.userId()).isEqualTo(7);
        assertThat(result.emailVerified()).isTrue();
    }
}
