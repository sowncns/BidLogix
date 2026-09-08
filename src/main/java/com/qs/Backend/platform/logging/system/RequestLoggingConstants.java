package com.qs.Backend.platform.logging.system;

public final class RequestLoggingConstants {

    // Key under which the per-request correlation id is stored in SLF4J MDC.
    // Referenced by logback-spring.xml's %X{requestId} and by AuditLogService.
    public static final String REQUEST_ID_MDC_KEY = "requestId";

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    private RequestLoggingConstants() {
    }
}
