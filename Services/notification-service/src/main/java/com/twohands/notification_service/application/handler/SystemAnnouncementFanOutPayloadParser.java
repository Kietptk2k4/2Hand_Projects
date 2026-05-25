package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.admin.SystemAnnouncementDismissibleMetadataPolicy;
import com.twohands.notification_service.domain.admin.SystemAnnouncementFanOutContext;
import com.twohands.notification_service.domain.admin.SystemAnnouncementNotificationMetadataPolicy;
import com.twohands.notification_service.domain.admin.SystemAnnouncementPinnedMetadataPolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class SystemAnnouncementFanOutPayloadParser {

    private static final String ANNOUNCEMENT_AGGREGATE_TYPE = "ANNOUNCEMENT";
    private static final String REFERENCE_TYPE = "SYSTEM_ANNOUNCEMENT";

    private final ObjectMapper objectMapper;

    public SystemAnnouncementFanOutPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SystemAnnouncementFanOutContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload());

        String announcementId = firstNonBlank(
                textField(payload, "announcement_id"),
                ANNOUNCEMENT_AGGREGATE_TYPE.equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );
        if (announcementId == null || announcementId.isBlank()) {
            throw new IllegalArgumentException("announcement_id is required for SYSTEM_ANNOUNCEMENT_SENT event.");
        }

        String title = textField(payload, "title");
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required for SYSTEM_ANNOUNCEMENT_SENT event.");
        }

        String content = textField(payload, "content");
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is required for SYSTEM_ANNOUNCEMENT_SENT event.");
        }

        String severity = textField(payload, "severity");
        if (severity == null || severity.isBlank()) {
            throw new IllegalArgumentException("severity is required for SYSTEM_ANNOUNCEMENT_SENT event.");
        }
        String normalizedSeverity = SystemAnnouncementNotificationMetadataPolicy.normalizeSeverity(severity);

        boolean isPinned = SystemAnnouncementPinnedMetadataPolicy.resolveIsPinned(payload);
        boolean dismissible = SystemAnnouncementDismissibleMetadataPolicy.resolveDismissible(payload);
        List<UUID> explicitRecipients = parseRecipientUserIds(payload);
        String targetAudience = textField(payload, "target_audience");

        return new SystemAnnouncementFanOutContext(
                announcementId.trim(),
                title.trim(),
                content.trim(),
                normalizedSeverity,
                isPinned,
                dismissible,
                explicitRecipients,
                targetAudience,
                REFERENCE_TYPE,
                announcementId.trim()
        );
    }

    private List<UUID> parseRecipientUserIds(JsonNode payload) {
        Set<UUID> recipients = new LinkedHashSet<>();
        JsonNode recipientIds = payload.get("recipient_user_ids");
        if (recipientIds != null && recipientIds.isArray()) {
            for (JsonNode node : recipientIds) {
                UUID parsed = parseUuid(node.isValueNode() ? node.asText() : null);
                if (parsed != null) {
                    recipients.add(parsed);
                }
            }
        }
        addRecipient(recipients, payload, "recipient_user_id");
        return new ArrayList<>(recipients);
    }

    private void addRecipient(Set<UUID> recipients, JsonNode payload, String field) {
        UUID parsed = parseUuid(textField(payload, field));
        if (parsed != null) {
            recipients.add(parsed);
        }
    }

    private JsonNode parsePayload(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(rawPayload);
            return node == null || node.isNull() ? objectMapper.createObjectNode() : node;
        } catch (Exception ex) {
            throw new IllegalArgumentException("SYSTEM_ANNOUNCEMENT_SENT event payload must be valid JSON.");
        }
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String textField(JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        if (node == null || node.isNull() || !node.isValueNode()) {
            return null;
        }
        String value = node.asText();
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
