package nl.vlasje24.dto;

import nl.vlasje24.domain.Comment;

import java.time.Instant;
import java.time.ZoneOffset;

public record CommentDto(
        Long commentId,
        Integer authorId,
        String username,
        String body,
        Instant createdAt,
        Instant updatedAt,
        boolean edited
) {
    public static CommentDto from(Comment comment) {
        Instant createdAt = comment.getCreatedAt().toInstant(ZoneOffset.UTC);
        Instant updatedAt = comment.getUpdatedAt().toInstant(ZoneOffset.UTC);
        return new CommentDto(
                comment.getCommentId(),
                comment.getAuthor().getUserId(),
                comment.getAuthor().getUsername(),
                comment.getBody(),
                createdAt,
                updatedAt,
                updatedAt.isAfter(createdAt));
    }
}
