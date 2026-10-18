package nl.vlasje24.dto;

import nl.vlasje24.domain.User;
import nl.vlasje24.domain.UserRole;
import nl.vlasje24.domain.UserStatus;

import java.time.Instant;
import java.time.ZoneOffset;

public record AdminUserDto(
        Integer userId,
        String username,
        UserRole role,
        UserStatus status,
        boolean emailVerified,
        Instant createdAt
) {
    public static AdminUserDto from(User user) {
        return new AdminUserDto(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                user.getStatus(),
                user.isEmailVerified(),
                user.getCreatedAt().toInstant(ZoneOffset.UTC));
    }
}
