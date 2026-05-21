package com.twohands.notification_service.exception;

public class AppException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String field;
    private final String reason;

    public AppException(ErrorCode errorCode) {
        super(errorCode.defaultMessage());
        this.errorCode = errorCode;
        this.field = null;
        this.reason = null;
    }

    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.field = null;
        this.reason = null;
    }

    public AppException(ErrorCode errorCode, String message, String field, String reason) {
        super(message);
        this.errorCode = errorCode;
        this.field = field;
        this.reason = reason;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getField() {
        return field;
    }

    public String getReason() {
        return reason;
    }
}
