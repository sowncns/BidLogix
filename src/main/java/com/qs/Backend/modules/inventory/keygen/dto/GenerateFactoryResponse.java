package com.qs.Backend.modules.inventory.keygen.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GenerateFactoryResponse {
    private List<FactoryPasswordResponse> passwords;
}
