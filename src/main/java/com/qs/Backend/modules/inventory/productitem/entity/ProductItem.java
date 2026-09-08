package com.qs.Backend.modules.inventory.productitem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_items")
@Getter
@Setter
public class ProductItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false)
    private UUID productId;

    private UUID customerId;

    private Instant manufacturingDate;
    private Instant installationDate;
    private Instant warrantyExpiry;

    @Column(nullable = false, length = 50)
    private String status = "stock";

    private Instant activatedAt;
    private UUID activatedBy;
    private String activationNotes;

    private Instant recalledAt;
    private String recallNotes;

    private Instant refurbishedAt;
    private String refurbishNotes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    private Instant deletedAt;
}
