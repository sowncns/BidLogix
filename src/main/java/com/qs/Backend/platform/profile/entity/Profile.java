package com.qs.Backend.platform.profile.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

// Display info split from AuthUser (auth identity) - one-to-one via userId, no FK
// (same decoupling convention as AccountOrganization).
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    private String firstName;

    private String lastName;

    private String avatarUrl;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();
}
