package nl.vlasje24.dto;

import nl.vlasje24.domain.Comment;
import nl.vlasje24.domain.CommentStatus;

import java.time.Instant;
import java.time.ZoneOffset;

public record AdminCommentDto(
        Long commentId,
        Integer chartWeekId,
        Integer authorId,
        String username,
        String body,
        CommentStatus status,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
    public static AdminCommentDto from(Comment comment) {
        return new AdminCommentDto(
                comment.getCommentId(),
                comment.getChartWeek().getWeekId(),
                comment.getAuthor().getUserId(),
                comment.getAuthor().getUsername(),
                comment.getBody(),
                comment.getStatus(),
                comment.getCreatedAt().toInstant(ZoneOffset.UTC),
                comment.getUpdatedAt().toInstant(ZoneOffset.UTC),
                comment.getDeletedAt() == null
                        ? null
                        : comment.getDeletedAt().toInstant(ZoneOffset.UTC));
    }
}
