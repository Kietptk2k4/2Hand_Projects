package com.twohands.notification_service.domain.commerce;

public final class OrderConfirmationEmailSummaryPolicy {

    private OrderConfirmationEmailSummaryPolicy() {
    }

    public static String formatAmountLine(String totalAmount) {
        if (totalAmount == null || totalAmount.isBlank()) {
            return "";
        }
        return "\nOrder total: " + sanitizeAmount(totalAmount) + "\n";
    }

    private static String sanitizeAmount(String value) {
        String trimmed = value.trim();
        if (trimmed.length() > 50) {
            return trimmed.substring(0, 50);
        }
        return trimmed.replace("<", "").replace(">", "");
    }
}
