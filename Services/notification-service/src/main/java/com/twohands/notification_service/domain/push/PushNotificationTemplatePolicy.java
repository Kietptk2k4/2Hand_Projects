package com.twohands.notification_service.domain.push;

import com.twohands.notification_service.domain.inapp.InAppNotificationTemplatePolicy;
import com.twohands.notification_service.domain.social.SocialNotificationTemplatePolicy;

import java.util.Optional;

public final class PushNotificationTemplatePolicy {

    private PushNotificationTemplatePolicy() {
    }

    public static final String SELLER_TEMPLATE_VARIANT = "seller";

    public static Optional<PushNotificationTemplate> resolve(String eventType) {
        return resolve(eventType, null);
    }

    public static Optional<PushNotificationTemplate> resolve(
            String eventType,
            String templateVariant,
            String actorDisplayName
    ) {
        if (eventType == null || eventType.isBlank()) {
            return Optional.empty();
        }
        if (templateVariant == null) {
            if ("POST_CREATED".equals(eventType)) {
                return Optional.of(SocialNotificationTemplatePolicy.postCreatedPush(actorDisplayName));
            }
            if ("USER_AVATAR_UPDATED".equals(eventType)) {
                return Optional.of(SocialNotificationTemplatePolicy.avatarUpdatedPush(actorDisplayName));
            }
        }
        return resolve(eventType, templateVariant);
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
        if ("ORDER_COMPLETED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Order completed",
                    "The buyer confirmed receipt for an order."
            ));
        }
        if ("PAYOUT_REQUEST_APPROVED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Payout approved",
                    "Your withdrawal request has been approved."
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
        if ("REVIEW_REMOVED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Review removed on your product",
                    "A review on one of your products was removed."
            ));
        }
        if ("REVIEW_RESTORED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Review restored on your product",
                    "A review on one of your products was restored after a policy review."
            ));
        }
        return Optional.ofNullable(switch (eventType) {
            case "PASSWORD_CHANGED" -> new PushNotificationTemplate("Password changed", "Your account password was changed.");
            case "POST_CREATED" -> new PushNotificationTemplate(
                    "New post",
                    "Someone you follow shared a new post."
            );
            case "POST_LIKED" -> new PushNotificationTemplate("New like", "Someone liked your post.");
            case "USER_FOLLOWED" -> new PushNotificationTemplate("New follower", "Someone started following you.");
            case "USER_AVATAR_UPDATED" -> new PushNotificationTemplate(
                    "Avatar updated",
                    "Someone you follow updated their profile photo."
            );
            case "COMMENT_CREATED" -> new PushNotificationTemplate("New comment", "Someone commented on your post.");
            case "COMMENT_REPLIED" -> new PushNotificationTemplate("New reply", "Someone replied to your comment.");
            case "COMMENT_LIKED" -> new PushNotificationTemplate("Comment liked", "Someone liked your comment.");
            case "ORDER_CREATED" -> new PushNotificationTemplate("Order confirmed", "Your order has been created.");
            case "PAYMENT_SUCCESS" -> new PushNotificationTemplate("Payment received", "Your payment was successful.");
            case "PAYMENT_FAILED" -> new PushNotificationTemplate("Payment failed", "Your payment could not be completed.");
            case "PAYMENT_REFUNDED" -> new PushNotificationTemplate(
                    "Refund processed",
                    "Your refund has been processed successfully."
            );
            case "ORDER_CANCELLED" -> new PushNotificationTemplate(
                    "Order cancelled",
                    "Your order has been cancelled."
            );
            case "SHIPMENT_READY_TO_SHIP" -> new PushNotificationTemplate(
                    "Shipment ready",
                    "Your package is ready to ship."
            );
            case "SHIPMENT_CANCELLED" -> new PushNotificationTemplate(
                    "Shipment cancelled",
                    "The seller cancelled your shipment."
            );
            case "ORDER_CANCEL_PENDING_REFUND" -> new PushNotificationTemplate(
                    "Refund pending",
                    "Your cancellation request was received. We are processing your refund."
            );
            case "SHIPMENT_SHIPPED" -> new PushNotificationTemplate("Order shipped", "Your order is on the way.");
            case "SHIPMENT_DELIVERED" -> new PushNotificationTemplate("Order delivered", "Your order has been delivered.");
            case "ORDER_COMPLETED" -> new PushNotificationTemplate("Order completed", "Your order is complete.");
            case "REVIEW_REMINDER" -> new PushNotificationTemplate(
                    "Review your purchase",
                    "Tell us what you think about your order."
            );
            case "REVIEW_REPLIED" -> new PushNotificationTemplate(
                    "Seller replied to your review",
                    "The shop responded to your product review."
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
            case "PRODUCT_RESTORED" -> new PushNotificationTemplate(
                    "Product restored",
                    "One of your products was restored after a policy review."
            );
            case "REVIEW_HIDDEN" -> new PushNotificationTemplate("Review hidden", "One of your reviews was hidden.");
            case "REVIEW_REMOVED" -> new PushNotificationTemplate("Review removed", "One of your reviews was removed.");
            case "REVIEW_RESTORED" -> new PushNotificationTemplate(
                    "Review restored",
                    "One of your reviews was restored after a policy review."
            );
            case "SHOP_SUSPENDED" -> new PushNotificationTemplate("Shop suspended", "Your shop has been suspended.");
            case "SHOP_CLOSED" -> new PushNotificationTemplate("Shop closed", "Your shop has been closed.");
            case "SHOP_RESTORED" -> new PushNotificationTemplate(
                    "Shop reopened",
                    "Your shop has been reopened after a policy review."
            );
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
