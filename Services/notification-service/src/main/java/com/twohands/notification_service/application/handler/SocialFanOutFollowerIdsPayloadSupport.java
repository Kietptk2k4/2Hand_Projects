package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class SocialFanOutFollowerIdsPayloadSupport {

    private SocialFanOutFollowerIdsPayloadSupport() {
    }

    static List<UUID> parseFollowerUserIds(JsonNode payload) {
        List<UUID> followerUserIds = new ArrayList<>();
        JsonNode recipientIds = payload.get("follower_user_ids");
        if (recipientIds == null || !recipientIds.isArray()) {
            recipientIds = payload.get("recipient_user_ids");
        }
        if (recipientIds != null && recipientIds.isArray()) {
            for (JsonNode node : recipientIds) {
                if (node != null && node.isTextual()) {
                    UUID parsed = parseUuid(node.asText());
                    if (parsed != null) {
                        followerUserIds.add(parsed);
                    }
                }
            }
        }
        return followerUserIds;
    }

    private static UUID parseUuid(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(rawValue.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
