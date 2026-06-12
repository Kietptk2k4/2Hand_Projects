package com.twohands.social_service.domain.comment;

public enum CommentModerationAction {
    HIDE,
    REMOVE,
    RESTORE;

    public static CommentModerationAction fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return CommentModerationAction.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
