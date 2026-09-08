package com.qs.Backend.modules.notification.messaging.config;

import com.qs.Backend.modules.notification.messaging.service.MessageSender;
import com.qs.Backend.modules.notification.messaging.service.NoopMessageSender;
import com.qs.Backend.modules.notification.messaging.service.WhatsAppMessageSender;
import com.qs.Backend.modules.notification.messaging.service.ZaloMessageSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {
    @Bean
    public MessageSender emailMessageSender() {
        return new NoopMessageSender("java-email");
    }

    @Bean
    public MessageSender zaloMessageSender(
            @Value("${app.messaging.zalo.enabled:false}") boolean enabled,
            @Value("${app.messaging.zalo.access-token:}") String accessToken,
            @Value("${app.messaging.zalo.refresh-token:}") String refreshToken,
            @Value("${app.messaging.zalo.app-secret:}") String appSecret,
            @Value("${app.messaging.zalo.send-endpoint:}") String sendEndpoint,
            @Value("${app.messaging.zalo.refresh-endpoint:}") String refreshEndpoint) {
        if (!enabled) return new NoopMessageSender("zalo_zns_mock");
        return new ZaloMessageSender(accessToken, refreshToken, appSecret, sendEndpoint, refreshEndpoint);
    }

    @Bean
    public MessageSender whatsAppMessageSender(
            @Value("${app.messaging.whatsapp.enabled:false}") boolean enabled,
            @Value("${app.messaging.whatsapp.access-token:}") String accessToken,
            @Value("${app.messaging.whatsapp.phone-number-id:}") String phoneNumberId,
            @Value("${app.messaging.whatsapp.api-version:v18.0}") String apiVersion) {
        if (!enabled) return new NoopMessageSender("whatsapp_cloud_mock");
        return new WhatsAppMessageSender(accessToken, phoneNumberId, apiVersion);
    }
}
