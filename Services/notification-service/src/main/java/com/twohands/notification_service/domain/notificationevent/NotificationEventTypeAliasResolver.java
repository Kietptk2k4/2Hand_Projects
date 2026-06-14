package com.twohands.notification_service.domain.notificationevent;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationEventTypeAliasResolver {

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("COMMERCE_ORDER_CREATED", "ORDER_CREATED"),
            Map.entry("COMMERCE_PAYMENT_PAID", "PAYMENT_SUCCESS"),
            Map.entry("COMMERCE_PAYMENT_FAILED", "PAYMENT_FAILED"),
            Map.entry("COMMERCE_PAYMENT_REFUNDED", "PAYMENT_REFUNDED"),
            Map.entry("COMMERCE_ORDER_CANCELLED", "ORDER_CANCELLED"),
            Map.entry("COMMERCE_ORDER_COMPLETED", "ORDER_COMPLETED"),
            Map.entry("COMMERCE_SHIPMENT_CREATED", "SHIPMENT_CREATED"),
            Map.entry("COMMERCE_SHIPMENT_READY_TO_SHIP", "SHIPMENT_READY_TO_SHIP"),
            Map.entry("COMMERCE_SHIPMENT_CANCELLED", "SHIPMENT_CANCELLED"),
            Map.entry("COMMERCE_ORDER_CANCEL_PENDING_REFUND", "ORDER_CANCEL_PENDING_REFUND"),
            Map.entry("COMMERCE_SHIPMENT_SHIPPED", "SHIPMENT_SHIPPED"),
            Map.entry("COMMERCE_SHIPMENT_DELIVERED", "SHIPMENT_DELIVERED"),
            Map.entry("COMMERCE_REVIEW_REMINDER", "REVIEW_REMINDER"),
            Map.entry("COMMERCE_REVIEW_REPLIED", "REVIEW_REPLIED"),
            Map.entry("COMMERCE_PAYOUT_REQUEST_APPROVED", "PAYOUT_REQUEST_APPROVED"),
            Map.entry("SYSTEM_ANNOUNCEMENT_PUBLISHED", "SYSTEM_ANNOUNCEMENT_SENT")
            // SYSTEM_ANNOUNCEMENT_CANCELLED has a dedicated handler (no alias)
    );

    public String resolve(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            return eventType;
        }
        String normalized = eventType.trim().toUpperCase();
        return ALIASES.getOrDefault(normalized, normalized);
    }
}
