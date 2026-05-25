package com.twohands.notification_service.unit.domain.commerce;

import com.twohands.notification_service.domain.commerce.ShipmentDeliveredTimestampPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ShipmentDeliveredTimestampPolicyTest {

    @Test
    void sanitize_returnsNullWhenBlank() {
        assertNull(ShipmentDeliveredTimestampPolicy.sanitize(null));
    }

    @Test
    void sanitize_trimsValue() {
        assertEquals(
                "2026-05-25T10:00:00Z",
                ShipmentDeliveredTimestampPolicy.sanitize("  2026-05-25T10:00:00Z  ")
        );
    }
}
