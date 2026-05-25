package com.twohands.notification_service.unit.domain.commerce;

import com.twohands.notification_service.domain.commerce.PaymentSuccessEmailSummaryPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentSuccessEmailSummaryPolicyTest {

    @Test
    void formatSummaryLine_returnsEmptyWhenNoDetails() {
        assertEquals("", PaymentSuccessEmailSummaryPolicy.formatSummaryLine(null, null));
    }

    @Test
    void formatSummaryLine_includesAmountAndPaymentMethod() {
        String line = PaymentSuccessEmailSummaryPolicy.formatSummaryLine("100000 VND", "COD");

        assertTrue(line.contains("Amount paid: 100000 VND"));
        assertTrue(line.contains("Payment method: COD"));
    }

    @Test
    void formatSummaryLine_includesAmountOnly() {
        assertEquals(
                "\nAmount paid: 100000\n",
                PaymentSuccessEmailSummaryPolicy.formatSummaryLine("100000", null)
        );
    }
}
