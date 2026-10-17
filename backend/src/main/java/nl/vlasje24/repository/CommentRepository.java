package nl.vlasje24.repository;

import nl.vlasje24.domain.Comment;
import nl.vlasje24.domain.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = "author")
    Page<Comment> findByChartWeekWeekIdAndStatus(
            Integer chartWeekId,
            CommentStatus status,
            Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"author", "chartWeek"})
    Optional<Comment> findById(Long commentId);
}
