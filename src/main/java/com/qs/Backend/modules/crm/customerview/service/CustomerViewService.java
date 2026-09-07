package com.qs.Backend.modules.crm.customerview.service;

import com.qs.Backend.modules.crm.customerview.entity.CustomerView;
import com.qs.Backend.modules.crm.customerview.repository.CustomerViewRepository;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CustomerViewService {
    private final CustomerViewRepository customerViewRepository;

    @Transactional
    public void recordView(String customerId, String salesId) {
        if (customerId == null || customerId.isBlank()) throw invalid("customer_id is required");
        if (salesId == null || salesId.isBlank()) throw invalid("sales_id is required");
        CustomerView view = customerViewRepository.findByCustomerIdAndSalesId(customerId.trim(), salesId.trim()).orElseGet(CustomerView::new);
        view.setCustomerId(customerId.trim());
        view.setSalesId(salesId.trim());
        view.setViewedAt(Instant.now());
        customerViewRepository.save(view);
    }

    private AppException invalid(String message) {
        return new AppException(message, HttpStatus.BAD_REQUEST, "INVALID_INPUT");
    }
}
