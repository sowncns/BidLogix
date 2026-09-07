package com.qs.Backend.platform.captcha.store;

public interface CaptchaStorage {

    void save(String captchaId, String answer);

    String get(String captchaId);

    void delete(String captchaId);
}
