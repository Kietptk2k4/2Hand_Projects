package com.twohands.notification_service.unit.domain.commerce;

import com.twohands.notification_service.domain.commerce.PaymentFailedReasonPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PaymentFailedReasonPolicyTest {

    @Test
    void resolveUserFacingReason_returnsSanitizedDescription() {
        assertEquals(
                "Insufficient balance",
                PaymentFailedReasonPolicy.resolveUserFacingReason("Insufficient balance", null)
        );
    }

    @Test
    void resolveUserFacingReason_omitsUnsafeProviderInternals() {
        assertNull(PaymentFailedReasonPolicy.resolveUserFacingReason(
                "Stripe webhook error: card_declined internal",
                null
        ));
    }

    @Test
    void resolveUserFacingReason_fallsBackToReasonCode() {
        assertEquals(
                "Insufficient funds",
                PaymentFailedReasonPolicy.resolveUserFacingReason(null, "INSUFFICIENT_FUNDS")
        );
    }
}
