package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.domain.delivery.NotificationDefaultChannelPolicy;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class NotificationDeliveryChannelPolicy {

    private static final Set<String> DEDICATED_SOCIAL_NOTIFICATION_EVENTS = Set.of(
            "POST_LIKED",
            "USER_FOLLOWED",
            "COMMENT_CREATED",
            "COMMENT_REPLIED",
            "COMMENT_LIKED"
    );

    private static final Set<String> DEDICATED_ACCOUNT_ENFORCEMENT_NOTIFICATION_EVENTS = Set.of(
            "USER_SUSPENDED",
            "USER_RESTRICTED"
    );

    private static final Set<String> DEDICATED_ADMIN_MODERATION_NOTIFICATION_EVENTS = Set.of(
            "PRODUCT_REMOVED",
            "REVIEW_HIDDEN",
            "SHOP_SUSPENDED",
            "SHOP_CLOSED"
    );

    private static final Set<String> DEDICATED_ACCOUNT_ENFORCEMENT_LIFTED_EVENTS = Set.of(
            "USER_ENFORCEMENT_REVOKED",
            "USER_ENFORCEMENT_EXPIRED"
    );

    private static final Set<String> DEDICATED_SYSTEM_ANNOUNCEMENT_EVENTS = Set.of(
            "SYSTEM_ANNOUNCEMENT_SENT",
            "SYSTEM_ANNOUNCEMENT_CANCELLED"
    );

    private static final Set<String> DEDICATED_COMMERCE_NOTIFICATION_EVENTS = Set.of(
            "ORDER_CREATED",
            "COMMERCE_ORDER_CREATED",
            "PAYMENT_SUCCESS",
            "COMMERCE_PAYMENT_PAID",
            "PAYMENT_FAILED",
            "COMMERCE_PAYMENT_FAILED",
            "SHIPMENT_CREATED",
            "COMMERCE_SHIPMENT_CREATED",
            "SHIPMENT_SHIPPED",
            "COMMERCE_SHIPMENT_SHIPPED",
            "SHIPMENT_DELIVERED",
            "COMMERCE_SHIPMENT_DELIVERED",
            "ORDER_COMPLETED",
            "COMMERCE_ORDER_COMPLETED",
            "REVIEW_REMINDER",
            "COMMERCE_REVIEW_REMINDER"
    );

    private static final Set<String> SOCIAL_IN_APP_EVENTS = Set.of();

    public boolean allowsInApp(String eventType) {
        return NotificationDefaultChannelPolicy.resolve(eventType)
                .map(flags -> flags.inApp())
                .orElse(false);
    }

    public boolean isSocialInAppEvent(String eventType) {
        return SOCIAL_IN_APP_EVENTS.contains(eventType);
    }

    public boolean isDedicatedSocialNotificationEvent(String eventType) {
        return DEDICATED_SOCIAL_NOTIFICATION_EVENTS.contains(eventType);
    }

    public boolean isDedicatedCommerceNotificationEvent(String eventType) {
        return DEDICATED_COMMERCE_NOTIFICATION_EVENTS.contains(eventType);
    }

    public boolean isDedicatedAccountEnforcementNotificationEvent(String eventType) {
        return DEDICATED_ACCOUNT_ENFORCEMENT_NOTIFICATION_EVENTS.contains(eventType);
    }

    public boolean isDedicatedAdminModerationNotificationEvent(String eventType) {
        return DEDICATED_ADMIN_MODERATION_NOTIFICATION_EVENTS.contains(eventType);
    }

    public boolean isDedicatedSystemAnnouncementEvent(String eventType) {
        return DEDICATED_SYSTEM_ANNOUNCEMENT_EVENTS.contains(eventType);
    }

    public boolean isDedicatedAccountEnforcementLiftedEvent(String eventType) {
        return DEDICATED_ACCOUNT_ENFORCEMENT_LIFTED_EVENTS.contains(eventType);
    }

    public boolean isKnownEventType(String eventType) {
        return NotificationDefaultChannelPolicy.isKnownEventType(eventType);
    }
}
