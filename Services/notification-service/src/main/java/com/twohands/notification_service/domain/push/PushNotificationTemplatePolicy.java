package com.twohands.notification_service.domain.push;

import com.twohands.notification_service.domain.inapp.InAppNotificationTemplatePolicy;

import java.util.Optional;

public final class PushNotificationTemplatePolicy {

    private PushNotificationTemplatePolicy() {
    }

    public static final String SELLER_TEMPLATE_VARIANT = "seller";

    public static Optional<PushNotificationTemplate> resolve(String eventType) {
        return resolve(eventType, null);
    }

    public static Optional<PushNotificationTemplate> resolve(String eventType, String templateVariant) {
        if (eventType == null || eventType.isBlank()) {
            return Optional.empty();
        }
        if ("ORDER_CREATED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "New order",
                    "You have received a new order."
            ));
        }
        if ("POST_MODERATED".equals(eventType)
                && InAppNotificationTemplatePolicy.HIDE_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Post hidden",
                    "Your post was hidden due to a policy enforcement action."
            ));
        }
        if ("POST_MODERATED".equals(eventType)
                && InAppNotificationTemplatePolicy.REMOVE_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Post removed",
                    "Your post was removed due to a policy enforcement action."
            ));
        }
        if ("COMMENT_MODERATED".equals(eventType)
                && InAppNotificationTemplatePolicy.HIDE_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Comment hidden",
                    "Your comment was hidden due to a policy enforcement action."
            ));
        }
        if ("COMMENT_MODERATED".equals(eventType)
                && InAppNotificationTemplatePolicy.REMOVE_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Comment removed",
                    "Your comment was removed due to a policy enforcement action."
            ));
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
            case "REVIEW_REMINDER" -> new PushNotificationTemplate(
                    "Review your purchase",
                    "Tell us what you think about your order."
            );
            case "USER_SUSPENDED" -> new PushNotificationTemplate("Account suspended", "Your account has been suspended.");
            case "USER_BANNED" -> new PushNotificationTemplate("Account banned", "Your account has been banned.");
            case "USER_RESTRICTED" -> new PushNotificationTemplate("Account restricted", "Your account access has been restricted.");
            case "POST_MODERATED" -> new PushNotificationTemplate(
                    "Post moderated",
                    "Your post was moderated due to a policy enforcement action."
            );
            case "COMMENT_MODERATED" -> new PushNotificationTemplate(
                    "Comment moderated",
                    "Your comment was moderated due to a policy enforcement action."
            );
            case "COMMENT_RESTORED" -> new PushNotificationTemplate(
                    "Comment restored",
                    "Your comment was restored after a policy review."
            );
            case "PRODUCT_REMOVED" -> new PushNotificationTemplate("Product removed", "One of your products was removed.");
            case "REVIEW_HIDDEN" -> new PushNotificationTemplate("Review hidden", "One of your reviews was hidden.");
            case "SHOP_SUSPENDED" -> new PushNotificationTemplate("Shop suspended", "Your shop has been suspended.");
            case "SHOP_CLOSED" -> new PushNotificationTemplate("Shop closed", "Your shop has been closed.");
            case "USER_ENFORCEMENT_REVOKED" -> new PushNotificationTemplate(
                    "Account restriction lifted",
                    "An account restriction was revoked."
            );
            case "USER_ENFORCEMENT_EXPIRED" -> new PushNotificationTemplate(
                    "Account restriction ended",
                    "A temporary account restriction has ended."
            );
            case "SYSTEM_ANNOUNCEMENT_SENT" -> new PushNotificationTemplate("System announcement", "You have a new announcement.");
            default -> null;
        });
    }
}
