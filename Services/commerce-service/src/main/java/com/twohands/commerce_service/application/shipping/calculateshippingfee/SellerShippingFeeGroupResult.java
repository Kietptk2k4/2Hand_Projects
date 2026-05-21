package com.twohands.commerce_service.application.shipping.calculateshippingfee;

import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SellerShippingFeeGroupResult(
        UUID sellerId,
        UUID shopId,
        BigDecimal shippingFee,
        BigDecimal shippingFeeOrigin,
        LocalDate estimatedDeliveryDate,
        ShipmentType shipmentType
) {
}
