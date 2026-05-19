package com.twohands.social_service.domain.user;

public record UserProjection(
        String userId,
        String status,
        String displayName,
        String avatarUrl
) {
    public boolean isSuspended() {
        return "SUSPENDED".equalsIgnoreCase(status);
    }

    public boolean isDeleted() {
        return "DELETED".equalsIgnoreCase(status);
    }

    public boolean isActionForbidden() {
        return isSuspended() || isDeleted();
    }
}
