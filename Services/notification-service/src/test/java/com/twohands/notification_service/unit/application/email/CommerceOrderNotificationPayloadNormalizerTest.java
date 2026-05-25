package com.twohands.notification_service.unit.application.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.email.CommerceOrderNotificationPayloadNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommerceOrderNotificationPayloadNormalizerTest {

    private CommerceOrderNotificationPayloadNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new CommerceOrderNotificationPayloadNormalizer(new ObjectMapper());
    }

    @Test
    void normalizeForStorage_mapsEmailAndOrderCodeForOrderCreated() {
        String raw = """
                {
                  "email":"buyer@example.com",
                  "order_id":"order-uuid",
                  "payment_method":"STRIPE_SECRET",
                  "final_amount":"100000"
                }
                """;

        String normalized = normalizer.normalizeForStorage("ORDER_CREATED", raw);

        assertTrue(normalized.contains("\"recipient_email\":\"buyer@example.com\""));
        assertTrue(normalized.contains("\"order_code\":\"order-uuid\""));
        assertFalse(normalized.contains("payment_method"));
    }

    @Test
    void normalizeForStorage_stripsPaymentSecretsForPaymentSuccess() {
        String raw = """
                {
                  "email":"buyer@example.com",
                  "order_id":"order-uuid",
                  "payment_method":"COD",
                  "provider_secret":"secret",
                  "raw_webhook":"{}"
                }
                """;

        String normalized = normalizer.normalizeForStorage("PAYMENT_SUCCESS", raw);

        assertTrue(normalized.contains("\"recipient_email\":\"buyer@example.com\""));
        assertTrue(normalized.contains("\"payment_method\":\"COD\""));
        assertFalse(normalized.contains("provider_secret"));
        assertFalse(normalized.contains("raw_webhook"));
    }

    @Test
    void normalizeForStorage_leavesOtherEventTypesUntouched() {
        String raw = "{\"email\":\"buyer@example.com\"}";

        String normalized = normalizer.normalizeForStorage("POST_LIKED", raw);

        assertTrue(normalized.contains("buyer@example.com"));
        assertFalse(normalized.contains("recipient_email"));
    }
}
