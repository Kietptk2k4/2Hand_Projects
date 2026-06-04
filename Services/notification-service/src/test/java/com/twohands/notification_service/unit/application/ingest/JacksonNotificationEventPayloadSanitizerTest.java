package com.twohands.notification_service.unit.application.ingest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.ingest.JacksonNotificationEventPayloadSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JacksonNotificationEventPayloadSanitizerTest {

    private JacksonNotificationEventPayloadSanitizer sanitizer;

    @BeforeEach
    void setUp() {
        sanitizer = new JacksonNotificationEventPayloadSanitizer(new ObjectMapper());
    }

    @Test
    void sanitize_returnsEmptyObjectForBlankPayload() {
        assertEquals("{}", sanitizer.sanitize(null));
        assertEquals("{}", sanitizer.sanitize("   "));
    }

    @Test
    void sanitize_unwrapsJsonEncodedStringPayload() {
        String result = sanitizer.sanitize("\"{\\\"postId\\\":\\\"post-1\\\"}\"");

        assertTrue(result.contains("postId"));
        assertTrue(result.contains("post-1"));
    }

    @Test
    void sanitize_preservesVerificationCodeField() {
        String result = sanitizer.sanitize("""
                {"recipient_email":"user@example.com","verification_code":"123456","verification_token":"secret"}
                """);

        assertTrue(result.contains("\"verification_code\":\"123456\""));
        assertTrue(result.contains("***REDACTED***"));
        assertFalse(result.contains("secret"));
    }

    @Test
    void sanitize_redactsSensitiveFields() {
        String result = sanitizer.sanitize("""
                {"actorName":"Alice","access_token":"abc","nested":{"otp_code":"123456"}}
                """);

        assertTrue(result.contains("Alice"));
        assertTrue(result.contains("***REDACTED***"));
        assertFalse(result.contains("abc"));
        assertFalse(result.contains("123456"));
    }
}
