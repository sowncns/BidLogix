package com.qs.Backend.modules.notification.messaging.controller;

import com.qs.Backend.modules.notification.messaging.dto.SendMessageRequest;
import com.qs.Backend.modules.notification.messaging.dto.SendMessageResponse;
import com.qs.Backend.modules.notification.messaging.exception.MessageSendException;
import com.qs.Backend.modules.notification.messaging.service.MessagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessagingController {

    private final MessagingService messagingService;

    @PostMapping("/send")
    public SendMessageResponse send(@RequestBody SendMessageRequest request) {
        return messagingService.send(request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MessageSendException.class)
    public ResponseEntity<SendMessageResponse> handleSendFailure(MessageSendException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ex.getResponse());
    }
}
