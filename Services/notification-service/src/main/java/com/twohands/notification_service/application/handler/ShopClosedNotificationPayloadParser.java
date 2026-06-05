package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.admin.ShopClosedNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ShopClosedNotificationPayloadParser {

    private static final String SHOP_AGGREGATE_TYPE = "SHOP";
    private static final String REFERENCE_TYPE = "SHOP";

    private final ObjectMapper objectMapper;

    public ShopClosedNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ShopClosedNotificationContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload());

        UUID shopOwnerId = firstUuid(
                event.recipientUserId(),
                textField(payload, "shop_owner_id"),
                textField(payload, "owner_id"),
                textField(payload, "seller_user_id")
        );
        if (shopOwnerId == null) {
            throw new IllegalArgumentException("shop_owner_id is required for SHOP_CLOSED notification event.");
        }

        String shopId = firstNonBlank(
                textField(payload, "shop_id"),
                SHOP_AGGREGATE_TYPE.equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );
        if (shopId == null || shopId.isBlank()) {
            throw new IllegalArgumentException("shop_id is required for SHOP_CLOSED notification event.");
        }

        String closeReason = firstNonBlank(
                textField(payload, "reason"),
                textField(payload, "close_reason")
        );

        return new ShopClosedNotificationContext(
                shopOwnerId,
                shopId.trim(),
                closeReason,
                REFERENCE_TYPE,
                shopId.trim()
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
            throw new IllegalArgumentException("SHOP_CLOSED event payload must be valid JSON.");
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
