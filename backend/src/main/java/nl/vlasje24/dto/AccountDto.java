package nl.vlasje24.dto;

import nl.vlasje24.domain.User;
import nl.vlasje24.domain.UserRole;

public record AccountDto(
        Integer userId,
        String username,
        UserRole role,
        boolean emailVerified
) {
    public static AccountDto from(User user) {
        return new AccountDto(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                user.isEmailVerified());
    }
}
