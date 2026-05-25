package com.twohands.notification_service.domain.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class SystemAnnouncementNotificationMetadataPolicy {

    private SystemAnnouncementNotificationMetadataPolicy() {
    }

    public static String build(
            ObjectMapper objectMapper,
            String announcementId,
            String severity,
            boolean isPinned,
            boolean dismissible
    ) {
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("announcement_id", announcementId);
        metadata.put("severity", severity);
        metadata.put("is_pinned", isPinned);
        metadata.put("dismissible", dismissible);
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }
}
