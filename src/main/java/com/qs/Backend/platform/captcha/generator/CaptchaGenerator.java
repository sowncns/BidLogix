package com.qs.Backend.platform.captcha.generator;

import com.google.code.kaptcha.Producer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class CaptchaGenerator {

    private final Producer captchaProducer;

    public GeneratedCaptcha generate() {

        String answer = captchaProducer.createText();
        BufferedImage image = captchaProducer.createImage(answer);

        String imageBase64 = "data:image/png;base64," + toBase64(image);

        return new GeneratedCaptcha(answer, imageBase64);
    }

    private String toBase64(BufferedImage image) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public record GeneratedCaptcha(String answer, String imageBase64) {
    }
}
