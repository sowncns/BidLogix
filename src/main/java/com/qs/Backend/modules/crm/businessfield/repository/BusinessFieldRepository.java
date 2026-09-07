package com.qs.Backend.modules.crm.businessfield.repository;

import com.qs.Backend.modules.crm.businessfield.entity.BusinessField;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessFieldRepository extends JpaRepository<BusinessField, String> {
}
