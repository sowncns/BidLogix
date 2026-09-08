package com.qs.Backend.platform.auth.repository;

import com.qs.Backend.platform.auth.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AuthUser, Long>, JpaSpecificationExecutor<AuthUser> {

    Optional<AuthUser> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM AuthUser u WHERE (u.username = :value OR u.email = :value OR u.phone = :value) AND u.active = true")
    Optional<AuthUser> findActiveByUsernameOrEmailOrPhone(@Param("value") String value);

    @Query("SELECT u FROM AuthUser u WHERE u.username = :value OR u.email = :value OR u.phone = :value")
    Optional<AuthUser> findAnyStatusByUsernameOrEmailOrPhone(@Param("value") String value);

    Optional<AuthUser> findByRefId(Long refId);
}
