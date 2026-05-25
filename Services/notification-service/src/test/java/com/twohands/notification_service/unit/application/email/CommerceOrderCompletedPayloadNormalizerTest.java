package com.twohands.notification_service.unit.application.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.email.CommerceOrderCompletedPayloadNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommerceOrderCompletedPayloadNormalizerTest {

    private CommerceOrderCompletedPayloadNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new CommerceOrderCompletedPayloadNormalizer(new ObjectMapper());
    }

    @Test
    void normalizeForStorage_mapsOrderCodeAndReviewMetadata() {
        String raw = """
                {
                  "buyer_id":"buyer-uuid",
                  "order_id":"order-1",
                  "completed_at":"  2026-05-25T12:00:00Z  ",
                  "show_review_prompt":true,
                  "reviewable_item_ids":["item-1","item-2"],
                  "internal_note":"secret"
                }
                """;

        String normalized = normalizer.normalizeForStorage("ORDER_COMPLETED", raw);

        assertTrue(normalized.contains("\"order_code\":\"order-1\""));
        assertTrue(normalized.contains("\"completed_at\":\"2026-05-25T12:00:00Z\""));
        assertTrue(normalized.contains("\"has_reviewable_items\":true"));
        assertTrue(normalized.contains("\"reviewable_item_count\":2"));
        assertFalse(normalized.contains("reviewable_item_ids"));
        assertFalse(normalized.contains("internal_note"));
    }
}
