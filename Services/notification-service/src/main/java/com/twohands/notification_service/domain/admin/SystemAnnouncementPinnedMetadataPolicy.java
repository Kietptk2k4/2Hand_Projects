package com.twohands.notification_service.domain.admin;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Pin flag for system announcement notifications comes only from Admin payload (FR_PinSystemAnnouncementNotification).
 */
public final class SystemAnnouncementPinnedMetadataPolicy {

    private SystemAnnouncementPinnedMetadataPolicy() {
    }

    public static boolean resolveIsPinned(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return false;
        }
        JsonNode isPinned = payload.get("is_pinned");
        if (isPinned != null && !isPinned.isNull()) {
            return parseBooleanValue(isPinned, "is_pinned");
        }
        JsonNode pinned = payload.get("pinned");
        if (pinned != null && !pinned.isNull()) {
            return parseBooleanValue(pinned, "pinned");
        }
        return false;
    }

    private static boolean parseBooleanValue(JsonNode node, String fieldName) {
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isValueNode()) {
            String value = node.asText();
            if (value == null || value.isBlank()) {
                return false;
            }
            return Boolean.parseBoolean(value.trim());
        }
        throw new IllegalArgumentException(fieldName + " must be a boolean value.");
    }
}
