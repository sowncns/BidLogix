package com.qs.Backend.modules.inventory.productitemlog.repository;

import com.qs.Backend.modules.inventory.productitemlog.entity.ProductItemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductItemLogRepository extends JpaRepository<ProductItemLog, UUID> {

    Page<ProductItemLog> findByProductItemIdOrderByOccurredAtDesc(UUID productItemId, Pageable pageable);
}
