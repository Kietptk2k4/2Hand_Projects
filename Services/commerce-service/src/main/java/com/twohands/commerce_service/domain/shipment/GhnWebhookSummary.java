package com.twohands.commerce_service.domain.shipment;

import java.time.Instant;

public record GhnWebhookSummary(
        String carrierStatus,
        boolean processed,
        Instant receivedAt
) {
}
