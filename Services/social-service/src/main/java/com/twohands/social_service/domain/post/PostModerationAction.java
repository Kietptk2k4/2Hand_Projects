package com.twohands.social_service.domain.post;

public enum PostModerationAction {
    HIDE,
    REMOVE,
    RESTORE;

    public static PostModerationAction fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return PostModerationAction.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
