package com.qs.Backend.modules.crm.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CustomerResponse {
    private String id;
    private String organizationId;
    private String code;
    private String fullName;
    private String email;
    private String phone;
    private String companyName;
    private String source;
    private String statusId;
    private String customerType;
    private String sourceCampaignId;
    private String notes;
    private String assignedSalesId;
    private String createdBy;
    private String createdByName;
    private String mainPhone;
    private String mainEmail;
    private String website;
    private String address;
    private String gender;
    private String region;
    private Instant lastContactAt;
    private Boolean isNew;
    private List<String> groupIds;
    private List<String> businessFieldIds;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
}
