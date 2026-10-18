package nl.vlasje24.security;

import nl.vlasje24.domain.User;
import nl.vlasje24.domain.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

public record AccountPrincipal(
        Integer userId,
        String username,
        String password,
        UserRole role,
        boolean emailVerified
) implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    public static AccountPrincipal from(User user) {
        return new AccountPrincipal(
                user.getUserId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                user.isEmailVerified());
    }

    public AccountPrincipal withoutPassword() {
        return new AccountPrincipal(userId, username, null, role, emailVerified);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
