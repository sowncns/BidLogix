package com.qs.Backend.modules.notification.messaging.service;

final class PhoneNumberNormalizer {
    private PhoneNumberNormalizer() {
    }

    static String normalizeVN(String raw) {
        String digits = raw == null ? "" : raw.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            throw new IllegalArgumentException("invalid VN phone number");
        }
        if (digits.startsWith("84")) {
            if (digits.length() != 11) throw new IllegalArgumentException("invalid VN phone number");
            return digits;
        }
        if (digits.startsWith("0")) {
            if (digits.length() != 10) throw new IllegalArgumentException("invalid VN phone number");
            return "84" + digits.substring(1);
        }
        if (digits.length() != 9) throw new IllegalArgumentException("invalid VN phone number");
        return "84" + digits;
    }
}
