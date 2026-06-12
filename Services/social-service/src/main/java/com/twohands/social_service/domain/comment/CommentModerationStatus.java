package com.twohands.social_service.domain.comment;

public enum CommentModerationStatus {
    NONE,
    HIDDEN,
    REMOVED;

    public boolean isHiddenFromDiscovery() {
        return this == HIDDEN;
    }
}
