package com.twohands.notification_service.domain.commerce;

public final class ReviewReminderReferencePolicy {

    private static final String ORDER_REFERENCE = "ORDER";
    private static final String PRODUCT_REFERENCE = "PRODUCT";

    private ReviewReminderReferencePolicy() {
    }

    public static String resolveReferenceType(String productId) {
        if (productId != null && !productId.isBlank()) {
            return PRODUCT_REFERENCE;
        }
        return ORDER_REFERENCE;
    }

    public static String resolveReferenceId(String productId, String orderId) {
        if (productId != null && !productId.isBlank()) {
            return productId.trim();
        }
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("order_id is required when product_id is absent.");
        }
        return orderId.trim();
    }
}
