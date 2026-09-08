package com.qs.Backend.platform.activationrequest.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activation_requests")
@Getter
@Setter
public class ActivationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String requestType = "activation";

    @Column(nullable = false)
    private UUID salesUserId;

    @Column(nullable = false)
    private UUID customerId;

    private String organizationId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String inputs;

    private Instant warrantyExpiry;

    @Column(nullable = false, length = 50)
    private String status = "pending";

    @Column(columnDefinition = "TEXT")
    private String result;

    private String rejectReason;

    @Column(nullable = false, updatable = false)
    private Instant submittedAt = Instant.now();

    private UUID reviewedBy;
    private Instant reviewedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();
}
