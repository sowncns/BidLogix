package com.qs.Backend.platform.activationrequest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ActivationRequestSubmitRequest {

    @NotBlank
    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("request_type")
    private String requestType;

    @NotEmpty
    private List<String> inputs;

    @JsonProperty("warranty_expiry")
    private Instant warrantyExpiry;
}
