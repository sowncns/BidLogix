package com.qs.Backend.platform.auth.repository;

import com.qs.Backend.platform.auth.entity.SSOTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SSOTicketRepository extends JpaRepository<SSOTicket, Long> {
    Optional<SSOTicket> findByTokenHash(String tokenHash);
}
