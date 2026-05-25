package com.twohands.notification_service.unit.application.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.email.CommerceShipmentNotificationPayloadNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommerceShipmentNotificationPayloadNormalizerTest {

    private CommerceShipmentNotificationPayloadNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new CommerceShipmentNotificationPayloadNormalizer(new ObjectMapper());
    }

    @Test
    void normalizeForStorage_sanitizesTrackingCodeAndStripsInternalFields() {
        String raw = """
                {
                  "shipment_id":"ship-1",
                  "buyer_id":"buyer-uuid",
                  "tracking_code":"  VN123  ",
                  "internal_note":"secret",
                  "carrier_raw_response":"{}"
                }
                """;

        String normalized = normalizer.normalizeForStorage("SHIPMENT_CREATED", raw);

        assertTrue(normalized.contains("\"tracking_code\":\"VN123\""));
        assertFalse(normalized.contains("internal_note"));
        assertFalse(normalized.contains("carrier_raw_response"));
    }

    @Test
    void normalizeForStorage_appliesToShipmentShippedEvents() {
        String raw = """
                {
                  "shipment_id":"ship-1",
                  "buyer_id":"buyer-uuid",
                  "tracking_code":"  TRACK99  ",
                  "carrier_raw_response":"{}"
                }
                """;

        String normalized = normalizer.normalizeForStorage("SHIPMENT_SHIPPED", raw);

        assertTrue(normalized.contains("\"tracking_code\":\"TRACK99\""));
        assertFalse(normalized.contains("carrier_raw_response"));
    }

    @Test
    void normalizeForStorage_sanitizesDeliveredMetadataForShipmentDelivered() {
        String raw = """
                {
                  "shipment_id":"ship-1",
                  "buyer_id":"buyer-uuid",
                  "delivered_at":"  2026-05-25T10:00:00Z  ",
                  "show_confirm_receipt":true,
                  "carrier_raw_response":"{}"
                }
                """;

        String normalized = normalizer.normalizeForStorage("SHIPMENT_DELIVERED", raw);

        assertTrue(normalized.contains("\"delivered_at\":\"2026-05-25T10:00:00Z\""));
        assertTrue(normalized.contains("\"prompt_confirm_receipt\":true"));
        assertFalse(normalized.contains("carrier_raw_response"));
    }

    @Test
    void normalizeForStorage_leavesOtherEventTypesUntouched() {
        String raw = "{\"tracking_code\":\"test\"}";

        String normalized = normalizer.normalizeForStorage("POST_LIKED", raw);

        assertTrue(normalized.contains("tracking_code"));
    }
}
