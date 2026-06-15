package com.twohands.notification_service.domain.commerce;

public final class OrderCancelNotificationContentPolicy {

    private static final String REASON_PREFIX = " Lý do: ";

    private OrderCancelNotificationContentPolicy() {
    }

    public static boolean supportsReasonInContent(String eventType) {
        return "ORDER_CANCELLED".equals(eventType)
                || "ORDER_CANCEL_PENDING_REFUND".equals(eventType)
                || "PAYOUT_REQUEST_REJECTED".equals(eventType);
    }

    public static String appendReason(String baseContent, String reason) {
        if (reason == null || reason.isBlank()) {
            return baseContent == null ? "" : baseContent;
        }
        String trimmed = reason.trim();
        String base = baseContent == null ? "" : baseContent;
        if (base.contains(trimmed)) {
            return base;
        }
        return base + REASON_PREFIX + trimmed;
    }
}
