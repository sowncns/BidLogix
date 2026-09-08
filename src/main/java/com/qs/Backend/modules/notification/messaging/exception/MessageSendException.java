package com.qs.Backend.modules.notification.messaging.exception;

import com.qs.Backend.modules.notification.messaging.dto.SendMessageResponse;

public class MessageSendException extends RuntimeException {
    private final SendMessageResponse response;

    public MessageSendException(SendMessageResponse response) {
        super(response.message());
        this.response = response;
    }

    public SendMessageResponse getResponse() {
        return response;
    }
}
