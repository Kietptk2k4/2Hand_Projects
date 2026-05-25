package com.twohands.notification_service.domain.commerce;

public final class PaymentSuccessEmailSummaryPolicy {

    private PaymentSuccessEmailSummaryPolicy() {
    }

    public static String formatSummaryLine(String amount, String paymentMethod) {
        String amountLine = formatAmountSegment(amount);
        String methodLine = formatPaymentMethodSegment(paymentMethod);

        if (amountLine.isEmpty() && methodLine.isEmpty()) {
            return "";
        }
        if (amountLine.isEmpty()) {
            return "\n" + methodLine + "\n";
        }
        if (methodLine.isEmpty()) {
            return "\n" + amountLine + "\n";
        }
        return "\n" + amountLine + "\n" + methodLine + "\n";
    }

    private static String formatAmountSegment(String amount) {
        if (amount == null || amount.isBlank()) {
            return "";
        }
        return "Amount paid: " + sanitize(amount);
    }

    private static String formatPaymentMethodSegment(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return "";
        }
        return "Payment method: " + sanitize(paymentMethod);
    }

    private static String sanitize(String value) {
        String trimmed = value.trim();
        if (trimmed.length() > 50) {
            return trimmed.substring(0, 50);
        }
        return trimmed.replace("<", "").replace(">", "");
    }
}
