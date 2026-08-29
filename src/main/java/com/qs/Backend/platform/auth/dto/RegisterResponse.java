package com.qs.Backend.platform.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterResponse {
    private Long id;
    private String username;
    private String email;
}
