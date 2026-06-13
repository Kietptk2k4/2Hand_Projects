package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.commerce.ReviewRepliedNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReviewRepliedNotificationPayloadParser {

    private final ObjectMapper objectMapper;

    public ReviewRepliedNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ReviewRepliedNotificationContext parse(NotificationEvent event) {
        JsonNode payload = readPayload(event.payload());

        UUID buyerId = readUuid(payload, "buyer_id", "buyerId");
        if (buyerId == null) {
            throw new IllegalArgumentException("buyer_id is required for REVIEW_REPLIED notification event.");
        }

        UUID sellerId = readUuid(payload, "seller_id", "sellerId");
        if (sellerId == null) {
            throw new IllegalArgumentException("seller_id is required for REVIEW_REPLIED notification event.");
        }

        UUID reviewId = readUuid(payload, "review_id", "reviewId");
        if (reviewId == null && event.aggregateId() != null && !event.aggregateId().isBlank()) {
            reviewId = UUID.fromString(event.aggregateId());
        }
        if (reviewId == null) {
            throw new IllegalArgumentException("review_id is required for REVIEW_REPLIED notification event.");
        }

        UUID productId = readUuid(payload, "product_id", "productId");

        return new ReviewRepliedNotificationContext(buyerId, sellerId, reviewId, productId);
    }

    private JsonNode readPayload(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("REVIEW_REPLIED event payload must be valid JSON.");
        }
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            throw new IllegalArgumentException("REVIEW_REPLIED event payload must be valid JSON.", ex);
        }
    }

    private UUID readUuid(JsonNode payload, String snakeKey, String camelKey) {
        String value = textValue(payload, snakeKey);
        if (value == null) {
            value = textValue(payload, camelKey);
        }
        if (value == null || value.isBlank()) {
            return null;
        }
        return UUID.fromString(value);
    }

    private String textValue(JsonNode payload, String key) {
        JsonNode node = payload.get(key);
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asText();
    }
}