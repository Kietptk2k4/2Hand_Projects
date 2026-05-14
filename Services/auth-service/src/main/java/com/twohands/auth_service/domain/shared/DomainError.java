package com.twohands.auth_service.domain.shared;

public abstract class DomainError extends RuntimeException {
    private final String code;

    protected DomainError(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
