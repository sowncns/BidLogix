package com.qs.Backend.modules.crm.customer.repository;

import com.qs.Backend.modules.crm.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCode(String code);

    boolean existsByCode(String code);

    Page<Customer> findByActiveTrue(Pageable pageable);
}
