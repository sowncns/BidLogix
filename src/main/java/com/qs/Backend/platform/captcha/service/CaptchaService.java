package com.qs.Backend.platform.captcha.service;
import com.qs.Backend.platform.captcha.dto.CaptchaResponse;
public interface CaptchaService {

    CaptchaResponse generate();

    void verify(String captchaId, String answer);
}