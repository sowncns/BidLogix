package com.qs.Backend.modules.notification.messaging.service;

import com.qs.Backend.modules.notification.messaging.dto.SendMessageRequest;
import com.qs.Backend.modules.notification.messaging.dto.SendMessageResponse;
import com.qs.Backend.modules.notification.messaging.entity.NotificationDeliveryLog;
import com.qs.Backend.modules.notification.messaging.exception.MessageSendException;
import com.qs.Backend.modules.notification.messaging.repository.NotificationDeliveryLogRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessagingServiceTest {

    private final NotificationDeliveryLogRepository repository = mock(NotificationDeliveryLogRepository.class);
    private final MessageSender emailSender = new StubSender("test-email", "email-id", null);
    private final MessageSender zaloSender = new StubSender("test-zalo", "zalo-id", null);
    private final MessageSender whatsAppSender = new StubSender("test-whatsapp", "wa-id", null);
    private final MessagingService service = new MessagingService(repository, emailSender, zaloSender, whatsAppSender);

    MessagingServiceTest() {
        when(repository.save(any(NotificationDeliveryLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void sendZaloReturnsDeliveryLog() {
        SendMessageResponse response = service.send(new SendMessageRequest(
                "zalo",
                "0901234567",
                "tpl_1",
                Map.of("p1", "42")
        ));

        assertThat(response.log()).isNotNull();
        assertThat(response.log().channel()).isEqualTo("zalo");
        assertThat(response.log().provider()).isEqualTo("test-zalo");
        assertThat(response.log().recipient()).isEqualTo("0901234567");
        assertThat(response.log().template()).isEqualTo("tpl_1");
        assertThat(response.log().status()).isEqualTo("sent");
        assertThat(response.log().providerMessageId()).isEqualTo("zalo-id");
        assertThat(response.message()).isNull();
        verify(repository).save(any(NotificationDeliveryLog.class));
    }

    @Test
    void sendFailurePersistsFailedLogAndThrows502Response() {
        MessagingService failingService = new MessagingService(
                repository,
                emailSender,
                new StubSender("test-zalo", null, new RuntimeException("zalo down")),
                whatsAppSender
        );
        SendMessageRequest request = new SendMessageRequest("zalo", "0901234567", "tpl", null);

        assertThatThrownBy(() -> failingService.send(request))
                .isInstanceOfSatisfying(MessageSendException.class, ex -> {
                    assertThat(ex.getResponse().message()).isEqualTo("send failed: zalo down");
                    assertThat(ex.getResponse().log().status()).isEqualTo("failed");
                    assertThat(ex.getResponse().log().error()).isEqualTo("zalo down");
                });
        verify(repository).save(any(NotificationDeliveryLog.class));
    }

    @Test
    void rejectsUnknownChannel() {
        SendMessageRequest request = new SendMessageRequest("telegram", "0901234567", null, null);

        assertThatThrownBy(() -> service.send(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("unknown channel (expected: email|zalo|whatsapp)");
    }

    @Test
    void rejectsMissingRecipient() {
        SendMessageRequest request = new SendMessageRequest("zalo", "", "tpl", null);

        assertThatThrownBy(() -> service.send(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("recipient is required");
    }

    @Test
    void rejectsMissingZaloTemplate() {
        SendMessageRequest request = new SendMessageRequest("zalo", "0901234567", "", null);

        assertThatThrownBy(() -> service.send(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("template (Zalo template ID) is required");
    }

    private record StubSender(String provider, String messageId, RuntimeException error) implements MessageSender {
        @Override
        public String send(String recipient, String template, Map<String, String> params) {
            if (error != null) {
                throw error;
            }
            return messageId;
        }
    }
}
