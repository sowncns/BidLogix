package com.qs.Backend.platform.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private TokenResponse tokens;
    private UUID userId;
    private String username;
}
