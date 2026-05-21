package com.twohands.commerce_service.domain.shipping;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ShippingGroupFeeQuote(
        BigDecimal shippingFee,
        BigDecimal shippingFeeOrigin,
        LocalDate estimatedDeliveryDate
) {
}
