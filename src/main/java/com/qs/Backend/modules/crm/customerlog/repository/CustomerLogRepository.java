package com.qs.Backend.modules.crm.customerlog.repository;

import com.qs.Backend.modules.crm.customerlog.entity.CustomerLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerLogRepository extends JpaRepository<CustomerLog, String> {
    Page<CustomerLog> findByCustomerId(String customerId, Pageable pageable);
}
