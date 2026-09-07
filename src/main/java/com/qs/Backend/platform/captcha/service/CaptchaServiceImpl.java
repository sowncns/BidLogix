package com.qs.Backend.platform.captcha.service;


import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.qs.Backend.platform.captcha.generator.CaptchaGenerator;
import com.qs.Backend.platform.captcha.dto.CaptchaResponse;

@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final CaptchaGenerator captchaGenerator;

    private final Map<String, String> captchaStore =
            new ConcurrentHashMap<>();

    @Override
    public CaptchaResponse generate() {

        CaptchaGenerator.GeneratedCaptcha generated =
                captchaGenerator.generate();

        String captchaId = UUID.randomUUID().toString();

        captchaStore.put(
                captchaId,
                generated.answer()
        );

        return new CaptchaResponse(
                captchaId,
                generated.imageBase64()
        );
        }

    @Override
    public void verify(String captchaId, String answer) {

        if (captchaId == null || answer == null) {
            throw new AppException(
                    "Captcha không hợp lệ!",
                    HttpStatus.BAD_REQUEST,
                    "INVALID_CAPTCHA"
            );
        }

        // remove luôn => CAPTCHA chỉ dùng được 1 lần
        String expectedAnswer = captchaStore.remove(captchaId);

        if (expectedAnswer == null ||
                !expectedAnswer.equalsIgnoreCase(answer.trim())) {

            throw new AppException(
                    "Captcha không hợp lệ hoặc đã hết hạn!",
                    HttpStatus.BAD_REQUEST,
                    "INVALID_CAPTCHA"
            );
        }
    }
}
