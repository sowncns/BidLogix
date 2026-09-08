package com.qs.Backend.platform.user.service;

import com.qs.Backend.platform.user.dto.UserCreateRequest;
import com.qs.Backend.platform.user.dto.UserUpdateRequest;
import com.qs.Backend.shared.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.regex.Pattern;

// Ported from qs-crm's internal/core/user/{validation,service}.go: same phone/email/region
// rules as customer (see CustomerRequestNormalizer), plus user-specific password rules.
final class UserRequestValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^0(3[0-9]|5[0-9]|7[0-9]|8[0-9]|9[0-9])[0-9]{7}$");
    private static final Pattern LANDLINE_PATTERN = Pattern.compile("^0(2[0-9]|2[68]|3[1-9]|4[2-689]|5[1-689]|6[1-689]|7[0-269]|9[01])[0-9]{7,8}$");
    private static final Pattern INTL_PLUS_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$");
    private static final Pattern INTL_ZERO_PATTERN = Pattern.compile("^[1-9]\\d{7,14}$");
    private static final Pattern DIGITS_ONLY_PATTERN = Pattern.compile("^\\d+$");
    private static final int MIN_PASSWORD_LENGTH = 6;

    private UserRequestValidator() {
    }

    static void normalize(UserCreateRequest p) {
        p.setEmail(trim(p.getEmail()));
        p.setPhone(trim(p.getPhone()));
        p.setFirstName(trim(p.getFirstName()));
        p.setLastName(trim(p.getLastName()));
        p.setAvatarUrl(trim(p.getAvatarUrl()));
        // Password intentionally not trimmed: login compares the raw submitted string.

        if ((p.getEmail() == null || p.getEmail().isEmpty()) && (p.getPhone() == null || p.getPhone().isEmpty())) {
            throw invalid("email or phone is required");
        }
        if (p.getPassword() == null || p.getPassword().isEmpty()) {
            throw invalid("password is required");
        }
        if (p.getPassword().length() < MIN_PASSWORD_LENGTH) {
            throw invalid("password must be at least 6 characters");
        }
        if (p.getEmail() != null && !p.getEmail().isEmpty()) {
            validateEmail(p.getEmail());
        }
        if (p.getPhone() != null && !p.getPhone().isEmpty()) {
            validatePhone(p.getPhone());
        }

        String region = trim(p.getRegion());
        if (region != null && region.length() > 16) {
            throw invalid("region must be at most 16 characters");
        }
        p.setRegion((region == null || region.isEmpty()) ? "VN" : region);
    }

    static void normalize(UserUpdateRequest p) {
        if (p.getEmail() != null) {
            String trimmed = trim(p.getEmail());
            if (trimmed.isEmpty()) {
                throw invalid("email cannot be empty");
            }
            validateEmail(trimmed);
            p.setEmail(trimmed);
        }
        if (p.getPhone() != null) {
            String trimmed = trim(p.getPhone());
            p.setPhone(trimmed);
            if (!trimmed.isEmpty()) {
                validatePhone(trimmed);
            }
        }
        if (p.getFirstName() != null) {
            p.setFirstName(trim(p.getFirstName()));
        }
        if (p.getLastName() != null) {
            p.setLastName(trim(p.getLastName()));
        }
        if (p.getAvatarUrl() != null) {
            p.setAvatarUrl(trim(p.getAvatarUrl()));
        }
        if (p.getRegion() != null) {
            String trimmed = trim(p.getRegion());
            if (trimmed.isEmpty()) {
                throw invalid("region cannot be empty");
            }
            if (trimmed.length() > 16) {
                throw invalid("region must be at most 16 characters");
            }
            p.setRegion(trimmed);
        }
    }

    static void validateNewPassword(String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.isEmpty()) {
            throw invalid("password is required");
        }
        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw invalid("password must be at least 6 characters");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw invalid("passwords do not match");
        }
    }

    private static void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw invalid("invalid email format");
        }
        if (email.length() > 254) {
            throw invalid("email address is too long (maximum 254 characters)");
        }
        if (email.contains("..")) {
            throw invalid("email cannot contain consecutive dots");
        }
        String[] parts = email.split("@");
        if (parts.length != 2) {
            throw invalid("invalid email format");
        }
        if (parts[0].startsWith(".") || parts[0].endsWith(".")) {
            throw invalid("email local part cannot start or end with a dot");
        }
        if (parts[1].startsWith(".") || parts[1].endsWith(".")) {
            throw invalid("email domain cannot start or end with a dot");
        }
    }

    private static void validatePhone(String phone) {
        String cleaned = phone.replace(" ", "").replace("-", "").replace(".", "").replace("(", "").replace(")", "");

        if (cleaned.startsWith("+")) {
            if (!INTL_PLUS_PATTERN.matcher(cleaned).matches()) {
                throw invalid("invalid international phone number format (E.164: +[1-9] followed by 7-14 digits, total 8-15 digits)");
            }
            return;
        }
        if (cleaned.startsWith("00")) {
            if (!INTL_ZERO_PATTERN.matcher(cleaned.substring(2)).matches()) {
                throw invalid("invalid international phone number format (00[1-9] followed by 7-14 digits, total 8-15 digits)");
            }
            return;
        }

        if (cleaned.startsWith("+84")) {
            cleaned = "0" + cleaned.substring(3);
        } else {
            cleaned = cleaned.replace("+", "");
            if (cleaned.startsWith("84")) {
                cleaned = "0" + cleaned.substring(2);
            }
        }

        if (!DIGITS_ONLY_PATTERN.matcher(cleaned).matches()) {
            throw invalid("phone number must contain only digits and valid separators");
        }
        if (MOBILE_PATTERN.matcher(cleaned).matches() || LANDLINE_PATTERN.matcher(cleaned).matches()) {
            return;
        }
        throw invalid("phone number must be a valid Vietnamese phone number (mobile or landline format) or international format (+country code or 00country code followed by 8-15 digits)");
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static AppException invalid(String message) {
        return new AppException(message, HttpStatus.BAD_REQUEST, "USER_INVALID");
    }
}
