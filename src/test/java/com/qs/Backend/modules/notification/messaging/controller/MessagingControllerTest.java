package com.qs.Backend.modules.notification.messaging.controller;

import com.qs.Backend.modules.notification.messaging.dto.DeliveryLogView;
import com.qs.Backend.modules.notification.messaging.dto.SendMessageRequest;
import com.qs.Backend.modules.notification.messaging.dto.SendMessageResponse;
import com.qs.Backend.modules.notification.messaging.service.MessagingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = MessagingController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.qs\\.Backend\\.platform\\.auth\\.security\\..*")
)
@AutoConfigureMockMvc(addFilters = false)
class MessagingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessagingService messagingService;

    @Test
    void sendReturnsDeliveryLog() throws Exception {
        when(messagingService.send(any(SendMessageRequest.class))).thenReturn(new SendMessageResponse(
                new DeliveryLogView(
                        "log-1",
                        null,
                        "zalo",
                        "java-zalo",
                        "0901234567",
                        "tpl_1",
                        "sent",
                        null,
                        "provider-1",
                        Instant.parse("2026-09-05T00:00:00Z"),
                        Instant.parse("2026-09-05T00:00:01Z")
                ),
                null
        ));

        mockMvc.perform(post("/api/v1/messages/send").contextPath("/api/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"channel\":\"zalo\",\"recipient\":\"0901234567\",\"template\":\"tpl_1\",\"params\":{\"p1\":\"42\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.log.id").value("log-1"))
                .andExpect(jsonPath("$.log.channel").value("zalo"))
                .andExpect(jsonPath("$.log.provider_message_id").value("provider-1"));
    }

    @Test
    void sendValidationErrorReturnsBadRequest() throws Exception {
        when(messagingService.send(any(SendMessageRequest.class)))
                .thenThrow(new IllegalArgumentException("unknown channel (expected: email|zalo|whatsapp)"));

        mockMvc.perform(post("/api/v1/messages/send").contextPath("/api/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"channel\":\"telegram\",\"recipient\":\"0901234567\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("unknown channel (expected: email|zalo|whatsapp)"));
    }
}
