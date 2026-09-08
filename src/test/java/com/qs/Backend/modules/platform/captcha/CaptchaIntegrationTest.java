package com.qs.Backend.modules.platform.captcha;

import com.qs.Backend.platform.captcha.config.CaptchaConfig;
import com.qs.Backend.platform.captcha.dto.CaptchaResponse;
import com.qs.Backend.platform.captcha.generator.CaptchaGenerator;
import com.qs.Backend.platform.captcha.service.CaptchaService;
import com.qs.Backend.platform.captcha.service.CaptchaServiceImpl;
import com.qs.Backend.shared.exception.AppException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CaptchaIntegrationTest {
    private final CaptchaService captchaService = new CaptchaServiceImpl(new CaptchaGenerator(new CaptchaConfig().captchaProducer()));

    @Test
    void generateCaptcha_ShouldSuccess() {
        CaptchaResponse response = captchaService.generate();

        assertNotNull(response);
        assertNotNull(response.captchaId());
        assertNotNull(response.imageBase64());
        assertTrue(response.imageBase64().startsWith("data:image/png;base64,"));
    }

    @Test
    void verifyWrongCaptcha_ShouldThrowException() {
        CaptchaResponse response = captchaService.generate();

        AppException exception = assertThrows(AppException.class, () -> captchaService.verify(response.captchaId(), "SAI_CAPTCHA"));

        assertEquals("INVALID_CAPTCHA", exception.getErrorCode());
    }
}
