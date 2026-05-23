package com.twohands.notification_service.application.idempotency;

import com.twohands.notification_service.domain.idempotency.NotificationErrorSanitizer;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class BoundedNotificationErrorSanitizer implements NotificationErrorSanitizer {

    private static final int MAX_LENGTH = 500;
    private static final Pattern SENSITIVE_VALUE = Pattern.compile(
            "(?i)(password|token|otp|secret|bearer|authorization|api[_-]?key|credential)\\s*[:=]\\s*\\S+"
    );
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    @Override
    public String sanitize(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return null;
        }
        String cleaned = SENSITIVE_VALUE.matcher(errorMessage).replaceAll("$1=***REDACTED***");
        cleaned = WHITESPACE.matcher(cleaned).replaceAll(" ").trim();
        if (cleaned.length() > MAX_LENGTH) {
            return cleaned.substring(0, MAX_LENGTH);
        }
        return cleaned;
    }
}
