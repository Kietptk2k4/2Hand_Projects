package com.twohands.auth_service.domain.enforcement;

public enum UserEnforcementActionType {
    SUSPEND,
    BAN,
    RESTRICT,
    REVOKE,
    EXPIRE;

    public static UserEnforcementActionType fromEventType(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("event_type is required");
        }
        return switch (eventType.trim().toUpperCase()) {
            case "USER_SUSPENDED" -> SUSPEND;
            case "USER_BANNED" -> BAN;
            case "USER_RESTRICTED" -> RESTRICT;
            case "USER_ENFORCEMENT_REVOKED" -> REVOKE;
            case "USER_ENFORCEMENT_EXPIRED" -> EXPIRE;
            default -> UserEnforcementActionType.valueOf(eventType.trim().toUpperCase());
        };
    }

    public boolean blocksLogin() {
        return this == SUSPEND || this == BAN;
    }
}
