package nl.vlasje24.security;

import lombok.RequiredArgsConstructor;
import nl.vlasje24.domain.User;
import nl.vlasje24.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        String normalizedIdentifier = identifier.trim();
        User user = (normalizedIdentifier.contains("@")
                ? userRepository.findByEmailIgnoreCase(normalizedIdentifier)
                : userRepository.findByUsername(normalizedIdentifier))
                .filter(User::isActive)
                .orElseThrow(() -> new UsernameNotFoundException("Account niet gevonden"));

        return AccountPrincipal.from(user);
    }
}
