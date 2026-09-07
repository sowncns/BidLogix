package com.qs.Backend.modules.crm.activity.service;

import com.qs.Backend.modules.crm.activity.dto.ActivityCreateRequest;
import com.qs.Backend.modules.crm.activity.dto.ActivityListResponse;
import com.qs.Backend.modules.crm.activity.dto.ActivityResponse;
import com.qs.Backend.modules.crm.activity.dto.ActivityUpdateRequest;
import com.qs.Backend.modules.crm.activity.entity.Activity;
import com.qs.Backend.modules.crm.activity.repository.ActivityRepository;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;

    @Transactional(readOnly = true)
    public ActivityListResponse listByCustomer(String customerId, int limit, int offset) {
        if (customerId == null || customerId.isBlank()) {
            throw invalid("activityable_id is required");
        }
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        var page = activityRepository.findByActivityableTypeAndActivityableId(
                "customer",
                customerId,
                PageRequest.of(offset / safeLimit, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return ActivityListResponse.builder()
                .items(page.map(this::toResponse).getContent())
                .limit(limit)
                .offset(Math.max(offset, 0))
                .build();
    }

    @Transactional(readOnly = true)
    public ActivityResponse get(String id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public ActivityResponse createForCustomer(String customerId, ActivityCreateRequest request, String salesId) {
        String normalizedSalesId = trim(salesId);
        if (normalizedSalesId == null || normalizedSalesId.isEmpty()) {
            normalizedSalesId = trim(request.getSalesId());
        }
        validateCreate(customerId, request, normalizedSalesId);

        Activity activity = new Activity();
        activity.setActivityableType("customer");
        activity.setActivityableId(customerId.trim());
        activity.setSalesId(normalizedSalesId);
        activity.setAction(request.getAction().trim());
        activity.setContent(request.getContent().trim());
        activity.setContactAt(request.getContactAt());
        return toResponse(activityRepository.save(activity));
    }

    @Transactional
    public ActivityResponse update(String id, ActivityUpdateRequest request) {
        Activity activity = findOrThrow(id);
        boolean hasUpdate = request.getAction() != null || request.getContent() != null || request.getContactAt() != null;
        if (!hasUpdate) {
            throw invalid("at least one field must be provided for update");
        }
        if (request.getAction() != null) {
            String action = trim(request.getAction());
            if (action == null || action.isEmpty()) throw invalid("action cannot be empty");
            activity.setAction(action);
        }
        if (request.getContent() != null) {
            String content = trim(request.getContent());
            if (content == null || content.isEmpty()) throw invalid("content cannot be empty");
            activity.setContent(content);
        }
        if (request.getContactAt() != null) activity.setContactAt(request.getContactAt());
        return toResponse(activity);
    }

    @Transactional
    public void delete(String id) {
        activityRepository.delete(findOrThrow(id));
    }

    private void validateCreate(String customerId, ActivityCreateRequest request, String salesId) {
        if (customerId == null || customerId.isBlank()) throw invalid("activityable_id is required");
        if (salesId == null || salesId.isBlank()) throw invalid("sales_id is required");
        if (request.getAction() == null || request.getAction().trim().isEmpty()) throw invalid("action is required");
        if (request.getContent() == null || request.getContent().trim().isEmpty()) throw invalid("content is required");
    }

    private Activity findOrThrow(String id) {
        if (id == null || id.isBlank()) throw invalid("id is required");
        return activityRepository.findById(id)
                .orElseThrow(() -> new AppException("activity not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));
    }

    private AppException invalid(String message) {
        return new AppException(message, HttpStatus.BAD_REQUEST, "INVALID_INPUT");
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private ActivityResponse toResponse(Activity activity) {
        return ActivityResponse.builder()
                .id(activity.getId())
                .activityableType(activity.getActivityableType())
                .activityableId(activity.getActivityableId())
                .salesId(activity.getSalesId())
                .salesName(activity.getAction())
                .action(activity.getAction())
                .content(activity.getContent())
                .contactAt(activity.getContactAt())
                .createdAt(activity.getCreatedAt())
                .images(List.of())
                .build();
    }
}
