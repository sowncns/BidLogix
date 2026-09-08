package com.qs.Backend.modules.notification.messaging.config;

import com.qs.Backend.modules.notification.messaging.service.MessageSender;
import com.qs.Backend.modules.notification.messaging.service.NoopMessageSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {
    @Bean
    public MessageSender emailMessageSender() {
        return new NoopMessageSender("java-email");
    }

    @Bean
    public MessageSender zaloMessageSender() {
        return new NoopMessageSender("java-zalo");
    }

    @Bean
    public MessageSender whatsAppMessageSender() {
        return new NoopMessageSender("java-whatsapp");
    }
}
