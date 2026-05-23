package com.twohands.notification_service.unit.application.idempotency;

import com.twohands.notification_service.application.idempotency.BoundedNotificationErrorSanitizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoundedNotificationErrorSanitizerTest {

    private final BoundedNotificationErrorSanitizer sanitizer = new BoundedNotificationErrorSanitizer();

    @Test
    void sanitize_redactsSensitiveValuesAndTruncates() {
        String input = "payment failed token=super-secret-value " + "x".repeat(600);

        String result = sanitizer.sanitize(input);

        assertTrue(result.contains("token=***REDACTED***"));
        assertFalse(result.contains("super-secret-value"));
        assertEquals(500, result.length());
    }

    @Test
    void sanitize_returnsNullForBlankInput() {
        assertNull(sanitizer.sanitize(null));
        assertNull(sanitizer.sanitize("   "));
    }
}
