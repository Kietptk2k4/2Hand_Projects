package com.twohands.auth_service.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INTERNAL_ERROR("AUTH-500", HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    BAD_REQUEST("AUTH-400", HttpStatus.BAD_REQUEST, "Invalid request"),
    VALIDATION_ERROR("AUTH-400-VALIDATION", HttpStatus.BAD_REQUEST, "Validation failed"),
    DUPLICATE_EMAIL("AUTH-409-DUPLICATE-EMAIL", HttpStatus.CONFLICT, "Email da duoc su dung."),
    REGISTER_RATE_LIMITED("AUTH-429-REGISTER", HttpStatus.TOO_MANY_REQUESTS, "Ban thao tac qua nhanh, vui long thu lai sau."),
    LOGIN_RATE_LIMITED("AUTH-429-LOGIN", HttpStatus.TOO_MANY_REQUESTS, "Ban thu dang nhap qua nhieu lan. Vui long thu lai sau."),
    REFRESH_RATE_LIMITED("AUTH-429-REFRESH", HttpStatus.TOO_MANY_REQUESTS, "Ban thao tac qua nhanh, vui long thu lai sau."),
    FORGOT_PASSWORD_RATE_LIMITED("AUTH-429-FORGOT-PASSWORD", HttpStatus.TOO_MANY_REQUESTS,
            "Ban thao tac qua nhanh, vui long thu lai sau."),
    RESEND_EMAIL_VERIFICATION_RATE_LIMITED("AUTH-429-RESEND-EMAIL-VERIFICATION", HttpStatus.TOO_MANY_REQUESTS,
            "Ban thao tac qua nhanh, vui long thu lai sau."),
    AVATAR_UPLOAD_RATE_LIMITED("AUTH-429-AVATAR-UPLOAD", HttpStatus.TOO_MANY_REQUESTS,
            "Ban thao tac qua nhanh, vui long thu lai sau."),
    OBJECT_STORAGE_UNAVAILABLE("AUTH-503-OBJECT-STORAGE", HttpStatus.SERVICE_UNAVAILABLE,
            "Dich vu luu tru tam thoi khong kha dung."),
    INVALID_LOGIN_CREDENTIALS("AUTH-401-INVALID-CREDENTIALS", HttpStatus.UNAUTHORIZED, "Email hoac mat khau khong chinh xac."),
    INVALID_REFRESH_SESSION("AUTH-401-INVALID-REFRESH-SESSION", HttpStatus.UNAUTHORIZED,
            "Phien dang nhap khong hop le hoac da het han. Vui long dang nhap lai."),
    OAUTH_EMAIL_MISSING("AUTH-400-OAUTH-EMAIL-MISSING", HttpStatus.BAD_REQUEST,
            "Vui long cap quyen Email de su dung tinh nang nay."),
    OAUTH_PROVIDER_PROFILE_INVALID("AUTH-401-OAUTH-PROFILE-INVALID", HttpStatus.UNAUTHORIZED,
            "Xac thuc OAuth that bai."),
    OAUTH_ACCOUNT_UNAVAILABLE("AUTH-403-OAUTH-ACCOUNT-UNAVAILABLE", HttpStatus.FORBIDDEN,
            "Tai khoan hien khong kha dung."),
    ACCOUNT_SUSPENDED("AUTH-403-ACCOUNT-SUSPENDED", HttpStatus.FORBIDDEN, "Tai khoan cua ban da bi khoa tam thoi."),
    ADMIN_PORTAL_ACCESS_DENIED("AUTH-403-ADMIN-PORTAL", HttpStatus.FORBIDDEN,
            "Tai khoan khong co quyen truy cap admin portal."),
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
