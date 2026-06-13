package com.twohands.notification_service.domain.inapp;

import java.util.Optional;

public final class InAppNotificationTemplatePolicy {

    private InAppNotificationTemplatePolicy() {
    }

    public static final String SELLER_TEMPLATE_VARIANT = "seller";
    public static final String HIDE_TEMPLATE_VARIANT = "hide";
    public static final String REMOVE_TEMPLATE_VARIANT = "remove";

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
        if ("ORDER_COMPLETED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Order completed",
                    "The buyer confirmed receipt for an order."
            ));
        }
        if ("PAYOUT_REQUEST_APPROVED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Payout approved",
                    "Your withdrawal request has been approved."
            ));
        }
        if ("REVIEW_HIDDEN".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Review hidden on your product",
                    "A review on one of your products was hidden."
            ));
        }
        if ("REVIEW_REMOVED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Review removed on your product",
                    "A review on one of your products was removed."
            ));
        }
        if ("REVIEW_RESTORED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Review restored on your product",
                    "A review on one of your products was restored after a policy review."
            ));
        }
        if ("POST_MODERATED".equals(eventType) && HIDE_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Post hidden",
                    "Your post was hidden due to a policy enforcement action."
            ));
        }
        if ("POST_MODERATED".equals(eventType) && REMOVE_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Post removed",
                    "Your post was removed due to a policy enforcement action."
            ));
        }
        if ("COMMENT_MODERATED".equals(eventType) && HIDE_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Comment hidden",
                    "Your comment was hidden due to a policy enforcement action."
            ));
        }
        if ("COMMENT_MODERATED".equals(eventType) && REMOVE_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Comment removed",
                    "Your comment was removed due to a policy enforcement action."
            ));
        }
        return Optional.ofNullable(switch (eventType) {
            case "POST_CREATED" -> new InAppNotificationTemplate(
                    "New post",
                    "Someone you follow shared a new post."
            );
            case "POST_LIKED" -> new InAppNotificationTemplate("New like", "Someone liked your post.");
            case "USER_FOLLOWED" -> new InAppNotificationTemplate("New follower", "Someone started following you.");
            case "USER_AVATAR_UPDATED" -> new InAppNotificationTemplate(
                    "Avatar updated",
                    "Someone you follow updated their profile photo."
            );
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
            case "SHIPMENT_READY_TO_SHIP" -> new InAppNotificationTemplate(
                    "Shipment ready",
                    "Your package is ready to ship."
            );
            case "SHIPMENT_SHIPPED" -> new InAppNotificationTemplate(
                    "Order shipped",
                    "Your order is on the way."
            );
            case "SHIPMENT_DELIVERED" -> new InAppNotificationTemplate(
                    "Order delivered",
                    "Your order has been delivered."
            );
            case "ORDER_COMPLETED" -> new InAppNotificationTemplate(
                    "Order completed",
                    "Your order is complete."
            );
            case "REVIEW_REMINDER" -> new InAppNotificationTemplate(
                    "Review your purchase",
                    "Share your experience with a product from your recent order."
            );
            case "REVIEW_REPLIED" -> new InAppNotificationTemplate(
                    "Seller replied to your review",
                    "The shop responded to your product review."
            );
            case "USER_SUSPENDED" -> new InAppNotificationTemplate(
                    "Account suspended",
                    "Your account has been suspended."
            );
            case "USER_BANNED" -> new InAppNotificationTemplate(
                    "Account banned",
                    "Your account has been banned."
            );
            case "USER_RESTRICTED" -> new InAppNotificationTemplate(
                    "Account restricted",
                    "Some features on your account have been restricted."
            );
            case "POST_MODERATED" -> new InAppNotificationTemplate(
                    "Post moderated",
                    "Your post was moderated due to a policy enforcement action."
            );
            case "COMMENT_MODERATED" -> new InAppNotificationTemplate(
                    "Comment moderated",
                    "Your comment was moderated due to a policy enforcement action."
            );
            case "COMMENT_RESTORED" -> new InAppNotificationTemplate(
                    "Comment restored",
                    "Your comment was restored after a policy review."
            );
            case "PRODUCT_REMOVED" -> new InAppNotificationTemplate(
                    "Product removed",
                    "One of your products was removed."
            );
            case "PRODUCT_RESTORED" -> new InAppNotificationTemplate(
                    "Product restored",
                    "One of your products was restored after a policy review."
            );
            case "REVIEW_HIDDEN" -> new InAppNotificationTemplate(
                    "Review hidden",
                    "One of your reviews was hidden."
            );
            case "REVIEW_REMOVED" -> new InAppNotificationTemplate(
                    "Review removed",
                    "One of your reviews was removed."
            );
            case "REVIEW_RESTORED" -> new InAppNotificationTemplate(
                    "Review restored",
                    "One of your reviews was restored after a policy review."
            );
            case "SHOP_SUSPENDED" -> new InAppNotificationTemplate(
                    "Shop suspended",
                    "Your shop has been suspended."
            );
            case "SHOP_CLOSED" -> new InAppNotificationTemplate(
                    "Shop closed",
                    "Your shop has been closed."
            );
            case "SHOP_RESTORED" -> new InAppNotificationTemplate(
                    "Shop reopened",
                    "Your shop has been reopened after a policy review."
            );
            case "USER_ENFORCEMENT_REVOKED" -> new InAppNotificationTemplate(
                    "Account restriction lifted",
                    "An account restriction on your profile was revoked."
            );
            case "USER_ENFORCEMENT_EXPIRED" -> new InAppNotificationTemplate(
                    "Account restriction ended",
                    "A temporary account restriction has ended."
            );
            default -> null;
        });
    }
}
