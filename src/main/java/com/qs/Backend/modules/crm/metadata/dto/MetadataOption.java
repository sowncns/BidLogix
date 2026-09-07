package com.qs.Backend.modules.crm.metadata.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MetadataOption {
    private String id;
    private String name;
    private String code;
}
