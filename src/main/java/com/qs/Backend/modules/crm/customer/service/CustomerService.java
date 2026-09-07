package com.qs.Backend.modules.crm.customer.service;

import com.qs.Backend.modules.crm.customer.dto.CustomerCreateRequest;
import com.qs.Backend.modules.crm.customer.dto.CustomerFilterParams;
import com.qs.Backend.modules.crm.customer.dto.CustomerListResponse;
import com.qs.Backend.modules.crm.customer.dto.CustomerResponse;
import com.qs.Backend.modules.crm.customer.dto.CustomerUpdateRequest;
import com.qs.Backend.modules.crm.customer.entity.Customer;
import com.qs.Backend.modules.crm.customer.repository.CustomerRepository;
import com.qs.Backend.platform.logging.audit.service.AuditLogService;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private static final String CODE_PREFIX = "C";
    private static final Pattern CODE_SUFFIX_PATTERN = Pattern.compile("^C(\\d+)$");

    private final CustomerRepository customerRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public CustomerResponse createCustomer(CustomerCreateRequest request, Long performedBy) {
        CustomerRequestNormalizer.normalize(request);

        if (request.getCode() == null || request.getCode().isEmpty()) {
            request.setCode(generateCode());
        } else if (customerRepository.existsByCode(request.getCode())) {
            throw new AppException("Customer code already exists", HttpStatus.CONFLICT, "CRM_CUSTOMER_CODE_EXISTS");
        }

        if (request.getPhone() != null && !request.getPhone().isEmpty()
                && customerRepository.existsByPhoneAndDeletedAtIsNull(request.getPhone())) {
            throw new AppException("Phone already exists", HttpStatus.CONFLICT, "CRM_CUSTOMER_PHONE_EXISTS");
        }

        Customer customer = new Customer();
        customer.setOrganizationId(request.getOrganizationId());
        customer.setCode(request.getCode());
        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setCompanyName(request.getCompanyName());
        customer.setSource(request.getSource());
        customer.setStatusId(request.getStatusId());
        customer.setCustomerType(request.getCustomerType() == null || request.getCustomerType().isBlank() ? "official" : request.getCustomerType());
        customer.setSourceCampaignId(request.getSourceCampaignId());
        customer.setNotes(request.getNotes());
        customer.setAssignedSalesId(request.getAssignedSalesId());
        customer.setCreatedBy(performedBy == null ? null : String.valueOf(performedBy));
        customer.setMainPhone(request.getMainPhone());
        customer.setMainEmail(request.getMainEmail());
        customer.setWebsite(request.getWebsite());
        customer.setAddress(request.getAddress());
        customer.setGender(request.getGender());
        customer.setRegion(request.getRegion());
        customer.setGroupIds(request.getGroupIds());
        customer.setBusinessFieldIds(request.getBusinessFieldIds());
        customerRepository.save(customer);

        auditLogService.log(performedBy, null, "CUSTOMER_CREATE", "Customer", customer.getId(), null);

        return toResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(String id) {
        return toResponse(findActiveOrThrow(id));
    }

    @Transactional(readOnly = true)
    public CustomerListResponse listCustomers(CustomerFilterParams filters, int limit, int offset) {
        Specification<Customer> spec = toSpecification(filters);
        long total = customerRepository.count(spec);
        Page<Customer> page = customerRepository.findAll(spec, PageRequest.of(offset / Math.max(limit, 1), Math.max(limit, 1)));

        return CustomerListResponse.builder()
                .items(page.map(this::toResponse).getContent())
                .limit(limit)
                .offset(offset)
                .total(total)
                .build();
    }

    @Transactional
    public CustomerResponse updateCustomer(String id, CustomerUpdateRequest request, Long performedBy) {
        CustomerRequestNormalizer.normalize(request);
        Customer customer = findActiveOrThrow(id);

        if (request.getPhone() != null && !request.getPhone().isEmpty()
                && customerRepository.existsByPhoneAndIdNotAndDeletedAtIsNull(request.getPhone(), id)) {
            throw new AppException("Phone already exists", HttpStatus.CONFLICT, "CRM_CUSTOMER_PHONE_EXISTS");
        }

        if (request.getOrganizationId() != null) customer.setOrganizationId(request.getOrganizationId());
        if (request.getCode() != null) customer.setCode(request.getCode());
        if (request.getFullName() != null) customer.setFullName(request.getFullName());
        if (request.getEmail() != null) customer.setEmail(request.getEmail());
        if (request.getPhone() != null) customer.setPhone(request.getPhone());
        if (request.getCompanyName() != null) customer.setCompanyName(request.getCompanyName());
        if (request.getSource() != null) customer.setSource(request.getSource());
        if (request.getStatusId() != null) customer.setStatusId(request.getStatusId());
        if (request.getCustomerType() != null) customer.setCustomerType(request.getCustomerType());
        if (request.getSourceCampaignId() != null) customer.setSourceCampaignId(request.getSourceCampaignId());
        if (request.getNotes() != null) customer.setNotes(request.getNotes());
        if (request.getAssignedSalesId() != null) customer.setAssignedSalesId(request.getAssignedSalesId());
        if (request.getMainPhone() != null) customer.setMainPhone(request.getMainPhone());
        if (request.getMainEmail() != null) customer.setMainEmail(request.getMainEmail());
        if (request.getWebsite() != null) customer.setWebsite(request.getWebsite());
        if (request.getAddress() != null) customer.setAddress(request.getAddress());
        if (request.getGender() != null) customer.setGender(request.getGender());
        if (request.getRegion() != null) customer.setRegion(request.getRegion());
        if (request.getGroupIds() != null) customer.setGroupIds(request.getGroupIds());
        if (request.getBusinessFieldIds() != null) customer.setBusinessFieldIds(request.getBusinessFieldIds());
        customer.setUpdatedAt(Instant.now());

        auditLogService.log(performedBy, null, "CUSTOMER_UPDATE", "Customer", customer.getId(), null);

        return toResponse(customer);
    }

    @Transactional
    public void deactivateCustomer(String id, Long performedBy) {
        Customer customer = findActiveOrThrow(id);
        customer.setDeletedAt(Instant.now());
        customer.setUpdatedAt(Instant.now());

        auditLogService.log(performedBy, null, "CUSTOMER_DELETE", "Customer", customer.getId(), null);
    }

    // ---- Internal helpers ----

    private String generateCode() {
        return customerRepository.findTopByCodeStartingWithOrderByCodeDesc(CODE_PREFIX)
                .map(last -> {
                    Matcher matcher = CODE_SUFFIX_PATTERN.matcher(last.getCode());
                    if (!matcher.matches()) {
                        return String.format("C%06d", customerRepository.count() + 1);
                    }
                    long next = Long.parseLong(matcher.group(1)) + 1;
                    return String.format("C%06d", next);
                })
                .orElse("C000001");
    }

    private Specification<Customer> toSpecification(CustomerFilterParams filters) {
        return (root, query, cb) -> {
            var predicate = cb.isNull(root.get("deletedAt"));

            if (filters.organizationId() != null && !filters.organizationId().isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("organizationId"), filters.organizationId()));
            }
            if (filters.keyword() != null && !filters.keyword().isBlank()) {
                String like = "%" + filters.keyword().trim().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("fullName")), like),
                        cb.like(cb.lower(root.get("code")), like),
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(root.get("phone")), like),
                        cb.like(cb.lower(root.get("companyName")), like)
                ));
            }
            if (filters.statusId() != null && !filters.statusId().isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("statusId"), filters.statusId()));
            }
            if (filters.assignedSalesId() != null && !filters.assignedSalesId().isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("assignedSalesId"), filters.assignedSalesId()));
            }
            if (filters.createdAtFrom() != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createdAt"), filters.createdAtFrom()));
            }
            if (filters.createdAtTo() != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createdAt"), filters.createdAtTo()));
            }
            return predicate;
        };
    }

    private Customer findActiveOrThrow(String id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException("Customer not found", HttpStatus.NOT_FOUND, "CRM_CUSTOMER_NOT_FOUND"));
        if (customer.getDeletedAt() != null) {
            throw new AppException("Customer not found", HttpStatus.NOT_FOUND, "CRM_CUSTOMER_NOT_FOUND");
        }
        return customer;
    }

    private CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .organizationId(customer.getOrganizationId())
                .code(customer.getCode())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .companyName(customer.getCompanyName())
                .source(customer.getSource())
                .statusId(customer.getStatusId())
                .customerType(customer.getCustomerType())
                .sourceCampaignId(customer.getSourceCampaignId())
                .notes(customer.getNotes())
                .assignedSalesId(customer.getAssignedSalesId())
                .createdBy(customer.getCreatedBy())
                .mainPhone(customer.getMainPhone())
                .mainEmail(customer.getMainEmail())
                .website(customer.getWebsite())
                .address(customer.getAddress())
                .gender(customer.getGender())
                .region(customer.getRegion())
                .lastContactAt(customer.getLastContactAt())
                .groupIds(customer.getGroupIds())
                .businessFieldIds(customer.getBusinessFieldIds())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .deletedAt(customer.getDeletedAt())
                .build();
    }
}
