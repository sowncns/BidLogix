package com.qs.Backend.platform.auth.service;

import com.qs.Backend.shared.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PasswordPolicy {

    public void validate(String password, String confirmPassword) {
        if (password == null || password.length() < 8) {
            throw new AppException("Mật khẩu phải có ít nhất 8 ký tự", HttpStatus.BAD_REQUEST, "PASSWORD_TOO_SHORT");
        }
        if (password.chars().noneMatch(Character::isUpperCase)) {
            throw new AppException("Mật khẩu phải có ít nhất 1 chữ hoa", HttpStatus.BAD_REQUEST, "PASSWORD_NO_UPPERCASE");
        }
        if (password.chars().noneMatch(Character::isDigit)) {
            throw new AppException("Mật khẩu phải có ít nhất 1 chữ số", HttpStatus.BAD_REQUEST, "PASSWORD_NO_NUMBER");
        }
        if (!password.equals(confirmPassword)) {
            throw new AppException("Xác nhận mật khẩu không khớp", HttpStatus.BAD_REQUEST, "PASSWORD_CONFIRM_MISMATCH");
        }
    }
}
