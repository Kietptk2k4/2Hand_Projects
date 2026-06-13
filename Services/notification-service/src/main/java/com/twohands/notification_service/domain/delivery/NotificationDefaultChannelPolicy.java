package com.twohands.notification_service.domain.delivery;

import java.util.Map;
import java.util.Optional;

public final class NotificationDefaultChannelPolicy {

    private static final Map<String, DefaultChannelFlags> DEFAULTS = Map.ofEntries(
            Map.entry("USER_CREATED", new DefaultChannelFlags(true, false, true)),
            Map.entry("EMAIL_VERIFICATION_REQUESTED", new DefaultChannelFlags(false, false, true)),
            Map.entry("PASSWORD_RESET_REQUESTED", new DefaultChannelFlags(false, false, true)),
            Map.entry("PASSWORD_CHANGED", new DefaultChannelFlags(true, true, true)),
            Map.entry("POST_CREATED", new DefaultChannelFlags(true, true, false)),
            Map.entry("POST_LIKED", new DefaultChannelFlags(true, true, false)),
            Map.entry("USER_FOLLOWED", new DefaultChannelFlags(true, true, false)),
            Map.entry("USER_AVATAR_UPDATED", new DefaultChannelFlags(true, true, false)),
            Map.entry("COMMENT_CREATED", new DefaultChannelFlags(true, true, false)),
            Map.entry("COMMENT_REPLIED", new DefaultChannelFlags(true, true, false)),
            Map.entry("COMMENT_LIKED", new DefaultChannelFlags(true, true, false)),
            Map.entry("ORDER_CREATED", new DefaultChannelFlags(true, true, true)),
            Map.entry("PAYMENT_SUCCESS", new DefaultChannelFlags(true, true, true)),
            Map.entry("PAYMENT_FAILED", new DefaultChannelFlags(true, true, false)),
            Map.entry("SHIPMENT_CREATED", new DefaultChannelFlags(true, false, false)),
            Map.entry("SHIPMENT_READY_TO_SHIP", new DefaultChannelFlags(true, true, false)),
            Map.entry("SHIPMENT_SHIPPED", new DefaultChannelFlags(true, true, false)),
            Map.entry("SHIPMENT_DELIVERED", new DefaultChannelFlags(true, true, false)),
            Map.entry("ORDER_COMPLETED", new DefaultChannelFlags(true, true, false)),
            Map.entry("PAYOUT_REQUEST_APPROVED", new DefaultChannelFlags(true, true, false)),
            Map.entry("REVIEW_REMINDER", new DefaultChannelFlags(true, true, false)),
            Map.entry("REVIEW_REPLIED", new DefaultChannelFlags(true, true, false)),
            Map.entry("USER_SUSPENDED", new DefaultChannelFlags(true, true, true)),
            Map.entry("USER_BANNED", new DefaultChannelFlags(true, true, true)),
            Map.entry("USER_RESTRICTED", new DefaultChannelFlags(true, true, true)),
            Map.entry("POST_MODERATED", new DefaultChannelFlags(true, true, false)),
            Map.entry("COMMENT_MODERATED", new DefaultChannelFlags(true, true, false)),
            Map.entry("COMMENT_RESTORED", new DefaultChannelFlags(true, true, false)),
            Map.entry("PRODUCT_REMOVED", new DefaultChannelFlags(true, true, false)),
            Map.entry("PRODUCT_RESTORED", new DefaultChannelFlags(true, true, false)),
            Map.entry("REVIEW_HIDDEN", new DefaultChannelFlags(true, false, false)),
            Map.entry("REVIEW_REMOVED", new DefaultChannelFlags(true, true, false)),
            Map.entry("REVIEW_RESTORED", new DefaultChannelFlags(true, true, false)),
            Map.entry("SHOP_SUSPENDED", new DefaultChannelFlags(true, true, true)),
            Map.entry("SHOP_CLOSED", new DefaultChannelFlags(true, true, true)),
            Map.entry("SHOP_RESTORED", new DefaultChannelFlags(true, true, false)),
            Map.entry("SYSTEM_ANNOUNCEMENT_SENT", new DefaultChannelFlags(true, true, false)),
            Map.entry("SYSTEM_ANNOUNCEMENT_CANCELLED", new DefaultChannelFlags(false, false, false)),
            Map.entry("USER_ENFORCEMENT_REVOKED", new DefaultChannelFlags(true, true, false)),
            Map.entry("USER_ENFORCEMENT_EXPIRED", new DefaultChannelFlags(true, true, false))
    );

    private NotificationDefaultChannelPolicy() {
    }

    public static boolean isKnownEventType(String eventType) {
        return DEFAULTS.containsKey(eventType);
    }

    public static Optional<DefaultChannelFlags> resolve(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(DEFAULTS.get(eventType));
    }

    public static java.util.Set<String> supportedEventTypes() {
        return DEFAULTS.keySet();
    }
}
