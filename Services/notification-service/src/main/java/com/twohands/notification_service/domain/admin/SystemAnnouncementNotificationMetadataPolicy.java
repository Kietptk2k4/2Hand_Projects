package com.twohands.notification_service.domain.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Set;

public final class SystemAnnouncementNotificationMetadataPolicy {

    private static final Set<String> ALLOWED_SEVERITIES = Set.of("INFO", "WARNING", "CRITICAL");

    private SystemAnnouncementNotificationMetadataPolicy() {
    }

    public static String build(
            ObjectMapper objectMapper,
            String announcementId,
            String severity,
            boolean isPinned,
            boolean dismissible
    ) {
        String normalizedSeverity = normalizeSeverity(severity);
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("announcement_id", announcementId);
        metadata.put("severity", normalizedSeverity);
        metadata.put("is_pinned", isPinned);
        metadata.put("dismissible", dismissible);
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to build system announcement notification metadata.");
        }
    }

    public static String normalizeSeverity(String severity) {
        if (severity == null || severity.isBlank()) {
            throw new IllegalArgumentException("severity is required for system announcement notification metadata.");
        }
        String normalized = severity.trim().toUpperCase();
        if (!ALLOWED_SEVERITIES.contains(normalized)) {
            throw new IllegalArgumentException("severity must be INFO, WARNING, or CRITICAL.");
        }
        return normalized;
    }

    /**
     * Ensures stored metadata exposes a boolean {@code is_pinned} for clients (read-path sanitize).
     */
    public static String sanitizeForResponse(ObjectMapper objectMapper, String rawMetadata) {
        if (rawMetadata == null || rawMetadata.isBlank()) {
            return "{}";
        }
        try {
            JsonNode root = objectMapper.readTree(rawMetadata);
            if (!root.isObject()) {
                return "{}";
            }
            ObjectNode sanitized = ((ObjectNode) root).deepCopy();
            if (!sanitized.has("is_pinned")) {
                sanitized.put("is_pinned", false);
            } else {
                JsonNode pinNode = sanitized.get("is_pinned");
                sanitized.put("is_pinned", pinNode.isBoolean() && pinNode.asBoolean());
            }
            return objectMapper.writeValueAsString(sanitized);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }
}
