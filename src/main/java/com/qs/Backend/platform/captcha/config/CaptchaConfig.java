package com.qs.Backend.platform.captcha.config;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;

@Configuration
public class CaptchaConfig {

    @Bean
    public Producer captchaProducer() {

        DefaultKaptcha captcha = new DefaultKaptcha();

        Properties properties = new Properties();

        properties.setProperty(
                "kaptcha.textproducer.char.length",
                "4"
        );

        properties.setProperty(
                "kaptcha.textproducer.char.string",
                "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        );

        properties.setProperty(
                "kaptcha.image.width",
                "200"
        );

        properties.setProperty(
                "kaptcha.image.height",
                "60"
        );

        captcha.setConfig(
                new Config(properties)
        );

        return captcha;
    }
}