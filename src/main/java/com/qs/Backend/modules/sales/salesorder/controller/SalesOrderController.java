package com.qs.Backend.modules.sales.salesorder.controller;

import com.qs.Backend.modules.sales.salesorder.dto.SalesOrderCreateRequest;
import com.qs.Backend.modules.sales.salesorder.dto.SalesOrderResponse;
import com.qs.Backend.modules.sales.salesorder.service.SalesOrderService;
import com.qs.Backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @PostMapping
    public ApiResponse<SalesOrderResponse> create(@Valid @RequestBody SalesOrderCreateRequest request) {
        return ApiResponse.created(salesOrderService.createOrder(request), "Sales order created");
    }

    @GetMapping("/{id}")
    public ApiResponse<SalesOrderResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(salesOrderService.getOrder(id), null);
    }

    @GetMapping
    public ApiResponse<Page<SalesOrderResponse>> listByCustomer(@RequestParam Long customerId, Pageable pageable) {
        return ApiResponse.ok(salesOrderService.listOrdersByCustomer(customerId, pageable), null);
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<SalesOrderResponse> confirm(@PathVariable Long id) {
        return ApiResponse.ok(salesOrderService.confirmOrder(id), "Sales order confirmed");
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<SalesOrderResponse> cancel(@PathVariable Long id) {
        return ApiResponse.ok(salesOrderService.cancelOrder(id), "Sales order cancelled");
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<SalesOrderResponse> complete(@PathVariable Long id) {
        return ApiResponse.ok(salesOrderService.completeOrder(id), "Sales order completed");
    }
}
