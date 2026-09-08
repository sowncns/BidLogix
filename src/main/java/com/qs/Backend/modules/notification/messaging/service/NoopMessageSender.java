package com.qs.Backend.modules.notification.messaging.service;

import java.util.Map;
import java.util.UUID;

public class NoopMessageSender implements MessageSender {
    private final String provider;

    public NoopMessageSender(String provider) {
        this.provider = provider;
    }

    @Override
    public String provider() {
        return provider;
    }

    @Override
    public String send(String recipient, String template, Map<String, String> params) {
        return UUID.randomUUID().toString();
    }
}
