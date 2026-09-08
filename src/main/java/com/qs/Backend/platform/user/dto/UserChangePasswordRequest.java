package com.qs.Backend.platform.user.dto;

import lombok.Getter;
import lombok.Setter;

// Administrative password reset: no old password required - authorization comes from the
// caller's permission, not from knowing the target's current credentials.
@Getter
@Setter
public class UserChangePasswordRequest {
    private String newPassword;
    private String confirmPassword;
}
