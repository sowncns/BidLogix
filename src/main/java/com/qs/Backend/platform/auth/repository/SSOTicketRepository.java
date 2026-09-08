package com.qs.Backend.platform.auth.repository;

import com.qs.Backend.platform.auth.entity.SSOTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SSOTicketRepository extends JpaRepository<SSOTicket, UUID> {
    Optional<SSOTicket> findByTokenHash(String tokenHash);
}
