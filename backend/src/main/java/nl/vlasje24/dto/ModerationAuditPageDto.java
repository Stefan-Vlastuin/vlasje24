package nl.vlasje24.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record ModerationAuditPageDto(
        List<ModerationAuditDto> actions,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static ModerationAuditPageDto from(Page<ModerationAuditDto> actions) {
        return new ModerationAuditPageDto(
                actions.getContent(),
                actions.getNumber(),
                actions.getSize(),
                actions.getTotalElements(),
                actions.getTotalPages());
    }
}
