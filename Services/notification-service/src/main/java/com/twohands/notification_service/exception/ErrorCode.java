package com.twohands.notification_service.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INTERNAL_ERROR("NOTIFICATION-500", HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    BAD_REQUEST("NOTIFICATION-400", HttpStatus.BAD_REQUEST, "Invalid request"),
    VALIDATION_ERROR("NOTIFICATION-400-VALIDATION", HttpStatus.BAD_REQUEST, "Validation failed"),
    UNAUTHORIZED("NOTIFICATION-401", HttpStatus.UNAUTHORIZED, "Authentication required"),
    FORBIDDEN("NOTIFICATION-403", HttpStatus.FORBIDDEN, "Access denied"),
    RESOURCE_NOT_FOUND("NOTIFICATION-404", HttpStatus.NOT_FOUND, "Resource not found"),
    DUPLICATE_EVENT("NOTIFICATION-409-DUPLICATE-EVENT", HttpStatus.CONFLICT, "Event already ingested"),
    INTERNAL_API_DISABLED("NOTIFICATION-403-INTERNAL", HttpStatus.FORBIDDEN, "Internal ingest is disabled"),
    INVALID_INTERNAL_API_KEY("NOTIFICATION-401-INTERNAL", HttpStatus.UNAUTHORIZED, "Invalid internal API key");

    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(String code, HttpStatus status, String defaultMessage) {
        this.code = code;
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public String code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
