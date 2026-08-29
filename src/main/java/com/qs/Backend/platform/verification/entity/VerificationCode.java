package com.qs.Backend.platform.verification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "verification_codes")
@Getter
@Setter
public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String target; // email or phone

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String purpose; // e.g. "registration"

    @Column(name = "account_id")
    private Long accountId;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant consumedAt;

    public boolean isConsumed() {
        return consumedAt != null;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }
}
