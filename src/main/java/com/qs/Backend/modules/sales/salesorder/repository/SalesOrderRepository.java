package com.qs.Backend.modules.sales.salesorder.repository;

import com.qs.Backend.modules.sales.salesorder.entity.SalesOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Page<SalesOrder> findByCustomerId(Long customerId, Pageable pageable);

    boolean existsByCode(String code);
}
