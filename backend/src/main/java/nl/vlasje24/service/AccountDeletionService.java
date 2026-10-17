package nl.vlasje24.service;

import lombok.RequiredArgsConstructor;
import nl.vlasje24.domain.Comment;
import nl.vlasje24.domain.User;
import nl.vlasje24.exception.InvalidCredentialsException;
import nl.vlasje24.repository.AccountTokenRepository;
import nl.vlasje24.repository.CommentRepository;
import nl.vlasje24.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountDeletionService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final AccountTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Transactional
    public void deleteAccount(Integer userId, String password) {
        User user = userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(InvalidCredentialsException::new);
        if (password.getBytes(StandardCharsets.UTF_8).length > 72
                || !passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String username = user.getUsername();
        commentRepository.findAllByAuthor(user).forEach(Comment::removeForDeletedAccount);
        tokenRepository.deleteAllByUser(user);
        deleteSessions(username);
        user.deleteAccount(
                "verwijderd-" + user.getUserId() + "-" + UUID.randomUUID(),
                passwordEncoder.encode(UUID.randomUUID().toString()));
    }

    private void deleteSessions(String username) {
        sessionRepository.findByPrincipalName(username)
                .keySet()
                .forEach(sessionRepository::deleteById);
    }
}
