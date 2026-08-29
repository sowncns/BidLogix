package com.qs.Backend.modules.crm.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class CustomerResponse {
    private Long id;
    private String code;
    private String name;
    private String email;
    private String phone;
    private boolean active;
    private Instant createdAt;
}
