package com.twohands.auth_service.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INTERNAL_ERROR("AUTH-500", HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    BAD_REQUEST("AUTH-400", HttpStatus.BAD_REQUEST, "Invalid request"),
    VALIDATION_ERROR("AUTH-400-VALIDATION", HttpStatus.BAD_REQUEST, "Validation failed"),
    DUPLICATE_EMAIL("AUTH-409-DUPLICATE-EMAIL", HttpStatus.CONFLICT, "Email da duoc su dung."),
    REGISTER_RATE_LIMITED("AUTH-429-REGISTER", HttpStatus.TOO_MANY_REQUESTS, "Ban thao tac qua nhanh, vui long thu lai sau."),
    LOGIN_RATE_LIMITED("AUTH-429-LOGIN", HttpStatus.TOO_MANY_REQUESTS, "Ban thu dang nhap qua nhieu lan. Vui long thu lai sau."),
    INVALID_LOGIN_CREDENTIALS("AUTH-401-INVALID-CREDENTIALS", HttpStatus.UNAUTHORIZED, "Email hoac mat khau khong chinh xac."),
    ACCOUNT_SUSPENDED("AUTH-403-ACCOUNT-SUSPENDED", HttpStatus.FORBIDDEN, "Tai khoan cua ban da bi khoa tam thoi."),
    UNAUTHORIZED("AUTH-401", HttpStatus.UNAUTHORIZED, "Authentication required"),
    FORBIDDEN("AUTH-403", HttpStatus.FORBIDDEN, "Access denied"),
    RESOURCE_NOT_FOUND("AUTH-404", HttpStatus.NOT_FOUND, "Resource not found"),
    CONFLICT("AUTH-409", HttpStatus.CONFLICT, "Resource conflict"),
    TOO_MANY_REQUESTS("AUTH-429", HttpStatus.TOO_MANY_REQUESTS, "Too many requests");

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
