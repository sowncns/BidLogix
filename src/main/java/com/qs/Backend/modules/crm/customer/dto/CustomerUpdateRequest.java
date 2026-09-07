package com.qs.Backend.modules.crm.customer.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CustomerUpdateRequest {

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
    private String mainPhone;
    private String mainEmail;
    private String website;
    private String address;
    private String gender;
    private String region;
    private List<String> groupIds;
    private List<String> businessFieldIds;
}
