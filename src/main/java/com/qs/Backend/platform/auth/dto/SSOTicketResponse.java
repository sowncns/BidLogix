package com.qs.Backend.platform.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SSOTicketResponse {
    private String ticket;
    private long expiresInSeconds;
}
