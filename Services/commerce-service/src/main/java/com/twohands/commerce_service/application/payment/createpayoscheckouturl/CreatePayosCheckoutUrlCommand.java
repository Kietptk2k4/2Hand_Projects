package com.twohands.commerce_service.application.payment.createpayoscheckouturl;

import java.util.UUID;

public record CreatePayosCheckoutUrlCommand(
        UUID paymentId,
        UUID buyerId
) {
}
