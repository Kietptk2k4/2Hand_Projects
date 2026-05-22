package com.twohands.commerce_service.application.order.trackorderstatus;

import java.util.UUID;

public record TrackOrderStatusCommand(
        UUID buyerId,
        UUID orderId
) {
}
