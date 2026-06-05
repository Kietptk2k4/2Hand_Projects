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
            Map.entry("social.post.liked", "POST_LIKED"),
            Map.entry("social.comment.created", "COMMENT_CREATED"),
            Map.entry("social.comment.replied", "COMMENT_REPLIED"),
            Map.entry("social.comment.liked", "COMMENT_LIKED"),
            Map.entry("social.user.followed", "USER_FOLLOWED"),
            Map.entry("commerce.order.created", "COMMERCE_ORDER_CREATED"),
            Map.entry("commerce.payment.paid", "COMMERCE_PAYMENT_PAID"),
            Map.entry("commerce.payment.failed", "COMMERCE_PAYMENT_FAILED"),
            Map.entry("commerce.shipment.created", "COMMERCE_SHIPMENT_CREATED"),
            Map.entry("commerce.shipment.shipped", "COMMERCE_SHIPMENT_SHIPPED"),
            Map.entry("commerce.shipment.delivered", "COMMERCE_SHIPMENT_DELIVERED"),
            Map.entry("commerce.order.completed", "COMMERCE_ORDER_COMPLETED"),
            Map.entry("commerce.review.reminder", "COMMERCE_REVIEW_REMINDER"),
            Map.entry("admin.user.suspended", "USER_SUSPENDED"),
            Map.entry("admin.user.banned", "USER_BANNED"),
            Map.entry("admin.user.restricted", "USER_RESTRICTED"),
            Map.entry("admin.product.removed", "PRODUCT_REMOVED"),
            Map.entry("admin.review.hidden", "REVIEW_HIDDEN"),
            Map.entry("admin.shop.suspended", "SHOP_SUSPENDED"),
            Map.entry("admin.shop.closed", "SHOP_CLOSED"),
            Map.entry("admin.announcement.published", "SYSTEM_ANNOUNCEMENT_PUBLISHED"),
            Map.entry("admin.announcement.cancelled", "SYSTEM_ANNOUNCEMENT_CANCELLED"),
            Map.entry("admin.user.enforcement_revoked", "USER_ENFORCEMENT_REVOKED"),
            Map.entry("admin.user.enforcement_expired", "USER_ENFORCEMENT_EXPIRED")
    );

    public NotificationSourceService resolveSourceService(String topic, String explicitSourceService) {
        if (explicitSourceService != null && !explicitSourceService.isBlank()) {
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
}
