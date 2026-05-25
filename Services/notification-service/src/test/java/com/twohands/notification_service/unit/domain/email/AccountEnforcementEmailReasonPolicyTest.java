package com.twohands.notification_service.unit.domain.email;

import com.twohands.notification_service.domain.email.AccountEnforcementEmailReasonPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountEnforcementEmailReasonPolicyTest {

    @Test
    void resolveUserFacingReason_usesDescriptionWhenSafe() {
        assertEquals(
                "Repeated policy violations",
                AccountEnforcementEmailReasonPolicy.resolveUserFacingReason(
                        "Repeated policy violations",
                        "SPAM_ABUSE"
                )
        );
    }

    @Test
    void resolveUserFacingReason_fallsBackToReasonCodeWhenDescriptionContainsInternalTerms() {
        assertEquals(
                "Spam abuse",
                AccountEnforcementEmailReasonPolicy.resolveUserFacingReason(
                        "internal admin investigation notes",
                        "SPAM_ABUSE"
                )
        );
    }

    @Test
    void formatReasonLine_returnsEmptyForBlankReason() {
        assertEquals("", AccountEnforcementEmailReasonPolicy.formatReasonLine("  "));
    }

    @Test
    void formatExpiresAtLine_includesExpiryWhenPresent() {
        String line = AccountEnforcementEmailReasonPolicy.formatExpiresAtLine("2026-12-31T00:00:00Z");

        assertTrue(line.contains("2026-12-31T00:00:00Z"));
        assertFalse(line.contains("internal"));
    }
}
