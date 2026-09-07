package com.qs.Backend.modules.crm.campaign.service;

import com.qs.Backend.modules.crm.campaign.dto.CampaignCreateRequest;
import com.qs.Backend.modules.crm.campaign.dto.CampaignListResponse;
import com.qs.Backend.modules.crm.campaign.dto.CampaignResponse;
import com.qs.Backend.modules.crm.campaign.dto.CampaignUpdateRequest;
import com.qs.Backend.modules.crm.campaign.dto.PublicCampaignResponse;
import com.qs.Backend.modules.crm.campaign.dto.PublicCampaignRegisterRequest;
import com.qs.Backend.modules.crm.campaign.dto.PublicCampaignRegisterResponse;
import com.qs.Backend.modules.crm.campaign.entity.Campaign;
import com.qs.Backend.modules.crm.campaign.repository.CampaignRepository;
import com.qs.Backend.modules.crm.customer.dto.CustomerCreateRequest;
import com.qs.Backend.modules.crm.customer.dto.CustomerResponse;
import com.qs.Backend.modules.crm.customer.service.CustomerService;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CampaignService {
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9-]+$");
    private static final Set<String> FUNNEL_TYPES = Set.of("sales", "course");

    private final CampaignRepository campaignRepository;
    private final CustomerService customerService;

    @Transactional
    public CampaignResponse create(CampaignCreateRequest request, String createdBy) {
        validateCreate(request, createdBy);
        if (campaignRepository.existsBySlugAndDeletedAtIsNull(request.getSlug())) {
            throw new AppException("campaign slug already taken", HttpStatus.CONFLICT, "SLUG_TAKEN");
        }

        Campaign campaign = new Campaign();
        campaign.setName(request.getName().trim());
        campaign.setSlug(request.getSlug());
        campaign.setProductId(blankToNull(request.getProductId()));
        campaign.setBusinessFieldId(blankToNull(request.getBusinessFieldId()));
        campaign.setOrganizationId(blankToNull(request.getOrganizationId()));
        campaign.setAssignedPersonId(blankToNull(request.getAssignedPersonId()));
        campaign.setFunnelType(request.getFunnelType());
        campaign.setChannelType(blankToNull(request.getChannelType()));
        campaign.setStatus("active");
        campaign.setSlaHours(request.getSlaHours());
        campaign.setCreatedBy(createdBy);
        campaign.setStartAt(request.getStartAt());
        campaign.setEndAt(request.getEndAt());

        return toResponse(campaignRepository.save(campaign));
    }

    @Transactional(readOnly = true)
    public CampaignResponse get(String id) {
        return toResponse(findActiveOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PublicCampaignResponse getPublic(String slug) {
        Campaign campaign = campaignRepository.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new AppException("campaign not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        return PublicCampaignResponse.builder()
                .name(campaign.getName())
                .slug(campaign.getSlug())
                .funnelType(campaign.getFunnelType())
                .status(campaign.getStatus())
                .build();
    }

    @Transactional
    public PublicCampaignRegisterResponse registerPublic(String slug, PublicCampaignRegisterRequest request) {
        Campaign campaign = campaignRepository.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new AppException("campaign not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        if (!"active".equals(campaign.getStatus())) {
            throw new AppException("campaign is no longer accepting registrations", HttpStatus.BAD_REQUEST, "BAD_REQUEST");
        }

        CustomerCreateRequest customerRequest = new CustomerCreateRequest();
        customerRequest.setOrganizationId(campaign.getOrganizationId());
        customerRequest.setFullName(request.getFullName());
        customerRequest.setPhone(request.getPhone());
        customerRequest.setEmail(request.getEmail());
        customerRequest.setGender(request.getGender());
        customerRequest.setSource(campaign.getName());
        customerRequest.setCustomerType("course".equals(campaign.getFunnelType()) ? "student" : "lead");
        customerRequest.setSourceCampaignId(campaign.getId());
        customerRequest.setNotes(request.getNotes());
        if (request.getBusinessFieldId() != null && !request.getBusinessFieldId().isBlank()) {
            customerRequest.setBusinessFieldIds(List.of(request.getBusinessFieldId().trim()));
        }

        CustomerResponse customer = customerService.createCustomer(customerRequest, null);
        String type = "course".equals(campaign.getFunnelType()) ? "student" : "lead";
        return PublicCampaignRegisterResponse.builder()
                .type(type)
                .id(customer.getId())
                .build();
    }

    @Transactional(readOnly = true)
    public CampaignListResponse list(String status, String productId, int limit, int offset) {
        int safeLimit = Math.max(limit, 1);
        Specification<Campaign> spec = (root, query, cb) -> {
            var predicate = cb.isNull(root.get("deletedAt"));
            if (status != null && !status.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (productId != null && !productId.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("productId"), productId));
            }
            return predicate;
        };
        Page<Campaign> page = campaignRepository.findAll(
                spec,
                PageRequest.of(offset / safeLimit, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return CampaignListResponse.builder()
                .items(page.map(this::toResponse).getContent())
                .total(page.getTotalElements())
                .limit(limit)
                .offset(offset)
                .build();
    }

    @Transactional
    public CampaignResponse update(String id, CampaignUpdateRequest request) {
        Campaign campaign = findActiveOrThrow(id);
        if (request.getName() != null) campaign.setName(request.getName());
        if (request.getProductId() != null) campaign.setProductId(blankToNull(request.getProductId()));
        if (request.getBusinessFieldId() != null) campaign.setBusinessFieldId(blankToNull(request.getBusinessFieldId()));
        if (request.getAssignedPersonId() != null) campaign.setAssignedPersonId(blankToNull(request.getAssignedPersonId()));
        if (request.getChannelType() != null) campaign.setChannelType(blankToNull(request.getChannelType()));
        if (request.getSlaHours() != null) campaign.setSlaHours(request.getSlaHours());
        if (request.getStartAt() != null) campaign.setStartAt(request.getStartAt());
        if (request.getEndAt() != null) campaign.setEndAt(request.getEndAt());
        campaign.setUpdatedAt(Instant.now());
        return toResponse(campaign);
    }

    @Transactional
    public CampaignResponse end(String id) {
        Campaign campaign = findActiveOrThrow(id);
        campaign.setStatus("ended");
        campaign.setUpdatedAt(Instant.now());
        return toResponse(campaign);
    }

    private void validateCreate(CampaignCreateRequest request, String createdBy) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw invalid("name is required");
        }
        if (request.getSlug() == null || request.getSlug().isBlank()) {
            throw invalid("slug is required");
        }
        if (!SLUG_PATTERN.matcher(request.getSlug()).matches()) {
            throw invalid("slug must contain only lowercase letters, digits, and hyphens");
        }
        if (!FUNNEL_TYPES.contains(request.getFunnelType())) {
            throw new AppException("invalid funnel type", HttpStatus.BAD_REQUEST, "INVALID_INPUT");
        }
        if (request.getOrganizationId() == null || request.getOrganizationId().isBlank()) {
            throw invalid("organization_id is required");
        }
        if (createdBy == null || createdBy.isBlank()) {
            throw invalid("created_by is required");
        }
    }

    private Campaign findActiveOrThrow(String id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new AppException("campaign not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        if (campaign.getDeletedAt() != null) {
            throw new AppException("campaign not found", HttpStatus.NOT_FOUND, "NOT_FOUND");
        }
        return campaign;
    }

    private AppException invalid(String message) {
        return new AppException("invalid campaign: " + message, HttpStatus.BAD_REQUEST, "INVALID_INPUT");
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private CampaignResponse toResponse(Campaign campaign) {
        return CampaignResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .slug(campaign.getSlug())
                .productId(campaign.getProductId())
                .businessFieldId(campaign.getBusinessFieldId())
                .organizationId(campaign.getOrganizationId())
                .assignedPersonId(campaign.getAssignedPersonId())
                .funnelType(campaign.getFunnelType())
                .channelType(campaign.getChannelType())
                .status(campaign.getStatus())
                .slaHours(campaign.getSlaHours())
                .createdBy(campaign.getCreatedBy())
                .startAt(campaign.getStartAt())
                .endAt(campaign.getEndAt())
                .createdAt(campaign.getCreatedAt())
                .updatedAt(campaign.getUpdatedAt())
                .build();
    }
}
