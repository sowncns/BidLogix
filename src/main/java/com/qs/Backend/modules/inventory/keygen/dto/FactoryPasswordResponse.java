package com.qs.Backend.modules.inventory.keygen.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FactoryPasswordResponse {
    private String time;
    private String password;
}
