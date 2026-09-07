package com.qs.Backend.platform.captcha.dto;

public record CaptchaResponse(
        String captchaId,
        String imageBase64
) {
}
