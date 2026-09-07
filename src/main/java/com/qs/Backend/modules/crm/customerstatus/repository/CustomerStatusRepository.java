package com.qs.Backend.modules.crm.customerstatus.repository;

import com.qs.Backend.modules.crm.customerstatus.entity.CustomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerStatusRepository extends JpaRepository<CustomerStatus, String> {
    Optional<CustomerStatus> findByNameIgnoreCase(String name);
}
