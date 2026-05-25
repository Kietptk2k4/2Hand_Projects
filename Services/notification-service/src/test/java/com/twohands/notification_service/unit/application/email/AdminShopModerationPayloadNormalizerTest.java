package com.twohands.notification_service.unit.application.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.email.AdminShopModerationPayloadNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminShopModerationPayloadNormalizerTest {

    private AdminShopModerationPayloadNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new AdminShopModerationPayloadNormalizer(new ObjectMapper());
    }

    @Test
    void normalizeForStorage_mapsShopOwnerAndSuspensionReason() {
        String result = normalizer.normalizeForStorage(
                "SHOP_SUSPENDED",
                """
                        {
                          "shop_id": "shop-1",
                          "owner_id": "11111111-1111-1111-1111-111111111111",
                          "email": "owner@example.com",
                          "reason": "Policy violation",
                          "expires_at": "2026-12-31T00:00:00Z",
                          "suspended_by": "admin-uuid",
                          "note": "internal admin note"
                        }
                        """
        );

        assertTrue(result.contains("\"shop_owner_id\":\"11111111-1111-1111-1111-111111111111\""));
        assertTrue(result.contains("\"recipient_email\":\"owner@example.com\""));
        assertTrue(result.contains("\"suspension_reason\":\"Policy violation\""));
        assertTrue(result.contains("\"suspension_expires_at\":\"2026-12-31T00:00:00Z\""));
        assertFalse(result.contains("suspended_by"));
        assertFalse(result.contains("internal admin note"));
        assertFalse(result.contains("\"reason\""));
    }
}
