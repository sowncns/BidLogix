package com.qs.Backend.modules.sales.salesorder.service;

import com.qs.Backend.modules.sales.salesorder.dto.SalesOrderCreateRequest;
import com.qs.Backend.modules.sales.salesorder.dto.SalesOrderResponse;
import com.qs.Backend.modules.sales.salesorder.entity.SalesOrder;
import com.qs.Backend.modules.sales.salesorder.entity.SalesOrderStatus;
import com.qs.Backend.modules.sales.salesorder.repository.SalesOrderRepository;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;

    @Transactional
    public SalesOrderResponse createOrder(SalesOrderCreateRequest request) {
        SalesOrder order = new SalesOrder();
        order.setCode(generateOrderCode());
        order.setCustomerId(request.getCustomerId());
        order.setTotalAmount(request.getTotalAmount());
        return toResponse(salesOrderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public SalesOrderResponse getOrder(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<SalesOrderResponse> listOrdersByCustomer(Long customerId, Pageable pageable) {
        return salesOrderRepository.findByCustomerId(customerId, pageable).map(this::toResponse);
    }

    @Transactional
    public SalesOrderResponse confirmOrder(Long id) {
        SalesOrder order = findOrThrow(id);
        requireStatus(order, SalesOrderStatus.DRAFT);
        order.setStatus(SalesOrderStatus.CONFIRMED);
        order.setUpdatedAt(Instant.now());
        return toResponse(order);
    }

    @Transactional
    public SalesOrderResponse cancelOrder(Long id) {
        SalesOrder order = findOrThrow(id);
        if (order.getStatus() == SalesOrderStatus.COMPLETED) {
            throw new AppException("Completed order cannot be cancelled", HttpStatus.CONFLICT, "SALES_ORDER_ALREADY_COMPLETED");
        }
        order.setStatus(SalesOrderStatus.CANCELLED);
        order.setUpdatedAt(Instant.now());
        return toResponse(order);
    }

    @Transactional
    public SalesOrderResponse completeOrder(Long id) {
        SalesOrder order = findOrThrow(id);
        requireStatus(order, SalesOrderStatus.CONFIRMED);
        order.setStatus(SalesOrderStatus.COMPLETED);
        order.setUpdatedAt(Instant.now());
        return toResponse(order);
    }

    private void requireStatus(SalesOrder order, SalesOrderStatus expected) {
        if (order.getStatus() != expected) {
            throw new AppException(
                    "Order must be in status " + expected + " (current: " + order.getStatus() + ")",
                    HttpStatus.CONFLICT, "SALES_ORDER_INVALID_STATUS");
        }
    }

    private SalesOrder findOrThrow(Long id) {
        return salesOrderRepository.findById(id)
                .orElseThrow(() -> new AppException("Sales order not found", HttpStatus.NOT_FOUND, "SALES_ORDER_NOT_FOUND"));
    }

    private String generateOrderCode() {
        return "SO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private SalesOrderResponse toResponse(SalesOrder order) {
        return SalesOrderResponse.builder()
                .id(order.getId())
                .code(order.getCode())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
