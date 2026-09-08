package com.qs.Backend.modules.notification.messaging.repository;

import com.qs.Backend.modules.notification.messaging.entity.NotificationDeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationDeliveryLogRepository extends JpaRepository<NotificationDeliveryLog, UUID> {
}
