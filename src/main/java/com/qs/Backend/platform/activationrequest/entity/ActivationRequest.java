package com.qs.Backend.platform.activationrequest.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_item_activation_requests")
@Getter
@Setter
public class ActivationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String requestType = "activation";

    @Column(nullable = false)
    private UUID salesUserId;

    @Column(nullable = false)
    private UUID customerId;

    private UUID organizationId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String inputs;

    private Instant warrantyExpiry;

    @Column(nullable = false, length = 50)
    private String status = "pending";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
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
