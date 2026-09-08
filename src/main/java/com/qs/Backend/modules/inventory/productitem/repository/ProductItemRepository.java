package com.qs.Backend.modules.inventory.productitem.repository;

import com.qs.Backend.modules.inventory.productitem.entity.ProductItem;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductItemRepository extends JpaRepository<ProductItem, UUID>, JpaSpecificationExecutor<ProductItem> {

    Optional<ProductItem> findByCodeAndDeletedAtIsNull(String code);

    boolean existsByCodeAndDeletedAtIsNull(String code);
}
