package com.qs.Backend.modules.notification.messaging.service;

import com.qs.Backend.modules.notification.messaging.dto.DeliveryLogView;
import com.qs.Backend.modules.notification.messaging.dto.SendMessageRequest;
import com.qs.Backend.modules.notification.messaging.dto.SendMessageResponse;
import com.qs.Backend.modules.notification.messaging.entity.NotificationDeliveryLog;
import com.qs.Backend.modules.notification.messaging.exception.MessageSendException;
import com.qs.Backend.modules.notification.messaging.repository.NotificationDeliveryLogRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

// Mirrors the Go QA messaging endpoint contract. Provider integrations can be
// plugged in behind this service without changing controller/request shapes.
@Service
public class MessagingService {

    private static final String STATUS_SENT = "sent";
    private static final String STATUS_FAILED = "failed";

    private final NotificationDeliveryLogRepository deliveryLogRepository;
    private final MessageSender emailSender;
    private final MessageSender zaloSender;
    private final MessageSender whatsAppSender;

    public MessagingService(
            NotificationDeliveryLogRepository deliveryLogRepository,
            @Qualifier("emailMessageSender")
            MessageSender emailMessageSender,
            @Qualifier("zaloMessageSender")
            MessageSender zaloMessageSender,
            @Qualifier("whatsAppMessageSender")
            MessageSender whatsAppMessageSender
    ) {
        this.deliveryLogRepository = deliveryLogRepository;
        this.emailSender = emailMessageSender;
        this.zaloSender = zaloMessageSender;
        this.whatsAppSender = whatsAppMessageSender;
    }

    public SendMessageResponse send(SendMessageRequest request) {
        String channel = normalizeChannel(request.channel());
        String recipient = request.recipient() == null ? "" : request.recipient().trim();
        if (recipient.isEmpty()) {
            throw new IllegalArgumentException("recipient is required");
        }

        String template = request.template() == null ? "" : request.template().trim();
        if ("email".equals(channel) && template.isEmpty()) {
            throw new IllegalArgumentException("template is required for channel=email");
        }
        if ("zalo".equals(channel) && template.isEmpty()) {
            throw new IllegalArgumentException("template (Zalo template ID) is required");
        }

        MessageSender sender = senderFor(channel);
        Instant now = Instant.now();
        NotificationDeliveryLog entry = new NotificationDeliveryLog();
        entry.setId(UUID.randomUUID());
        entry.setChannel(channel);
        entry.setProvider(sender.provider());
        entry.setRecipient(recipient);
        entry.setTemplate(template.isEmpty() ? null : template);
        entry.setCreatedAt(now);

        try {
            String providerMessageId = sender.send(recipient, template, request.params() == null ? Map.of() : request.params());
            entry.setStatus(STATUS_SENT);
            entry.setProviderMessageId(providerMessageId);
            entry.setSentAt(now);
            deliveryLogRepository.save(entry);
            return new SendMessageResponse(toView(entry), null);
        } catch (RuntimeException ex) {
            entry.setStatus(STATUS_FAILED);
            entry.setError(ex.getMessage());
            deliveryLogRepository.save(entry);
            DeliveryLogView view = toView(entry);
            throw new MessageSendException(new SendMessageResponse(view, "send failed: " + ex.getMessage()));
        }
    }

    private DeliveryLogView toView(NotificationDeliveryLog entry) {
        return new DeliveryLogView(
                entry.getId().toString(),
                null,
                entry.getChannel(),
                entry.getProvider(),
                entry.getRecipient(),
                entry.getTemplate(),
                entry.getStatus(),
                entry.getError(),
                entry.getProviderMessageId(),
                entry.getCreatedAt(),
                entry.getSentAt()
        );
    }

    private String normalizeChannel(String rawChannel) {
        String channel = rawChannel == null ? "" : rawChannel.trim().toLowerCase(Locale.ROOT);
        if (!channel.equals("email") && !channel.equals("zalo") && !channel.equals("whatsapp")) {
            throw new IllegalArgumentException("unknown channel (expected: email|zalo|whatsapp)");
        }
        return channel;
    }

    private MessageSender senderFor(String channel) {
        return switch (channel) {
            case "email" -> emailSender;
            case "zalo" -> zaloSender;
            case "whatsapp" -> whatsAppSender;
            default -> throw new IllegalArgumentException("unknown channel (expected: email|zalo|whatsapp)");
        };
    }
}
