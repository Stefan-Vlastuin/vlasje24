package nl.vlasje24.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record CommentPageDto(
        List<CommentDto> comments,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static CommentPageDto from(Page<CommentDto> comments) {
        return new CommentPageDto(
                comments.getContent(),
                comments.getNumber(),
                comments.getSize(),
                comments.getTotalElements(),
                comments.getTotalPages());
    }
}
