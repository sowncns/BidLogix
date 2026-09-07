package com.qs.Backend.modules.crm.customerview.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "customer_views", uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id", "sales_id"}))
@Getter
@Setter
public class CustomerView {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "customer_id", nullable = false)
    private String customerId;
    @Column(name = "sales_id", nullable = false)
    private String salesId;
    @Column(name = "viewed_at", nullable = false)
    private Instant viewedAt = Instant.now();
}
