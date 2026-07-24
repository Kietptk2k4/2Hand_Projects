package com.twohands.commerce_service.domain.shipment;

import java.time.LocalDate;

public record GhnLeadtimeResult(
        LocalDate estimatedDeliveryDate,
        String rawResponse
) {
}
