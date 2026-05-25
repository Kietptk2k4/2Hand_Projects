package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.commerce.ReviewReminderNotificationContext;
import com.twohands.notification_service.domain.commerce.ReviewReminderReferencePolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReviewReminderNotificationPayloadParser {

    private static final String ORDER_ITEM_AGGREGATE_TYPE = "ORDER_ITEM";

    private final ObjectMapper objectMapper;

    public ReviewReminderNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ReviewReminderNotificationContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload());

        UUID buyerId = firstUuid(
                event.recipientUserId(),
                textField(payload, "buyer_id"),
                textField(payload, "buyer_user_id")
        );
        if (buyerId == null) {
            throw new IllegalArgumentException("buyer_id is required for REVIEW_REMINDER notification event.");
        }

        String orderItemId = firstNonBlank(
                textField(payload, "order_item_id"),
                ORDER_ITEM_AGGREGATE_TYPE.equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );
        if (orderItemId == null || orderItemId.isBlank()) {
            throw new IllegalArgumentException("order_item_id is required for REVIEW_REMINDER notification event.");
        }

        String orderId = textField(payload, "order_id");
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("order_id is required for REVIEW_REMINDER notification event.");
        }

        int reminderDay = parseReminderDay(payload);
        String productId = textField(payload, "product_id");
        String referenceType = ReviewReminderReferencePolicy.resolveReferenceType(productId);
        String referenceId = ReviewReminderReferencePolicy.resolveReferenceId(productId, orderId);

        return new ReviewReminderNotificationContext(
                buyerId,
                orderItemId.trim(),
                orderId.trim(),
                firstNonBlank(textField(payload, "order_code"), orderId.trim()),
                productId,
                textField(payload, "product_name"),
                reminderDay,
                referenceType,
                referenceId,
                resolveAlreadyReviewed(payload)
        );
    }

    private int parseReminderDay(JsonNode payload) {
        JsonNode reminderDay = payload.get("reminder_day");
        if (reminderDay == null || reminderDay.isNull()) {
            throw new IllegalArgumentException("reminder_day is required for REVIEW_REMINDER notification event.");
        }
        if (reminderDay.isInt() || reminderDay.isLong()) {
            return reminderDay.asInt();
        }
        if (reminderDay.isValueNode()) {
            try {
                return Integer.parseInt(reminderDay.asText().trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("reminder_day must be a valid integer.");
            }
        }
        throw new IllegalArgumentException("reminder_day must be a valid integer.");
    }

    private boolean resolveAlreadyReviewed(JsonNode payload) {
        return booleanField(payload, "already_reviewed")
                || booleanField(payload, "review_exists")
                || booleanField(payload, "has_review");
    }

    private boolean booleanField(JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        if (node == null || node.isNull()) {
            return false;
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isValueNode()) {
            String value = node.asText();
            return value != null && Boolean.parseBoolean(value.trim());
        }
        return false;
    }

    private JsonNode parsePayload(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(rawPayload);
            return node == null || node.isNull() ? objectMapper.createObjectNode() : node;
        } catch (Exception ex) {
            throw new IllegalArgumentException("REVIEW_REMINDER event payload must be valid JSON.");
        }
    }

    private UUID firstUuid(UUID primary, String firstFallback, String secondFallback) {
        if (primary != null) {
            return primary;
        }
        UUID first = parseUuid(firstFallback);
        if (first != null) {
            return first;
        }
        return parseUuid(secondFallback);
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
