package com.qs.Backend.platform.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SSOLoginRequest {

    @NotBlank
    private String ticket;
}
