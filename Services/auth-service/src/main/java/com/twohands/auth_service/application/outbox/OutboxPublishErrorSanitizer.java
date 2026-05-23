package com.twohands.auth_service.application.outbox;

import java.util.regex.Pattern;

public final class OutboxPublishErrorSanitizer {

    private static final Pattern SENSITIVE_VALUE = Pattern.compile(
            "(?i)(password|token|otp|secret|bearer)(\\s*[=:]\\s*)(\\S+)"
    );

    private OutboxPublishErrorSanitizer() {
    }

    public static String sanitize(String error) {
        if (error == null || error.isBlank()) {
            return "Unknown outbox publish error";
        }
        String redacted = SENSITIVE_VALUE.matcher(error).replaceAll("$1$2***");
        return redacted.length() > 500 ? redacted.substring(0, 500) : redacted;
    }
}
