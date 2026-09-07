package nl.vlasje24.service;

import lombok.RequiredArgsConstructor;
import nl.vlasje24.domain.User;
import nl.vlasje24.dto.AccountDto;
import nl.vlasje24.dto.RegisterRequestDto;
import nl.vlasje24.exception.AccountAlreadyExistsException;
import nl.vlasje24.exception.InvalidAccountDataException;
import nl.vlasje24.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AccountDto register(RegisterRequestDto request) {
        String username = request.username().trim();
        String email = request.email().trim();

        if (request.password().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES) {
            throw new InvalidAccountDataException("Wachtwoord mag maximaal 72 bytes bevatten");
        }
        if (userRepository.existsByUsername(username) || userRepository.existsByEmailIgnoreCase(email)) {
            throw new AccountAlreadyExistsException();
        }

        try {
            User user = userRepository.saveAndFlush(
                    new User(username, email, passwordEncoder.encode(request.password())));
            return AccountDto.from(user);
        } catch (DataIntegrityViolationException exception) {
            // A concurrent registration can pass the pre-check; the database remains authoritative.
            throw new AccountAlreadyExistsException();
        }
    }
}
