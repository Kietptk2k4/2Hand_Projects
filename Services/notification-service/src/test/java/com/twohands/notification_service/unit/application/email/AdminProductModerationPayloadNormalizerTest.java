package com.twohands.notification_service.unit.application.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.email.AdminProductModerationPayloadNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminProductModerationPayloadNormalizerTest {

    private AdminProductModerationPayloadNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new AdminProductModerationPayloadNormalizer(new ObjectMapper());
    }

    @Test
    void normalizeForStorage_mapsSellerAndUserSafeReason() {
        String result = normalizer.normalizeForStorage(
                "PRODUCT_REMOVED",
                """
                        {
                          "product_id": "prod-1",
                          "seller_id": "11111111-1111-1111-1111-111111111111",
                          "reason": "Counterfeit listing",
                          "removed_by": "admin-uuid",
                          "note": "internal admin note"
                        }
                        """
        );

        assertTrue(result.contains("\"seller_user_id\":\"11111111-1111-1111-1111-111111111111\""));
        assertTrue(result.contains("\"removal_reason\":\"Counterfeit listing\""));
        assertFalse(result.contains("removed_by"));
        assertFalse(result.contains("internal admin note"));
        assertFalse(result.contains("\"reason\""));
    }

    @Test
    void normalizeForStorage_redactsUnsafeReason() {
        String result = normalizer.normalizeForStorage(
                "PRODUCT_REMOVED",
                """
                        {
                          "product_id": "prod-1",
                          "seller_user_id": "11111111-1111-1111-1111-111111111111",
                          "reason": "internal investigation only",
                          "reason_code": "POLICY_VIOLATION"
                        }
                        """
        );

        assertTrue(result.contains("\"removal_reason\":\"Policy violation\""));
    }
}
