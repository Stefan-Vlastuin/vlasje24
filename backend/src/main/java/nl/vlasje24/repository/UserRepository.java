package nl.vlasje24.repository;

import nl.vlasje24.domain.User;
import nl.vlasje24.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findAllByStatusAndEmailVerifiedAtIsNotNullAndChartNotificationsEnabledTrue(
            UserStatus status);

    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
}
