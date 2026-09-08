package com.qs.Backend.platform.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserListResponse {
    private List<UserResponse> items;
    private int limit;
    private int offset;
    private long total;
}
