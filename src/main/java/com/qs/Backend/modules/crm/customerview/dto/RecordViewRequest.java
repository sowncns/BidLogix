package com.qs.Backend.modules.crm.customerview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecordViewRequest {
    @JsonProperty("customer_id")
    private String customerId;
}
