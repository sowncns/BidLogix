package com.qs.Backend.modules.inventory.keygen.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateMachineLockRequest {
    @NotBlank
    private String input;
    @NotBlank
    private String password;
}
