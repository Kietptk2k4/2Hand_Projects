package com.twohands.notification_service.unit.application.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.email.CommercePaymentFailedPayloadNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommercePaymentFailedPayloadNormalizerTest {

    private CommercePaymentFailedPayloadNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new CommercePaymentFailedPayloadNormalizer(new ObjectMapper());
    }

    @Test
    void normalizeForStorage_mapsUserSafeFailureReasonAndStripsSecrets() {
        String raw = """
                {
                  "buyer_id":"buyer-uuid",
                  "payment_id":"pay-1",
                  "order_id":"order-1",
                  "failure_reason":"Insufficient balance",
                  "provider_secret":"secret",
                  "raw_webhook":"{}"
                }
                """;

        String normalized = normalizer.normalizeForStorage("PAYMENT_FAILED", raw);

        assertTrue(normalized.contains("\"user_failure_reason\":\"Insufficient balance\""));
        assertFalse(normalized.contains("\"failure_reason\""));
        assertFalse(normalized.contains("provider_secret"));
        assertFalse(normalized.contains("raw_webhook"));
        assertTrue(normalized.contains("\"order_code\":\"order-1\""));
    }

    @Test
    void normalizeForStorage_leavesOtherEventTypesUntouched() {
        String raw = "{\"failure_reason\":\"test\"}";

        String normalized = normalizer.normalizeForStorage("POST_LIKED", raw);

        assertTrue(normalized.contains("failure_reason"));
    }
}
