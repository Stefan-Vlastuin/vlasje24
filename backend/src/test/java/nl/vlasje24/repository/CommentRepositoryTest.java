package nl.vlasje24.repository;

import nl.vlasje24.domain.ChartWeek;
import nl.vlasje24.domain.Comment;
import nl.vlasje24.domain.CommentStatus;
import nl.vlasje24.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.autoconfigure.exclude=",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class CommentRepositoryTest {

    @Container
    static final MySQLContainer mysql = new MySQLContainer("mysql:8.0");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
    }

    @Autowired CommentRepository commentRepository;
    @Autowired ChartWeekRepository chartWeekRepository;
    @Autowired UserRepository userRepository;

    @Test
    void publicQueryExcludesSoftDeletedComments() {
        User author = userRepository.save(new User("member", "member@example.nl", "hash"));
        ChartWeek week = chartWeekRepository.save(new ChartWeek(LocalDate.of(2026, 10, 17)));
        Comment visible = commentRepository.save(new Comment(week, author, "Zichtbaar"));
        Comment deleted = new Comment(week, author, "Verwijderd");
        deleted.softDelete(false);
        commentRepository.saveAndFlush(deleted);

        var result = commentRepository.findByChartWeekWeekIdAndStatus(
                week.getWeekId(),
                CommentStatus.VISIBLE,
                PageRequest.of(0, 20, Sort.by(Sort.Order.desc("createdAt"))));

        assertThat(result.getContent())
                .extracting(Comment::getCommentId)
                .containsExactly(visible.getCommentId());
    }
}
