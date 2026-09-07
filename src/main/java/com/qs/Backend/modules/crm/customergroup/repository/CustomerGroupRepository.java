package com.qs.Backend.modules.crm.customergroup.repository;

import com.qs.Backend.modules.crm.customergroup.entity.CustomerGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerGroupRepository extends JpaRepository<CustomerGroup, String> {
}
