package nl.vlasje24.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chart_week_id", nullable = false)
    private ChartWeek chartWeek;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 1000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private CommentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Comment(ChartWeek chartWeek, User author, String body) {
        this.chartWeek = Objects.requireNonNull(chartWeek);
        this.author = Objects.requireNonNull(author);
        this.body = Objects.requireNonNull(body);
        this.status = CommentStatus.VISIBLE;
    }

    public void edit(String body) {
        this.body = Objects.requireNonNull(body);
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public void softDelete(boolean deletedByModerator) {
        status = deletedByModerator
                ? CommentStatus.DELETED_BY_MODERATOR
                : CommentStatus.DELETED_BY_AUTHOR;
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        deletedAt = now;
        updatedAt = now;
    }

    public boolean isVisible() {
        return status == CommentStatus.VISIBLE;
    }

    public void removeForDeletedAccount() {
        body = "[inhoud verwijderd]";
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (status == CommentStatus.VISIBLE) {
            status = CommentStatus.DELETED_BY_AUTHOR;
            deletedAt = now;
        }
        updatedAt = now;
    }

    @PrePersist
    void initializeTimestamps() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (status == null) {
            status = CommentStatus.VISIBLE;
        }
    }
}
