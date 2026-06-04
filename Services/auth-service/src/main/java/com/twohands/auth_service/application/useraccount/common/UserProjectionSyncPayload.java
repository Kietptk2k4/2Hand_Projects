package com.twohands.auth_service.application.useraccount.common;

import java.util.Map;

public record UserProjectionSyncPayload(
        String status,
        String displayName,
        String avatarUrl,
        Boolean isPrivate
) {
    public static UserProjectionSyncPayload empty() {
        return new UserProjectionSyncPayload(null, null, null, null);
    }

    public static UserProjectionSyncPayload statusOnly(String status) {
        return new UserProjectionSyncPayload(status, null, null, null);
    }

    public static UserProjectionSyncPayload profileOnly(String displayName, String avatarUrl, Boolean isPrivate) {
        return new UserProjectionSyncPayload(null, displayName, avatarUrl, isPrivate);
    }

    public void applyTo(Map<String, Object> payload) {
        putIfPresent(payload, "status", status);
        putIfPresent(payload, "display_name", displayName);
        putIfPresent(payload, "avatar_url", avatarUrl);
        if (isPrivate != null) {
            payload.put("is_private", isPrivate);
        }
    }

    private static void putIfPresent(Map<String, Object> payload, String key, String value) {
        if (value != null && !value.isBlank()) {
            payload.put(key, value);
        }
    }
}
