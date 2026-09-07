package nl.vlasje24.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class UserAccountMigrationTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Test
    void migration_convertsLegacyAccountsAndUsesSafeDefaultsForNewAccounts() {
        var dataSource = new DriverManagerDataSource(
                mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());

        Flyway.configure()
                .dataSource(dataSource)
                .target(MigrationVersion.fromVersion("1"))
                .load()
                .migrate();

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update("INSERT INTO `user` (`username`, `password`) VALUES (?, ?)",
                "legacy-admin", "$2a$10$legacy-hash");

        Flyway.configure()
                .dataSource(dataSource)
                .load()
                .migrate();

        assertThat(jdbc.queryForObject(
                "SELECT `role` FROM `user` WHERE `username` = 'legacy-admin'", String.class))
                .isEqualTo("ADMIN");
        assertThat(jdbc.queryForObject(
                "SELECT `status` FROM `user` WHERE `username` = 'legacy-admin'", String.class))
                .isEqualTo("ACTIVE");
        assertThat(jdbc.queryForObject(
                "SELECT `created_at` IS NOT NULL FROM `user` WHERE `username` = 'legacy-admin'", Boolean.class))
                .isTrue();

        jdbc.update("INSERT INTO `user` (`username`, `password`, `email`) VALUES (?, ?, ?)",
                "regular-user", "$2a$10$user-hash", "user@example.nl");

        assertThat(jdbc.queryForObject(
                "SELECT `role` FROM `user` WHERE `username` = 'regular-user'", String.class))
                .isEqualTo("USER");
        assertThat(jdbc.queryForObject(
                "SELECT `status` FROM `user` WHERE `username` = 'regular-user'", String.class))
                .isEqualTo("ACTIVE");

        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                        "WHERE table_schema = DATABASE() AND table_name = 'SPRING_SESSION'", Integer.class))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                        "WHERE table_schema = DATABASE() AND table_name = 'SPRING_SESSION_ATTRIBUTES'", Integer.class))
                .isEqualTo(1);
    }
}
