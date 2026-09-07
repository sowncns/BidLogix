package com.qs.Backend.modules.crm.metadata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CustomerMetadataResponse {
    @JsonProperty("customer_groups")
    private List<MetadataOption> customerGroups;
    @JsonProperty("customer_statuses")
    private List<MetadataOption> customerStatuses;
    @JsonProperty("business_fields")
    private List<MetadataOption> businessFields;
    private List<MetadataOption> organizations;
    private List<MetadataOption> users;
    @JsonProperty("assignable_users")
    private List<MetadataOption> assignableUsers;
}
