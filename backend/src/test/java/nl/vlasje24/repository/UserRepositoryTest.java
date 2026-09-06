package nl.vlasje24.repository;

import nl.vlasje24.domain.User;
import nl.vlasje24.domain.UserRole;
import nl.vlasje24.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.autoconfigure.exclude=",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class UserRepositoryTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
    }

    @Autowired
    UserRepository userRepository;

    @Test
    void save_regularUser_normalizesFieldsAndDoesNotGrantAdminRole() {
        User user = userRepository.saveAndFlush(
                new User("  member  ", "  Member@Example.nl  ", "$2a$10$test-hash"));

        assertThat(user.getUsername()).isEqualTo("member");
        assertThat(user.getEmail()).isEqualTo("member@example.nl");
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(userRepository.findByEmailIgnoreCase("MEMBER@EXAMPLE.NL"))
                .contains(user);
    }

    @Test
    void save_duplicateUsername_isRejected() {
        userRepository.saveAndFlush(new User("member", "first@example.nl", "hash"));

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(new User("member", "second@example.nl", "hash")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_duplicateEmailWithDifferentCase_isRejected() {
        userRepository.saveAndFlush(new User("member-one", "member@example.nl", "hash"));

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(new User("member-two", "MEMBER@example.nl", "hash")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
