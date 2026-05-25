package com.twohands.notification_service.unit.application.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.email.CommerceReviewReminderPayloadNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommerceReviewReminderPayloadNormalizerTest {

    private CommerceReviewReminderPayloadNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new CommerceReviewReminderPayloadNormalizer(new ObjectMapper());
    }

    @Test
    void normalizeForStorage_mapsItemIdAndReviewFlags() {
        UUID buyerId = UUID.randomUUID();

        String normalized = normalizer.normalizeForStorage(
                "REVIEW_REMINDER",
                """
                        {
                          "buyer_id":"%s",
                          "item_id":"item-55",
                          "order_id":"order-9",
                          "reminder_day":14,
                          "review_exists":true,
                          "internal_note":"secret"
                        }
                        """.formatted(buyerId)
        );

        assertTrue(normalized.contains("\"order_item_id\":\"item-55\""));
        assertTrue(normalized.contains("\"already_reviewed\":true"));
        assertFalse(normalized.contains("review_exists"));
        assertFalse(normalized.contains("internal_note"));
    }
}
