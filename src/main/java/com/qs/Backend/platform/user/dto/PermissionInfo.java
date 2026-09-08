package com.qs.Backend.platform.user.dto;

import java.util.UUID;

public record PermissionInfo(UUID id, String code, String description) {
}
