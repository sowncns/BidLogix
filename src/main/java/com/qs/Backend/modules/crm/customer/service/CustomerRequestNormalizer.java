package com.qs.Backend.modules.crm.customer.service;

import com.qs.Backend.modules.crm.customer.dto.CustomerCreateRequest;
import com.qs.Backend.modules.crm.customer.dto.CustomerUpdateRequest;
import com.qs.Backend.shared.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.regex.Pattern;

// Trims and validates customer request fields. Mirrors qs-crm's normalizeCreateParams /
// normalizeUpdateParams: null means "not provided" for update fields (Go's XxxSet flags
// collapse into "field is non-null" here since Java has no separate presence tracking).
final class CustomerRequestNormalizer {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^0(3[0-9]|5[0-9]|7[0-9]|8[0-9]|9[0-9])[0-9]{7}$");
    private static final Pattern LANDLINE_PATTERN = Pattern.compile("^0(2[0-9]|2[68]|3[1-9]|4[2-689]|5[1-689]|6[1-689]|7[0-269]|9[01])[0-9]{7,8}$");
    private static final Pattern INTL_PLUS_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$");
    private static final Pattern INTL_ZERO_PATTERN = Pattern.compile("^[1-9]\\d{7,14}$");
    private static final Pattern DIGITS_ONLY_PATTERN = Pattern.compile("^\\d+$");

    private CustomerRequestNormalizer() {
    }

    static void normalize(CustomerCreateRequest p) {
        p.setOrganizationId(blankToNull(trim(p.getOrganizationId())));
        p.setCode(trim(p.getCode()));
        p.setFullName(trim(p.getFullName()));
        p.setEmail(trim(p.getEmail()));
        p.setPhone(trim(p.getPhone()));
        p.setCompanyName(trim(p.getCompanyName()));
        p.setSource(trim(p.getSource()));
        p.setMainPhone(trim(p.getMainPhone()));
        p.setMainEmail(trim(p.getMainEmail()));
        p.setWebsite(trim(p.getWebsite()));

        validateEmail(p.getEmail());
        validateEmail(p.getMainEmail());
        validatePhone(p.getPhone());
        validatePhone(p.getMainPhone());

        p.setStatusId(blankToNull(trim(p.getStatusId())));
        if (p.getAssignedSalesId() != null) {
            p.setAssignedSalesId(trim(p.getAssignedSalesId()));
        }

        p.setGroupIds(normalizeIds(p.getGroupIds()));
        p.setBusinessFieldIds(normalizeIds(p.getBusinessFieldIds()));

        p.setGender(trim(p.getGender()));
        if (p.getGender() != null && !p.getGender().isEmpty() && !p.getGender().equals("male") && !p.getGender().equals("female")) {
            throw invalid("gender must be 'male' or 'female'");
        }

        p.setRegion(trim(p.getRegion()));
        if (p.getRegion() != null && p.getRegion().length() > 16) {
            throw invalid("region must be at most 16 characters");
        }
        if (p.getRegion() == null || p.getRegion().isEmpty()) {
            p.setRegion("VN");
        }

        if (p.getFullName() == null || p.getFullName().isEmpty()) {
            throw invalid("full_name is required");
        }
    }

    static void normalize(CustomerUpdateRequest p) {
        if (p.getOrganizationId() != null) {
            p.setOrganizationId(blankToNull(trim(p.getOrganizationId())));
        }
        p.setCode(trim(p.getCode()));
        p.setFullName(trim(p.getFullName()));
        p.setEmail(trim(p.getEmail()));
        p.setPhone(trim(p.getPhone()));
        p.setCompanyName(trim(p.getCompanyName()));
        p.setSource(trim(p.getSource()));

        if (p.getStatusId() != null) {
            p.setStatusId(blankToNull(trim(p.getStatusId())));
        }
        if (p.getAssignedSalesId() != null) {
            p.setAssignedSalesId(trim(p.getAssignedSalesId()));
        }

        p.setMainPhone(trim(p.getMainPhone()));
        p.setMainEmail(trim(p.getMainEmail()));
        p.setWebsite(trim(p.getWebsite()));

        if (p.getEmail() != null && !p.getEmail().isEmpty()) {
            validateEmail(p.getEmail());
        }
        if (p.getMainEmail() != null && !p.getMainEmail().isEmpty()) {
            validateEmail(p.getMainEmail());
        }
        if (p.getPhone() != null && !p.getPhone().isEmpty()) {
            validatePhone(p.getPhone());
        }
        if (p.getMainPhone() != null && !p.getMainPhone().isEmpty()) {
            validatePhone(p.getMainPhone());
        }

        if (p.getFullName() != null && p.getFullName().isEmpty()) {
            throw invalid("full_name is required");
        }
        if (p.getPhone() != null && p.getPhone().isEmpty()) {
            throw invalid("phone is required");
        }

        if (p.getGroupIds() != null) {
            p.setGroupIds(normalizeIds(p.getGroupIds()));
        }
        if (p.getBusinessFieldIds() != null) {
            p.setBusinessFieldIds(normalizeIds(p.getBusinessFieldIds()));
        }

        if (p.getGender() != null) {
            String g = trim(p.getGender());
            p.setGender(g);
            if (!g.isEmpty() && !g.equals("male") && !g.equals("female")) {
                throw invalid("gender must be 'male' or 'female'");
            }
        }

        if (p.getRegion() != null) {
            String r = trim(p.getRegion());
            p.setRegion(r);
            if (r.isEmpty()) {
                throw invalid("region cannot be empty");
            }
            if (r.length() > 16) {
                throw invalid("region must be at most 16 characters");
            }
        }
    }

    private static List<String> normalizeIds(List<String> ids) {
        if (ids == null) {
            return null;
        }
        return ids.stream()
                .map(CustomerRequestNormalizer::trim)
                .filter(id -> id != null && !id.isEmpty())
                .toList();
    }

    static void validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            return;
        }
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
        String localPart = parts[0];
        String domainPart = parts[1];
        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            throw invalid("email local part cannot start or end with a dot");
        }
        if (domainPart.startsWith(".") || domainPart.endsWith(".")) {
            throw invalid("email domain cannot start or end with a dot");
        }
    }

    static void validatePhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return;
        }

        String cleaned = phone.replace(" ", "")
                .replace("-", "")
                .replace(".", "")
                .replace("(", "")
                .replace(")", "");

        boolean hasPlus = cleaned.startsWith("+");
        boolean hasDoubleZero = cleaned.startsWith("00");

        if (hasPlus) {
            if (!INTL_PLUS_PATTERN.matcher(cleaned).matches()) {
                throw invalid("invalid international phone number format (E.164: +[1-9] followed by 7-14 digits, total 8-15 digits)");
            }
            return;
        }
        if (hasDoubleZero) {
            String rest = cleaned.substring(2);
            if (!INTL_ZERO_PATTERN.matcher(rest).matches()) {
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

    private static String blankToNull(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }

    private static AppException invalid(String message) {
        return new AppException(message, HttpStatus.BAD_REQUEST, "CRM_CUSTOMER_INVALID");
    }
}
