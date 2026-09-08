package com.qs.Backend.modules.inventory.keygen.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GenerateMachineLockResponse {
    private MachineLockKeysResponse keys;
}
