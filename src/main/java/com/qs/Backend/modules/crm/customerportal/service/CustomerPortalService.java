package com.qs.Backend.modules.crm.customerportal.service;

import com.qs.Backend.modules.crm.customerportal.dto.PortalListResponse;
import com.qs.Backend.modules.crm.customerportal.dto.PortalServiceRequestCreateRequest;
import com.qs.Backend.modules.crm.customerportal.dto.PortalServiceRequestResponse;
import com.qs.Backend.modules.crm.customerportal.dto.PortalWarrantySummaryResponse;
import com.qs.Backend.shared.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerPortalService {

    public PortalListResponse<Object> productItems() {
        return PortalListResponse.builder().items(List.of()).build();
    }

    public Object productItem(String id) {
        throw new AppException("product item not found", HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    public PortalWarrantySummaryResponse warranties() {
        return PortalWarrantySummaryResponse.builder()
                .totalProductItems(0)
                .activeWarranties(0)
                .expiringSoon(0)
                .expired(0)
                .build();
    }

    public PortalListResponse<PortalServiceRequestResponse> serviceRequests() {
        return PortalListResponse.<PortalServiceRequestResponse>builder().items(List.of()).build();
    }

    public PortalServiceRequestResponse serviceRequest(String id) {
        throw new AppException("service request not found", HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    public PortalServiceRequestResponse createServiceRequest(PortalServiceRequestCreateRequest request) {
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new AppException("description is required", HttpStatus.BAD_REQUEST, "INVALID_INPUT");
        }
        return PortalServiceRequestResponse.builder()
                .id(UUID.randomUUID().toString())
                .productItemId(request.getProductItemId())
                .type(request.getType())
                .serviceType(request.getServiceType())
                .priority(request.getPriority())
                .description(request.getDescription().trim())
                .status("open")
                .serviceLogs(List.of())
                .attachments(List.of())
                .createdAt(Instant.now())
                .build();
    }
}
