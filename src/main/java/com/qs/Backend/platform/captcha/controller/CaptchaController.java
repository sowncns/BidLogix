package com.qs.Backend.platform.captcha.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qs.Backend.platform.captcha.service.CaptchaService;
import com.qs.Backend.platform.captcha.dto.CaptchaResponse;
import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
public class CaptchaController {
    private final CaptchaService captchaService;

    // Returns the bare {id, image} shape (no ApiResponse envelope) to match
    // the Go backend's captcha handler, which the FE parses directly.
    @GetMapping({"/captcha", "/public/captcha"})
    public CaptchaResponse generate() {
        return captchaService.generate();
    }
}
