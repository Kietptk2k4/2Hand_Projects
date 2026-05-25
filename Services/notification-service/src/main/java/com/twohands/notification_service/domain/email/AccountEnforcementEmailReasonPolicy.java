package com.twohands.notification_service.domain.email;

import java.util.Locale;
import java.util.regex.Pattern;

public final class AccountEnforcementEmailReasonPolicy {

    private static final int MAX_REASON_LENGTH = 500;
    private static final Pattern BLOCKED_TERM = Pattern.compile(
            "(?i)\\b(internal|confidential|admin[_ -]?only)\\b"
    );

    private AccountEnforcementEmailReasonPolicy() {
    }

    public static String resolveUserFacingReason(String description, String reasonCode) {
        String sanitizedDescription = sanitize(description);
        if (sanitizedDescription != null && !BLOCKED_TERM.matcher(sanitizedDescription).find()) {
            return sanitizedDescription;
        }
        return formatReasonCode(reasonCode);
    }

    public static String formatExpiresAtLine(String expiresAt) {
        if (expiresAt == null || expiresAt.isBlank()) {
            return "";
        }
        return "\nThis action expires on: " + sanitize(expiresAt) + "\n";
    }

    public static String formatReasonLine(String reason) {
        if (reason == null || reason.isBlank()) {
            return "";
        }
        return "\nReason: " + reason + "\n";
    }

    private static String formatReasonCode(String reasonCode) {
        if (reasonCode == null || reasonCode.isBlank()) {
            return "";
        }
        String normalized = reasonCode.trim().toLowerCase(Locale.ROOT).replace('_', ' ');
        if (normalized.isBlank()) {
            return "";
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
