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

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(userRepository, passwordEncoder);
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
}
