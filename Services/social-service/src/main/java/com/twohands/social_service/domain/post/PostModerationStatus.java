package com.twohands.social_service.domain.post;

public enum PostModerationStatus {
    NONE,
    HIDDEN,
    REMOVED;

    public boolean isHiddenFromDiscovery() {
        return this == HIDDEN;
    }
}
