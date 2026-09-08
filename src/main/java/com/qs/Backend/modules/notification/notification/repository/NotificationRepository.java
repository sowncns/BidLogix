package com.qs.Backend.modules.notification.notification.repository;

import com.qs.Backend.modules.notification.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotificationRepository extends JpaRepository<Notification, String>, JpaSpecificationExecutor<Notification> {
    long countByUserIdAndReadAtIsNull(String userId);
    long deleteByUserId(String userId);
}
