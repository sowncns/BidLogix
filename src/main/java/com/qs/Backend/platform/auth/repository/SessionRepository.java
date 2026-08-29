package com.qs.Backend.platform.auth.repository;

import com.qs.Backend.platform.auth.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByRefreshTokenHash(String refreshTokenHash);

    Optional<Session> findByAccessTokenJti(String accessTokenJti);

    @Modifying
    @Query("UPDATE Session s SET s.revokedAt = :now WHERE s.account.id = :accountId AND s.revokedAt IS NULL")
    void revokeAllForAccount(@Param("accountId") Long accountId, @Param("now") Instant now);
}
