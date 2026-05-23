package com.twohands.social_service.application.integration.consumeauthuserevents;

public enum AuthUserEventType {
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED,
    USER_SUSPENDED,
    USER_BANNED,
    USER_RESTRICTED,
    USER_ENFORCEMENT_REVOKED,
    USER_ENFORCEMENT_EXPIRED;

    public static AuthUserEventType fromValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("event_type is required");
        }
        return AuthUserEventType.valueOf(rawValue.trim().toUpperCase());
    }
}
