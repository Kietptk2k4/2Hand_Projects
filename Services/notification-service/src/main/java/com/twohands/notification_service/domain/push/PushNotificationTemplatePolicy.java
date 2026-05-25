package com.twohands.notification_service.domain.push;

import java.util.Optional;

public final class PushNotificationTemplatePolicy {

    private PushNotificationTemplatePolicy() {
    }

    public static Optional<PushNotificationTemplate> resolve(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(switch (eventType) {
            case "PASSWORD_CHANGED" -> new PushNotificationTemplate("Password changed", "Your account password was changed.");
            case "POST_LIKED" -> new PushNotificationTemplate("New like", "Someone liked your post.");
            case "USER_FOLLOWED" -> new PushNotificationTemplate("New follower", "Someone started following you.");
            case "COMMENT_CREATED" -> new PushNotificationTemplate("New comment", "Someone commented on your post.");
            case "COMMENT_REPLIED" -> new PushNotificationTemplate("New reply", "Someone replied to your comment.");
            case "COMMENT_LIKED" -> new PushNotificationTemplate("Comment liked", "Someone liked your comment.");
            case "ORDER_CREATED" -> new PushNotificationTemplate("Order confirmed", "Your order has been created.");
            case "PAYMENT_SUCCESS" -> new PushNotificationTemplate("Payment received", "Your payment was successful.");
            case "PAYMENT_FAILED" -> new PushNotificationTemplate("Payment failed", "Your payment could not be completed.");
            case "SHIPMENT_SHIPPED" -> new PushNotificationTemplate("Order shipped", "Your order is on the way.");
            case "SHIPMENT_DELIVERED" -> new PushNotificationTemplate("Order delivered", "Your order has been delivered.");
            case "ORDER_COMPLETED" -> new PushNotificationTemplate("Order completed", "Your order is complete.");
            case "USER_SUSPENDED" -> new PushNotificationTemplate("Account suspended", "Your account has been suspended.");
            case "USER_RESTRICTED" -> new PushNotificationTemplate("Account restricted", "Your account access has been restricted.");
            case "PRODUCT_REMOVED" -> new PushNotificationTemplate("Product removed", "One of your products was removed.");
            case "REVIEW_HIDDEN" -> new PushNotificationTemplate("Review hidden", "One of your reviews was hidden.");
            case "SHOP_SUSPENDED" -> new PushNotificationTemplate("Shop suspended", "Your shop has been suspended.");
            case "SYSTEM_ANNOUNCEMENT_SENT" -> new PushNotificationTemplate("System announcement", "You have a new announcement.");
            default -> null;
        });
    }
}
