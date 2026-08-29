package com.qs.Backend.platform.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

// Best-effort async fan-out of an already-generated OTP to Zalo and/or WhatsApp. Each channel is
// independently toggled via config; when both are disabled (the default with no credentials set),
// this only logs. Failures never propagate - the caller (customer registration) must not be
// blocked by a delivery-provider outage.
@Slf4j
@Service
public class PhoneOtpDispatcherImpl implements PhoneOtpDispatcher {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.notification.zalo.enabled:false}")
    private boolean zaloEnabled;

    @Value("${app.notification.zalo.access-token:}")
    private String zaloAccessToken;

    @Value("${app.notification.zalo.template-id:}")
    private String zaloTemplateId;

    @Value("${app.notification.zalo.api-url:https://business.openapi.zalo.me/message/template}")
    private String zaloApiUrl;

    @Value("${app.notification.whatsapp.enabled:false}")
    private boolean whatsAppEnabled;

    @Value("${app.notification.whatsapp.access-token:}")
    private String whatsAppAccessToken;

    @Value("${app.notification.whatsapp.phone-number-id:}")
    private String whatsAppPhoneNumberId;

    @Value("${app.notification.whatsapp.template-name:}")
    private String whatsAppTemplateName;

    @Value("${app.notification.whatsapp.template-lang:vi}")
    private String whatsAppTemplateLang;

    @Value("${app.notification.whatsapp.api-url:https://graph.facebook.com/v19.0}")
    private String whatsAppApiUrl;

    @Override
    @Async
    public void dispatch(Long accountId, String rawPhone, String code, int expiryMinutes) {
        if (rawPhone == null || rawPhone.isBlank()) {
            return;
        }
        if (!zaloEnabled && !whatsAppEnabled) {
            log.info("[phone-otp] no delivery provider enabled, skipping fan-out to {} for account {}",
                    maskPhone(rawPhone), accountId);
            return;
        }
        if (zaloEnabled) {
            fireZalo(accountId, rawPhone, code, expiryMinutes);
        }
        if (whatsAppEnabled) {
            fireWhatsApp(accountId, rawPhone, code, expiryMinutes);
        }
    }

    private void fireZalo(Long accountId, String phone, String code, int expiryMinutes) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("access_token", zaloAccessToken);

            Map<String, Object> body = Map.of(
                    "phone", phone,
                    "template_id", zaloTemplateId,
                    "template_data", Map.of("otp", code, "expire", expiryMinutes)
            );

            restTemplate.postForEntity(zaloApiUrl, new HttpEntity<>(body, headers), String.class);
        } catch (RestClientException ex) {
            log.warn("[phone-otp] Zalo send failed for account {}: {}", accountId, ex.getMessage());
        }
    }

    private void fireWhatsApp(Long accountId, String phone, String code, int expiryMinutes) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(whatsAppAccessToken);

            Map<String, Object> body = Map.of(
                    "messaging_product", "whatsapp",
                    "to", phone,
                    "type", "template",
                    "template", Map.of(
                            "name", whatsAppTemplateName,
                            "language", Map.of("code", whatsAppTemplateLang),
                            "components", List.of(Map.of(
                                    "type", "body",
                                    "parameters", List.of(Map.of("type", "text", "text", code))
                            ))
                    )
            );

            String url = whatsAppApiUrl + "/" + whatsAppPhoneNumberId + "/messages";
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        } catch (RestClientException ex) {
            log.warn("[phone-otp] WhatsApp send failed for account {}: {}", accountId, ex.getMessage());
        }
    }

    private String maskPhone(String phone) {
        if (phone.length() <= 5) return phone;
        return phone.substring(0, 3) + "***" + phone.substring(phone.length() - 2);
    }
}
