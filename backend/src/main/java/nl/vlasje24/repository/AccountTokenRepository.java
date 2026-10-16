package nl.vlasje24.repository;

import nl.vlasje24.domain.AccountToken;
import nl.vlasje24.domain.AccountTokenPurpose;
import nl.vlasje24.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AccountTokenRepository extends JpaRepository<AccountToken, Long> {

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<AccountToken> findByTokenHashAndPurpose(String tokenHash, AccountTokenPurpose purpose);

    Optional<AccountToken> findFirstByUserAndPurposeOrderByCreatedAtDesc(
            User user, AccountTokenPurpose purpose);

    @Modifying
    @Query("""
            update AccountToken token
            set token.usedAt = :usedAt
            where token.user = :user
              and token.purpose = :purpose
              and token.usedAt is null
            """)
    int invalidateUnused(
            @Param("user") User user,
            @Param("purpose") AccountTokenPurpose purpose,
            @Param("usedAt") LocalDateTime usedAt);

    @Modifying
    @Query("""
            delete from AccountToken token
            where token.expiresAt < :cutoff
               or (token.usedAt is not null and token.usedAt < :cutoff)
            """)
    int deleteExpiredOrUsedBefore(@Param("cutoff") LocalDateTime cutoff);
}
