package com.twohands.notification_service.application.consume;

import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Component
public class DomainEventTopicResolver {

    private static final Map<String, String> TOPIC_TO_EVENT_TYPE = Map.ofEntries(
            Map.entry("auth.user.created", "USER_CREATED"),
            Map.entry("auth.user.updated", "USER_UPDATED"),
            Map.entry("auth.user.deleted", "USER_DELETED"),
            Map.entry("auth.email.verification_requested", "EMAIL_VERIFICATION_REQUESTED"),
            Map.entry("auth.password.reset_requested", "PASSWORD_RESET_REQUESTED"),
            Map.entry("social.post.created", "POST_CREATED"),
            Map.entry("social.post.liked", "POST_LIKED"),
            Map.entry("social.comment.created", "COMMENT_CREATED"),
            Map.entry("social.comment.replied", "COMMENT_REPLIED"),
            Map.entry("social.comment.liked", "COMMENT_LIKED"),
            Map.entry("social.user.followed", "USER_FOLLOWED"),
            Map.entry("social.user.avatar_updated", "USER_AVATAR_UPDATED"),
            Map.entry("commerce.order.created", "COMMERCE_ORDER_CREATED"),
            Map.entry("commerce.payment.paid", "COMMERCE_PAYMENT_PAID"),
            Map.entry("commerce.payment.failed", "COMMERCE_PAYMENT_FAILED"),
            Map.entry("commerce.payment.refunded", "COMMERCE_PAYMENT_REFUNDED"),
            Map.entry("commerce.order.cancelled", "COMMERCE_ORDER_CANCELLED"),
            Map.entry("commerce.shipment.created", "COMMERCE_SHIPMENT_CREATED"),
            Map.entry("commerce.shipment.ready_to_ship", "COMMERCE_SHIPMENT_READY_TO_SHIP"),
            Map.entry("commerce.shipment.cancelled", "COMMERCE_SHIPMENT_CANCELLED"),
            Map.entry("commerce.order.cancel_pending_refund", "COMMERCE_ORDER_CANCEL_PENDING_REFUND"),
            Map.entry("commerce.shipment.shipped", "COMMERCE_SHIPMENT_SHIPPED"),
            Map.entry("commerce.shipment.delivered", "COMMERCE_SHIPMENT_DELIVERED"),
            Map.entry("commerce.order.completed", "COMMERCE_ORDER_COMPLETED"),
            Map.entry("commerce.payout.request_approved", "COMMERCE_PAYOUT_REQUEST_APPROVED"),
            Map.entry("commerce.review.reminder", "COMMERCE_REVIEW_REMINDER"),
            Map.entry("commerce.review.replied", "COMMERCE_REVIEW_REPLIED"),
            Map.entry("admin.user.suspended", "USER_SUSPENDED"),
            Map.entry("admin.user.banned", "USER_BANNED"),
            Map.entry("admin.user.restricted", "USER_RESTRICTED"),
            Map.entry("admin.post.moderated", "POST_MODERATED"),
            Map.entry("admin.comment.moderated", "COMMENT_MODERATED"),
            Map.entry("admin.comment.restored", "COMMENT_RESTORED"),
            Map.entry("admin.product.removed", "PRODUCT_REMOVED"),
            Map.entry("admin.product.restored", "PRODUCT_RESTORED"),
            Map.entry("admin.review.hidden", "REVIEW_HIDDEN"),
            Map.entry("admin.review.removed", "REVIEW_REMOVED"),
            Map.entry("admin.review.restored", "REVIEW_RESTORED"),
            Map.entry("admin.shop.suspended", "SHOP_SUSPENDED"),
            Map.entry("admin.shop.closed", "SHOP_CLOSED"),
            Map.entry("admin.shop.restored", "SHOP_RESTORED"),
            Map.entry("admin.announcement.published", "SYSTEM_ANNOUNCEMENT_PUBLISHED"),
            Map.entry("admin.announcement.cancelled", "SYSTEM_ANNOUNCEMENT_CANCELLED"),
            Map.entry("admin.user.enforcement_revoked", "USER_ENFORCEMENT_REVOKED"),
            Map.entry("admin.user.enforcement_expired", "USER_ENFORCEMENT_EXPIRED")
    );

    public NotificationSourceService resolveSourceService(String topic, String explicitSourceService) {
        if (explicitSourceService != null && !explicitSourceService.isBlank()) {
            NotificationSourceService resolved = resolveExplicitSourceService(explicitSourceService);
            if (resolved != null) {
                return resolved;
            }
            try {
                return NotificationSourceService.valueOf(explicitSourceService.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new InvalidDomainEventException("Unsupported source_service: " + explicitSourceService);
            }
        }
        if (topic == null || topic.isBlank()) {
            throw new InvalidDomainEventException("source_service is required when topic is unavailable");
        }
        String prefix = topic.split("\\.", 2)[0].toUpperCase(Locale.ROOT);
        return switch (prefix) {
            case "AUTH" -> NotificationSourceService.AUTH;
            case "SOCIAL" -> NotificationSourceService.SOCIAL;
            case "COMMERCE" -> NotificationSourceService.COMMERCE;
            case "ADMIN" -> NotificationSourceService.ADMIN;
            case "SYSTEM" -> NotificationSourceService.SYSTEM;
            default -> throw new InvalidDomainEventException("Unsupported topic prefix for source_service: " + prefix);
        };
    }

    public String resolveFallbackEventType(String topic) {
        if (topic == null) {
            return null;
        }
        return TOPIC_TO_EVENT_TYPE.get(topic);
    }

    /**
     * Maps broker envelope {@code source} / {@code source_service} strings to enum values.
     * Auth/commerce envelopes use short or {@code *-service} forms instead of enum names.
     */
    private NotificationSourceService resolveExplicitSourceService(String explicitSourceService) {
        String normalized = explicitSourceService.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "auth", "auth-service" -> NotificationSourceService.AUTH;
            case "social", "social-service" -> NotificationSourceService.SOCIAL;
            case "commerce", "commerce-service" -> NotificationSourceService.COMMERCE;
            case "admin", "admin-service" -> NotificationSourceService.ADMIN;
            case "system", "system-service" -> NotificationSourceService.SYSTEM;
            default -> null;
        };
    }
}
