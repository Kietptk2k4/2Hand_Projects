package com.twohands.commerce_service.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INTERNAL_ERROR("COMMERCE-500", HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    BAD_REQUEST("COMMERCE-400", HttpStatus.BAD_REQUEST, "Invalid request"),
    VALIDATION_ERROR("COMMERCE-400-VALIDATION", HttpStatus.BAD_REQUEST, "Validation failed"),
    UNAUTHORIZED("COMMERCE-401", HttpStatus.UNAUTHORIZED, "Authentication required"),
    FORBIDDEN("COMMERCE-403", HttpStatus.FORBIDDEN, "Access denied"),
    RESOURCE_NOT_FOUND("COMMERCE-404", HttpStatus.NOT_FOUND, "Resource not found"),
    INVALID_PAGINATION("COMMERCE-400-PAGINATION", HttpStatus.BAD_REQUEST, "Invalid pagination parameters");

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
