package org.example.sumoney.repositories;

import org.example.sumoney.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Query("""
        select rt from RefreshToken rt
        where rt.tokenHash = :hash
          and rt.revoked = false
          and rt.expiresAt > :now
    """)
    Optional<RefreshToken> findActiveByHash(
            @Param("hash") String hash,
            @Param("now") Instant now
    );

    @Modifying
    @Transactional
    @Query("""
        update RefreshToken rt
        set rt.revoked = true,
            rt.lastUsedAt = :now
        where rt.user.id = :userId
          and rt.revoked = false
    """)
    int revokeAllByUserId(
            @Param("userId") Long userId,
            @Param("now") Instant now
    );
}