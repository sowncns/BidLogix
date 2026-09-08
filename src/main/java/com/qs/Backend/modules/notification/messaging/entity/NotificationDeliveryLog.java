package com.qs.Backend.modules.notification.messaging.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_delivery_log")
@Getter
@Setter
public class NotificationDeliveryLog {
    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String channel;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String recipient;

    private String template;

    @Column(nullable = false)
    private String status;

    @Column(name = "error")
    private String error;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;
}
