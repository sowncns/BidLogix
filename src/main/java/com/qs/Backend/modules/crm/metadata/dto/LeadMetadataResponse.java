package com.qs.Backend.modules.crm.metadata.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LeadMetadataResponse {
    private List<MetadataOption> organizations;
    private List<MetadataOption> users;
}
