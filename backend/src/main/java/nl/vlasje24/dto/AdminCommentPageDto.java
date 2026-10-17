package nl.vlasje24.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record AdminCommentPageDto(
        List<AdminCommentDto> comments,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static AdminCommentPageDto from(Page<AdminCommentDto> comments) {
        return new AdminCommentPageDto(
                comments.getContent(),
                comments.getNumber(),
                comments.getSize(),
                comments.getTotalElements(),
                comments.getTotalPages());
    }
}
