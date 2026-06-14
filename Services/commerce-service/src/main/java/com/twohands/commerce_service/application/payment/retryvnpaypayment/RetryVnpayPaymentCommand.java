package com.twohands.commerce_service.application.payment.retryvnpaypayment;

import java.util.UUID;

public record RetryVnpayPaymentCommand(
        UUID orderId,
        UUID buyerId,
        String clientIp
) {
}
