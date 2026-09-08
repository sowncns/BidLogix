package com.qs.Backend.modules.inventory.productitem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductItemNotesRequest {

    @NotBlank
    private String notes;
}
