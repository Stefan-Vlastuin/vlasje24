package nl.vlasje24.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record AdminUserPageDto(
        List<AdminUserDto> users,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static AdminUserPageDto from(Page<AdminUserDto> users) {
        return new AdminUserPageDto(
                users.getContent(),
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages());
    }
}
