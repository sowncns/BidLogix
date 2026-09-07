package com.qs.Backend.platform.captcha.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qs.Backend.platform.captcha.service.CaptchaService;
import com.qs.Backend.shared.response.ApiResponse;
import com.qs.Backend.platform.captcha.dto.CaptchaResponse;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/captcha")
@RequiredArgsConstructor 
public class CaptchaController {
    private final CaptchaService captchaService;

    @GetMapping 
     public ApiResponse<CaptchaResponse> generate() {

        CaptchaResponse response = captchaService.generate();

        return 
                ApiResponse.ok(
                        response,
                        "Tạo captcha thành công"
                )
        ;
    }
}
