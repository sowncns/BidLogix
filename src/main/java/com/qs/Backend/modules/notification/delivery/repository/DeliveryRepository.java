package com.qs.Backend.modules.notification.delivery.repository;

import com.qs.Backend.modules.notification.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}
