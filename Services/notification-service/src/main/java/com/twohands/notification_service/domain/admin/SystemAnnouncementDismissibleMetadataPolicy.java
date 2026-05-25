package com.twohands.notification_service.domain.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    public static boolean isDismissibleFromStoredMetadata(ObjectMapper objectMapper, String metadata) {
        if (metadata == null || metadata.isBlank()) {
            return false;
        }
        try {
            JsonNode root = objectMapper.readTree(metadata);
            if (!root.isObject()) {
                return false;
            }
            return resolveDismissible(root);
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            return false;
        }
    }
}
