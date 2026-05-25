package com.twohands.notification_service.unit.domain.commerce;

import com.twohands.notification_service.domain.commerce.OrderConfirmationEmailSummaryPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderConfirmationEmailSummaryPolicyTest {

    @Test
    void formatAmountLine_returnsEmptyWhenMissing() {
        assertEquals("", OrderConfirmationEmailSummaryPolicy.formatAmountLine(null));
        assertEquals("", OrderConfirmationEmailSummaryPolicy.formatAmountLine("  "));
    }

    @Test
    void formatAmountLine_includesSanitizedTotal() {
        assertEquals(
                "\nOrder total: 100000 VND\n",
                OrderConfirmationEmailSummaryPolicy.formatAmountLine("100000 VND")
        );
    }
}
