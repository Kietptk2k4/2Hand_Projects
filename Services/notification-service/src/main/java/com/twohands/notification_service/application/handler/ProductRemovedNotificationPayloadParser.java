package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.admin.ProductRemovedNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductRemovedNotificationPayloadParser {

    private static final String PRODUCT_AGGREGATE_TYPE = "PRODUCT";
    private static final String REFERENCE_TYPE = "PRODUCT";

    private final ObjectMapper objectMapper;

    public ProductRemovedNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ProductRemovedNotificationContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload());

        UUID sellerUserId = firstUuid(
                event.recipientUserId(),
                textField(payload, "seller_user_id"),
                textField(payload, "seller_id"),
                textField(payload, "owner_user_id")
        );
        if (sellerUserId == null) {
            throw new IllegalArgumentException("seller_user_id is required for PRODUCT_REMOVED notification event.");
        }

        String productId = firstNonBlank(
                textField(payload, "product_id"),
                PRODUCT_AGGREGATE_TYPE.equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("product_id is required for PRODUCT_REMOVED notification event.");
        }

        String removalReason = firstNonBlank(
                textField(payload, "removal_reason"),
                textField(payload, "user_reason")
        );

        return new ProductRemovedNotificationContext(
                sellerUserId,
                productId.trim(),
                removalReason,
                REFERENCE_TYPE,
                productId.trim()
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
            throw new IllegalArgumentException("PRODUCT_REMOVED event payload must be valid JSON.");
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
