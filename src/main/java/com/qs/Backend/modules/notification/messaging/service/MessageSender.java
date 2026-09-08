package com.qs.Backend.modules.notification.messaging.service;

import java.util.Map;

public interface MessageSender {
    String provider();
    String send(String recipient, String template, Map<String, String> params);
}
