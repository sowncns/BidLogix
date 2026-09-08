package com.qs.Backend.modules.inventory.productitemlog.repository;

import com.qs.Backend.modules.inventory.productitemlog.entity.ProductItemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductItemLogRepository extends JpaRepository<ProductItemLog, Long> {

    Page<ProductItemLog> findByProductItemIdOrderByOccurredAtDesc(Long productItemId, Pageable pageable);
}
