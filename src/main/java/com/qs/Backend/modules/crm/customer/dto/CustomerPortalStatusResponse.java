package com.qs.Backend.modules.crm.customer.dto;

public record CustomerPortalStatusResponse(boolean hasPortalAccess, String email, String region) {
}
