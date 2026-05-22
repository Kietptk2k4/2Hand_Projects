package com.twohands.commerce_service.application.payment.viewpaymentstatus;

import java.util.UUID;

public record ViewPaymentStatusCommand(
        UUID buyerId,
        UUID paymentId
) {
}
