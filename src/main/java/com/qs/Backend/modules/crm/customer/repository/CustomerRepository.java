package com.qs.Backend.modules.crm.customer.repository;

import com.qs.Backend.modules.crm.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByPhoneAndDeletedAtIsNull(String phone);

    boolean existsByPhoneAndIdNotAndDeletedAtIsNull(String phone, UUID id);

    Page<Customer> findByDeletedAtIsNull(Pageable pageable);

    Optional<Customer> findTopByCodeStartingWithOrderByCodeDesc(String prefix);
}
