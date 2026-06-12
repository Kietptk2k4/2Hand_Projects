package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.admin.ReviewHiddenNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReviewHiddenNotificationPayloadParser {

    private static final String REVIEW_AGGREGATE_TYPE = "REVIEW";
    private static final String REFERENCE_TYPE = "REVIEW";

    private final ObjectMapper objectMapper;

    public ReviewHiddenNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ReviewHiddenNotificationContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload());

        UUID reviewAuthorId = firstUuid(
                event.recipientUserId(),
                textField(payload, "review_author_id"),
                textField(payload, "author_id"),
                textField(payload, "author_user_id")
        );
        UUID sellerUserId = firstUuid(
                null,
                textField(payload, "seller_user_id"),
                textField(payload, "seller_id")
        );

        String eventType = event.eventType() == null ? "REVIEW_HIDDEN" : event.eventType();

        if (reviewAuthorId == null && sellerUserId == null) {
            throw new IllegalArgumentException(
                    "review_author_id or seller_user_id is required for " + eventType + " notification event."
            );
        }

        String reviewId = firstNonBlank(
                textField(payload, "review_id"),
                REVIEW_AGGREGATE_TYPE.equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );
        if (reviewId == null || reviewId.isBlank()) {
            throw new IllegalArgumentException("review_id is required for " + eventType + " notification event.");
        }

        String hiddenReason = firstNonBlank(
                textField(payload, "hidden_reason"),
                textField(payload, "removal_reason"),
                textField(payload, "restored_reason"),
                textField(payload, "reason"),
                textField(payload, "user_reason")
        );

        return new ReviewHiddenNotificationContext(
                reviewAuthorId,
                sellerUserId,
                reviewId.trim(),
                hiddenReason,
                REFERENCE_TYPE,
                reviewId.trim()
        );
    }

    private JsonNode parsePayload(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(rawPayload);
            return node == null || node.isNull() ? objectMapper.createObjectNode() : node;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Review moderation event payload must be valid JSON.");
        }
    }

    private UUID firstUuid(UUID primary, String... fallbacks) {
        if (primary != null) {
            return primary;
        }
        for (String fallback : fallbacks) {
            UUID parsed = parseUuid(fallback);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
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
