package com.qs.Backend.modules.inventory.keygen.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateActiveRequest {
    @NotBlank
    private String input;
}
