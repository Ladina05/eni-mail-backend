package mg.eni.mail.repository;

import mg.eni.mail.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(String tokenHash, Instant now);
    boolean existsByUserIdAndCreatedAtAfter(UUID userId, Instant since);

    @Modifying
    @Query("update PasswordResetToken token set token.usedAt = :now where token.user.id = :userId and token.usedAt is null")
    void invalidateUnusedByUserId(@Param("userId") UUID userId, @Param("now") Instant now);

    @Modifying
    @Query("update PasswordResetToken token set token.usedAt = :now where token.id = :id and token.usedAt is null and token.expiresAt > :now")
    int consume(@Param("id") UUID id, @Param("now") Instant now);
}
