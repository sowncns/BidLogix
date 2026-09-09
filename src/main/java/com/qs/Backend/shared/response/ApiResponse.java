package com.qs.Backend.shared.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private int statusCode;
    private String message;
    private T data;
    private Object errorDetails;

    @Builder.Default
    private Instant timestamp = Instant.now();

    // The qs-crm-be Go backend never wraps responses: success bodies are the
    // bare payload, error bodies are {"error": "..."}. FE was written against
    // that shape. @JsonValue makes Jackson serialize this envelope AS that
    // bare shape instead, so callers can keep building ApiResponse<T> as
    // before without every controller reaching into the wrapper.
    @JsonValue
    public Object toJson() {
        return success ? data : Map.of("error", message == null ? "" : message);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(201)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int statusCode, String message, Object errorDetails) {
        return ApiResponse.<T>builder()
                .success(false)
                .statusCode(statusCode)
                .message(message)
                .errorDetails(errorDetails)
                .build();
    }
}