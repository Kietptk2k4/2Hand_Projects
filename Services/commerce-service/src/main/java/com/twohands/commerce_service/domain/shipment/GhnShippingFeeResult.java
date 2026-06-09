package com.twohands.commerce_service.domain.shipment;

import java.math.BigDecimal;

public record GhnShippingFeeResult(
        BigDecimal totalFee,
        BigDecimal serviceFee,
        String rawResponse,
        boolean mock
) {
}
