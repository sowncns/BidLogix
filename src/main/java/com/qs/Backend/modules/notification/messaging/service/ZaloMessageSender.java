package com.qs.Backend.modules.notification.messaging.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class ZaloMessageSender implements MessageSender {
    private static final String PROVIDER = "zalo_zns";
    private static final int TOKEN_INVALID = -124;

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String sendEndpoint;
    private final String refreshEndpoint;
    private final String appSecret;
    private String accessToken;
    private String refreshToken;
    private Instant expiresAt;

    public ZaloMessageSender(String accessToken, String refreshToken, String appSecret, String sendEndpoint, String refreshEndpoint) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.appSecret = appSecret;
        this.sendEndpoint = blankToDefault(sendEndpoint, "https://business.openapi.zalo.me/message/template");
        this.refreshEndpoint = blankToDefault(refreshEndpoint, "https://oauth.zaloapp.com/v4/oa/access_token");
    }

    @Override
    public String provider() {
        return PROVIDER;
    }

    @Override
    public String send(String recipient, String template, Map<String, String> params) {
        String phone = PhoneNumberNormalizer.normalizeVN(recipient);
        byte[] body = write(Map.of("phone", phone, "template_id", template, "template_data", params, "tracking_id", ""));
        PostResult first = post(body);
        if (first.error() == null) return first.messageId();
        if (first.providerCode() != TOKEN_INVALID) throw new IllegalStateException(first.error());
        refreshToken();
        PostResult second = post(body);
        if (second.error() != null) throw new IllegalStateException(second.error());
        return second.messageId();
    }

    private PostResult post(byte[] body) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(sendEndpoint))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("access_token", accessToken == null ? "" : accessToken)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 500) return new PostResult(null, 0, "zalo: send failed: http " + response.statusCode());
            JsonNode root = objectMapper.readTree(response.body());
            int error = root.path("error").asInt(0);
            if (error != 0) return new PostResult(null, error, "zalo: send failed: code=" + error + " msg=" + root.path("message").asText());
            return new PostResult(root.path("data").path("msg_id").asText(null), 0, null);
        } catch (Exception e) {
            return new PostResult(null, 0, "zalo: send failed: " + e.getMessage());
        }
    }

    private synchronized void refreshToken() {
        if (expiresAt != null && expiresAt.isAfter(Instant.now().plusSeconds(60))) return;
        try {
            String form = "app_id=&grant_type=refresh_token&refresh_token=" + java.net.URLEncoder.encode(refreshToken == null ? "" : refreshToken, java.nio.charset.StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(refreshEndpoint))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("secret_key", appSecret == null ? "" : appSecret)
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();
            JsonNode root = objectMapper.readTree(httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body());
            String newAccessToken = root.path("access_token").asText("");
            if (newAccessToken.isBlank()) throw new IllegalStateException("zalo: refresh failed code=" + root.path("error").asInt() + " msg=" + root.path("message").asText());
            accessToken = newAccessToken;
            String newRefreshToken = root.path("refresh_token").asText("");
            if (!newRefreshToken.isBlank()) refreshToken = newRefreshToken;
            expiresAt = Instant.now().plusSeconds(3600);
        } catch (Exception e) {
            throw new IllegalStateException("zalo: send failed: token refresh: " + e.getMessage());
        }
    }

    private byte[] write(Object value) {
        try { return objectMapper.writeValueAsBytes(value); } catch (Exception e) { throw new IllegalStateException("zalo: marshal payload: " + e.getMessage()); }
    }

    private static String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private record PostResult(String messageId, int providerCode, String error) {}
}
