package com.twohands.notification_service.domain.commerce;

import java.util.Locale;
import java.util.regex.Pattern;

public final class PaymentFailedReasonPolicy {

    private static final int MAX_REASON_LENGTH = 500;
    private static final Pattern BLOCKED_TERM = Pattern.compile(
            "(?i)\\b(internal|confidential|stacktrace|exception|webhook|provider[_ -]?secret|"
                    + "stripe|paypal|raw[_ -]?error|error[_ -]?code|sql|timeout at)\\b"
    );

    private PaymentFailedReasonPolicy() {
    }

    public static String resolveUserFacingReason(String failureReason, String reasonCode) {
        String sanitized = sanitize(failureReason);
        if (sanitized != null && !BLOCKED_TERM.matcher(sanitized).find()) {
            return sanitized;
        }
        return formatReasonCode(reasonCode);
    }

    private static String formatReasonCode(String reasonCode) {
        if (reasonCode == null || reasonCode.isBlank()) {
            return null;
        }
        String normalized = reasonCode.trim().toLowerCase(Locale.ROOT).replace('_', ' ');
        if (normalized.isBlank() || BLOCKED_TERM.matcher(normalized).find()) {
            return null;
        }
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim().replace("<", "").replace(">", "");
        if (trimmed.length() > MAX_REASON_LENGTH) {
            return trimmed.substring(0, MAX_REASON_LENGTH);
        }
        return trimmed;
    }
}
