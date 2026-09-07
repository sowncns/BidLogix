package com.qs.Backend.modules.crm.customerview.repository;

import com.qs.Backend.modules.crm.customerview.entity.CustomerView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerViewRepository extends JpaRepository<CustomerView, String> {
    Optional<CustomerView> findByCustomerIdAndSalesId(String customerId, String salesId);
}
