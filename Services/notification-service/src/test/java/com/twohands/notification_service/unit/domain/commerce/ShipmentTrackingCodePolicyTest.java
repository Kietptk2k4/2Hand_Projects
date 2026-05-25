package com.twohands.notification_service.unit.domain.commerce;

import com.twohands.notification_service.domain.commerce.ShipmentTrackingCodePolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ShipmentTrackingCodePolicyTest {

    @Test
    void sanitize_returnsNullWhenBlank() {
        assertNull(ShipmentTrackingCodePolicy.sanitize("  "));
    }

    @Test
    void sanitize_trimsAndStripsUnsafeCharacters() {
        assertEquals("VN123456", ShipmentTrackingCodePolicy.sanitize("  VN123456  "));
    }
}
