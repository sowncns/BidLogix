package com.qs.Backend.platform.auth.service;

public final class LocaleResolver {

    private LocaleResolver() {}

    public static String fromRegion(String region) {
        if (region == null || region.isBlank() || region.equalsIgnoreCase("VN")) {
            return "vi";
        }
        return "en";
    }
}
