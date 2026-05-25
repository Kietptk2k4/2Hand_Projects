package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.commerce.OrderCreatedNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class OrderCreatedNotificationPayloadParser {

    private static final String ORDER_AGGREGATE_TYPE = "ORDER";

    private final ObjectMapper objectMapper;

    public OrderCreatedNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OrderCreatedNotificationContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload());

        UUID buyerId = firstUuid(
                event.recipientUserId(),
                textField(payload, "buyer_id"),
                textField(payload, "buyer_user_id")
        );
        if (buyerId == null) {
            throw new IllegalArgumentException("buyer_id is required for ORDER_CREATED notification event.");
        }

        String orderId = firstNonBlank(
                textField(payload, "order_id"),
                ORDER_AGGREGATE_TYPE.equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("order_id is required for ORDER_CREATED notification event.");
        }

        String orderCode = firstNonBlank(
                textField(payload, "order_code"),
                orderId
        );

        String totalAmountSummary = firstNonBlank(
                textField(payload, "total_amount"),
                textField(payload, "final_amount")
        );

        return new OrderCreatedNotificationContext(
                buyerId,
                orderId.trim(),
                orderCode.trim(),
                parseSellerIds(payload, textField(payload, "seller_id")),
                totalAmountSummary
        );
    }

    private List<UUID> parseSellerIds(JsonNode payload, String singleSellerId) {
        Set<UUID> sellerIds = new LinkedHashSet<>();
        addSellerId(sellerIds, singleSellerId);

        JsonNode sellerIdsNode = payload.get("seller_ids");
        if (sellerIdsNode != null && sellerIdsNode.isArray()) {
            for (JsonNode node : sellerIdsNode) {
                if (node != null && node.isTextual()) {
                    addSellerId(sellerIds, node.asText());
                }
            }
        }

        return List.copyOf(sellerIds);
    }

    private void addSellerId(Set<UUID> sellerIds, String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return;
        }
        try {
            sellerIds.add(UUID.fromString(rawValue.trim()));
        } catch (IllegalArgumentException ignored) {
            // Ignore invalid seller UUID values.
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
            throw new IllegalArgumentException("ORDER_CREATED event payload must be valid JSON.");
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

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }
}
