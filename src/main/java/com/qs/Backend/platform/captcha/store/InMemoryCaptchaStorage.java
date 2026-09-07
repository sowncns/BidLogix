package com.qs.Backend.platform.captcha.store;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component 
public class InMemoryCaptchaStorage implements CaptchaStorage {

    private final Map<String, String> store = new ConcurrentHashMap<>();
    
    @Override
    public void save(String captchaId, String answer) {
        store.put(captchaId, answer);
    }

    @Override
    public String get(String captchaId) {
        return store.get(captchaId);
    }

    @Override
    public void delete(String captchaId) {
        store.remove(captchaId);
    }
}