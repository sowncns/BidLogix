package com.qs.Backend.modules.crm.customer.service;

import com.qs.Backend.modules.crm.customer.dto.CustomerCreateRequest;
import com.qs.Backend.modules.crm.customer.dto.CustomerResponse;
import com.qs.Backend.modules.crm.customer.dto.CustomerUpdateRequest;
import com.qs.Backend.modules.crm.customer.entity.Customer;
import com.qs.Backend.modules.crm.customer.repository.CustomerRepository;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse createCustomer(CustomerCreateRequest request) {
        if (customerRepository.existsByCode(request.getCode())) {
            throw new AppException("Customer code already exists", HttpStatus.CONFLICT, "CRM_CUSTOMER_CODE_EXISTS");
        }
        Customer customer = new Customer();
        customer.setCode(request.getCode());
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        return toResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(Long id) {
        return toResponse(findActiveOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> listCustomers(Pageable pageable) {
        return customerRepository.findByActiveTrue(pageable).map(this::toResponse);
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerUpdateRequest request) {
        Customer customer = findActiveOrThrow(id);
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setUpdatedAt(Instant.now());
        return toResponse(customer);
    }

    @Transactional
    public void deactivateCustomer(Long id) {
        Customer customer = findActiveOrThrow(id);
        customer.setActive(false);
        customer.setUpdatedAt(Instant.now());
    }

    private Customer findActiveOrThrow(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException("Customer not found", HttpStatus.NOT_FOUND, "CRM_CUSTOMER_NOT_FOUND"));
        if (!customer.isActive()) {
            throw new AppException("Customer not found", HttpStatus.NOT_FOUND, "CRM_CUSTOMER_NOT_FOUND");
        }
        return customer;
    }

    private CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .code(customer.getCode())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .active(customer.isActive())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
