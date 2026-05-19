package com.twohands.social_service.domain.user;

public record UserProjection(
        String userId,
        String status,
        String displayName,
        String avatarUrl,
        Boolean isPrivate
) {
    public boolean isPrivateProfile() {
        return Boolean.TRUE.equals(isPrivate);
    }
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
