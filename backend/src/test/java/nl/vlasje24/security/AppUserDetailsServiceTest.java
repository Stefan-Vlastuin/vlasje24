package nl.vlasje24.security;

import nl.vlasje24.domain.User;
import nl.vlasje24.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock UserRepository userRepository;

    @Test
    void loadUserByUsername_acceptsEmailCaseInsensitively() {
        User user = new User("member", "member@example.nl", "hash");
        when(userRepository.findByEmailIgnoreCase("MEMBER@EXAMPLE.NL")).thenReturn(Optional.of(user));

        var details = new AppUserDetailsService(userRepository)
                .loadUserByUsername("  MEMBER@EXAMPLE.NL  ");

        assertThat(details.getUsername()).isEqualTo("member");
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_rejectsSuspendedAccount() {
        User user = mock(User.class);
        when(user.isActive()).thenReturn(false);
        when(userRepository.findByUsername("member")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> new AppUserDetailsService(userRepository)
                .loadUserByUsername("member"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
