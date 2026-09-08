package com.qs.Backend.modules.notification.messaging.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class WhatsAppMessageSender implements MessageSender {
    private static final String PROVIDER = "whatsapp_cloud";

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String accessToken;
    private final String endpoint;

    public WhatsAppMessageSender(String accessToken, String phoneNumberId, String apiVersion) {
        this.accessToken = accessToken;
        String version = apiVersion == null || apiVersion.isBlank() ? "v18.0" : apiVersion;
        if (phoneNumberId == null || phoneNumberId.isBlank()) throw new IllegalArgumentException("whatsapp: phoneNumberID is required");
        this.endpoint = "https://graph.facebook.com/" + version + "/" + phoneNumberId + "/messages";
    }

    @Override
    public String provider() {
        return PROVIDER;
    }

    @Override
    public String send(String recipient, String template, Map<String, String> params) {
        String phone = PhoneNumberNormalizer.normalizeVN(recipient);
        Map<String, ?> payload = template == null || template.isBlank()
                ? textPayload(phone, params.getOrDefault("body", ""))
                : templatePayload(phone, template, params);
        return post(payload);
    }

    private Map<String, ?> textPayload(String phone, String body) {
        return Map.of("messaging_product", "whatsapp", "recipient_type", "individual", "to", phone, "type", "text", "text", Map.of("body", body));
    }

    private Map<String, ?> templatePayload(String phone, String template, Map<String, String> params) {
        List<String> args = orderedParams(params);
        List<Map<String, Object>> bodyParams = args.stream()
                .map(v -> Map.<String, Object>of("type", "text", "text", v))
                .toList();
        List<Map<String, ?>> components = new ArrayList<>();
        components.add(Map.of("type", "body", "parameters", bodyParams));
        if (args.size() == 1) {
            components.add(Map.of("type", "button", "sub_type", "url", "index", "0", "parameters", List.of(Map.of("type", "text", "text", args.get(0)))));
        }
        return Map.of("messaging_product", "whatsapp", "recipient_type", "individual", "to", phone, "type", "template", "template",
                Map.of("name", template, "language", Map.of("code", params.getOrDefault("lang", "vi")), "components", components));
    }

    private List<String> orderedParams(Map<String, String> params) {
        return params.entrySet().stream()
                .filter(e -> e.getKey().matches("p\\d+"))
                .sorted(Comparator.comparingInt(e -> Integer.parseInt(e.getKey().substring(1))))
                .map(Map.Entry::getValue)
                .toList();
    }

    private String post(Object payload) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + (accessToken == null ? "" : accessToken))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(payload)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());
            if (response.statusCode() >= 400) {
                JsonNode error = root.path("error");
                throw new IllegalStateException("whatsapp: send failed: http " + response.statusCode() + " code=" + error.path("code").asInt() + " msg=" + error.path("message").asText());
            }
            String id = root.path("messages").path(0).path("id").asText("");
            if (id.isBlank()) throw new IllegalStateException("whatsapp: send failed: empty messages array");
            return id;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("whatsapp: send failed: " + e.getMessage());
        }
    }
}
