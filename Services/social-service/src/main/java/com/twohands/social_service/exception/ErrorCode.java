package com.twohands.social_service.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INTERNAL_ERROR("SOCIAL-500", HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    BAD_REQUEST("SOCIAL-400", HttpStatus.BAD_REQUEST, "Invalid request"),
    VALIDATION_ERROR("SOCIAL-400-VALIDATION", HttpStatus.BAD_REQUEST, "Validation failed"),
    UNAUTHORIZED("SOCIAL-401", HttpStatus.UNAUTHORIZED, "Authentication required"),
    FORBIDDEN("SOCIAL-403", HttpStatus.FORBIDDEN, "Access denied"),
    RESOURCE_NOT_FOUND("SOCIAL-404", HttpStatus.NOT_FOUND, "Resource not found"),
    INVALID_PAGINATION("SOCIAL-400-PAGINATION", HttpStatus.BAD_REQUEST, "Invalid pagination parameters");

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
