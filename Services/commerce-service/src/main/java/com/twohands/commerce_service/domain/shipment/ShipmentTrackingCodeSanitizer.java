package com.twohands.commerce_service.domain.shipment;

public final class ShipmentTrackingCodeSanitizer {

    private static final int MAX_TRACKING_CODE_LENGTH = 100;

    private ShipmentTrackingCodeSanitizer() {
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
