package com.twohands.notification_service.unit.application.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.email.AccountEnforcementEmailPayloadNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountEnforcementEmailPayloadNormalizerTest {

    private AccountEnforcementEmailPayloadNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new AccountEnforcementEmailPayloadNormalizer(new ObjectMapper());
    }

    @Test
    void normalizeForStorage_mapsAdminSuspendedPayloadToUserSafeFields() {
        String result = normalizer.normalizeForStorage(
                "USER_SUSPENDED",
                """
                        {
                          "user_id": "11111111-1111-1111-1111-111111111111",
                          "enforcement_id": "22222222-2222-2222-2222-222222222222",
                          "reason_code": "SPAM_ABUSE",
                          "description": "Repeated policy violations",
                          "expires_at": "2026-12-31T00:00:00Z",
                          "enforced_by": "33333333-3333-3333-3333-333333333333",
                          "note": "internal admin note"
                        }
                        """
        );

        assertTrue(result.contains("\"target_user_id\":\"11111111-1111-1111-1111-111111111111\""));
        assertTrue(result.contains("\"enforcement_reason\":\"Repeated policy violations\""));
        assertTrue(result.contains("\"enforcement_expires_at\":\"2026-12-31T00:00:00Z\""));
        assertFalse(result.contains("enforced_by"));
        assertFalse(result.contains("internal admin note"));
        assertFalse(result.contains("\"description\""));
    }

    @Test
    void normalizeForStorage_redactsUnsafeDescriptionToReasonCodeFallback() {
        String result = normalizer.normalizeForStorage(
                "USER_RESTRICTED",
                """
                        {
                          "user_id": "11111111-1111-1111-1111-111111111111",
                          "reason_code": "POLICY_VIOLATION",
                          "description": "internal investigation only"
                        }
                        """
        );

        assertTrue(result.contains("\"enforcement_reason\":\"Policy violation\""));
    }
}
