package com.twohands.commerce_service.application.order.confirmorderreceived;

import java.util.UUID;

public record ConfirmOrderReceivedCommand(
        UUID buyerId,
        UUID orderId
) {
}
