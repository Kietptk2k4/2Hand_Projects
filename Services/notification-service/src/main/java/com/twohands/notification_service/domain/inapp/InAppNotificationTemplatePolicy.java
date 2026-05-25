package com.twohands.notification_service.domain.inapp;

import java.util.Optional;

public final class InAppNotificationTemplatePolicy {

    private InAppNotificationTemplatePolicy() {
    }

    public static final String SELLER_TEMPLATE_VARIANT = "seller";

    public static Optional<InAppNotificationTemplate> resolve(String eventType) {
        return resolve(eventType, null);
    }

    public static Optional<InAppNotificationTemplate> resolve(String eventType, String templateVariant) {
        if (eventType == null || eventType.isBlank()) {
            return Optional.empty();
        }
        if ("ORDER_CREATED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "New order",
                    "You have received a new order."
            ));
        }
        if ("SHIPMENT_CREATED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Shipment created",
                    "A shipment was created for an order you are fulfilling."
            ));
        }
        return Optional.ofNullable(switch (eventType) {
            case "POST_LIKED" -> new InAppNotificationTemplate("New like", "Someone liked your post.");
            case "USER_FOLLOWED" -> new InAppNotificationTemplate("New follower", "Someone started following you.");
            case "COMMENT_CREATED" -> new InAppNotificationTemplate("New comment", "Someone commented on your post.");
            case "COMMENT_REPLIED" -> new InAppNotificationTemplate("New reply", "Someone replied to your comment.");
            case "COMMENT_LIKED" -> new InAppNotificationTemplate("Comment liked", "Someone liked your comment.");
            case "ORDER_CREATED" -> new InAppNotificationTemplate(
                    "Order confirmed",
                    "Your order has been placed successfully."
            );
            case "PAYMENT_SUCCESS" -> new InAppNotificationTemplate(
                    "Payment received",
                    "Your payment was successful."
            );
            case "PAYMENT_FAILED" -> new InAppNotificationTemplate(
                    "Payment failed",
                    "Your payment could not be completed."
            );
            case "SHIPMENT_CREATED" -> new InAppNotificationTemplate(
                    "Shipment created",
                    "A shipment has been created for your order."
            );
            case "SHIPMENT_SHIPPED" -> new InAppNotificationTemplate(
                    "Order shipped",
                    "Your order is on the way."
            );
            case "SHIPMENT_DELIVERED" -> new InAppNotificationTemplate(
                    "Order delivered",
                    "Your order has been delivered."
            );
            default -> null;
        });
    }
}
