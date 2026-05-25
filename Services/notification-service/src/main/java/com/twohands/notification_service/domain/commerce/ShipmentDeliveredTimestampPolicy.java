package com.twohands.notification_service.domain.commerce;

public final class ShipmentDeliveredTimestampPolicy {

    private static final int MAX_LENGTH = 50;

    private ShipmentDeliveredTimestampPolicy() {
    }

    public static String sanitize(String deliveredAt) {
        if (deliveredAt == null || deliveredAt.isBlank()) {
            return null;
        }
        String trimmed = deliveredAt.trim().replace("<", "").replace(">", "");
        if (trimmed.length() > MAX_LENGTH) {
            return trimmed.substring(0, MAX_LENGTH);
        }
        return trimmed;
    }
}
