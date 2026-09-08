package com.qs.Backend.modules.crm.customersupplementalproduct.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CustomerSupplementalProductListResponse {
    private List<CustomerSupplementalProductResponse> items;
}
