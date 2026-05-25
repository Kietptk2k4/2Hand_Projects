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
}
