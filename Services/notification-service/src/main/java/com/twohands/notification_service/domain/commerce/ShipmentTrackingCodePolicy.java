package com.twohands.notification_service.domain.commerce;

public final class ShipmentTrackingCodePolicy {

    private static final int MAX_TRACKING_CODE_LENGTH = 100;

    private ShipmentTrackingCodePolicy() {
    }

    public static String sanitize(String trackingCode) {
        if (trackingCode == null || trackingCode.isBlank()) {
            return null;
        }
        String trimmed = trackingCode.trim().replace("<", "").replace(">", "");
        if (trimmed.length() > MAX_TRACKING_CODE_LENGTH) {
            return trimmed.substring(0, MAX_TRACKING_CODE_LENGTH);
        }
        return trimmed;
    }
}
