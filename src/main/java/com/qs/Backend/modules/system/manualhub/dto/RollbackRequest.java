package com.qs.Backend.modules.system.manualhub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RollbackRequest {
    @NotBlank
    private String reason;
}
