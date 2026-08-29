package com.qs.Backend.platform.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private TokenResponse tokens;
    private Long userId;
    private String username;
}
