package com.qs.Backend.modules.inventory.productitem.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductItemPreflightRequest {
    private List<String> inputs;
}
