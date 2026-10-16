package nl.vlasje24.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "account_token")
@Getter
@NoArgsConstructor
public class AccountToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_token_id")
    private Long accountTokenId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountTokenPurpose purpose;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AccountToken(
            User user,
            AccountTokenPurpose purpose,
            String tokenHash,
            LocalDateTime expiresAt,
            LocalDateTime createdAt) {
        this.user = Objects.requireNonNull(user);
        this.purpose = Objects.requireNonNull(purpose);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public boolean isExpiredAt(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public void markUsed(LocalDateTime now) {
        usedAt = Objects.requireNonNull(now);
    }
}
