package com.twohands.commerce_service.application.payment.createvnpaycheckouturl;

import java.util.UUID;

public record CreateVnpayCheckoutUrlCommand(
        UUID paymentId,
        UUID buyerId,
        String clientIp
) {
}
