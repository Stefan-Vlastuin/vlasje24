package nl.vlasje24.repository;

import nl.vlasje24.domain.Comment;
import nl.vlasje24.domain.CommentStatus;
import nl.vlasje24.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Override
    @EntityGraph(attributePaths = {"author", "chartWeek"})
    Page<Comment> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "author")
    Page<Comment> findByChartWeekWeekIdAndStatus(
            Integer chartWeekId,
            CommentStatus status,
            Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"author", "chartWeek"})
    Optional<Comment> findById(Long commentId);

    @EntityGraph(attributePaths = {"author", "chartWeek"})
    Page<Comment> findByStatusIn(Collection<CommentStatus> statuses, Pageable pageable);

    List<Comment> findAllByAuthor(User author);

    @Modifying
    @Query("delete from Comment comment where comment.status <> nl.vlasje24.domain.CommentStatus.VISIBLE and comment.deletedAt < :cutoff")
    int deleteSoftDeletedBefore(@Param("cutoff") LocalDateTime cutoff);
}
