package com.qs.Backend.platform.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerRegisterResponse {
    private String message;
    private String maskedTarget;
}
