package com.qs.Backend.platform.logging.system.fillter;

import com.qs.Backend.platform.logging.system.RequestLoggingConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

// Assigns a correlation id to every request (reused from the caller's header if present),
// exposes it via MDC for logback (%X{requestId}) and downstream code (AuditLogService),
// and logs one line per request with method/path/status/duration.
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = firstNonBlank(request.getHeader(RequestLoggingConstants.REQUEST_ID_HEADER), UUID.randomUUID().toString());
        MDC.put(RequestLoggingConstants.REQUEST_ID_MDC_KEY, requestId);
        response.setHeader(RequestLoggingConstants.REQUEST_ID_HEADER, requestId);

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            log.info("{} {} -> {} ({}ms)", request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs);
            MDC.remove(RequestLoggingConstants.REQUEST_ID_MDC_KEY);
        }
    }

    private static String firstNonBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
