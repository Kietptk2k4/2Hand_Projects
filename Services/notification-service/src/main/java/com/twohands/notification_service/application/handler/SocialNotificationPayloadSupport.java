package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;

final class SocialNotificationPayloadSupport {

    static final String FALLBACK_ACTOR_LABEL = "Someone you follow";

    private SocialNotificationPayloadSupport() {
    }

    static String parseActorDisplayName(JsonNode payload) {
        return firstNonBlank(
                textField(payload, "actor_display_name"),
                textField(payload, "display_name"),
                textField(payload, "actor_name")
        );
    }

    static String resolveActorLabel(String actorDisplayName) {
        if (actorDisplayName != null && !actorDisplayName.isBlank()) {
            return actorDisplayName.trim();
        }
        return FALLBACK_ACTOR_LABEL;
    }

    private static String textField(JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            String value = node.asText();
            return value == null || value.isBlank() ? null : value.trim();
        }
        return null;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
