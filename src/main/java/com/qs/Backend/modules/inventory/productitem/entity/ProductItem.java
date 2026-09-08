package com.qs.Backend.modules.inventory.productitem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "product_items")
@Getter
@Setter
public class ProductItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false)
    private Long productId;

    private String customerId;

    private Instant manufacturingDate;
    private Instant installationDate;
    private Instant warrantyExpiry;

    @Column(nullable = false, length = 50)
    private String status = "stock";

    private Instant activatedAt;
    private Long activatedBy;
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
