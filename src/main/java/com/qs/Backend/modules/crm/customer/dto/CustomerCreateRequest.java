package com.qs.Backend.modules.crm.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerCreateRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @Email
    private String email;

    private String phone;
}
