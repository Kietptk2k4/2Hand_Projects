package com.twohands.notification_service.domain.admin;

import com.fasterxml.jackson.databind.JsonNode;

public final class SystemAnnouncementDismissibleMetadataPolicy {

    private SystemAnnouncementDismissibleMetadataPolicy() {
    }

    public static boolean resolveDismissible(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return false;
        }
        JsonNode dismissible = payload.get("dismissible");
        if (dismissible == null || dismissible.isNull()) {
            return false;
        }
        if (dismissible.isBoolean()) {
            return dismissible.asBoolean();
        }
        if (dismissible.isValueNode()) {
            String value = dismissible.asText();
            if (value == null || value.isBlank()) {
                return false;
            }
            return Boolean.parseBoolean(value.trim());
        }
        throw new IllegalArgumentException("dismissible must be a boolean value.");
    }
}
