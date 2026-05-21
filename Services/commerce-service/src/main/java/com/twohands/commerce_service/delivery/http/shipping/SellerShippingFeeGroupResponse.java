package com.twohands.commerce_service.delivery.http.shipping;

import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SellerShippingFeeGroupResponse(
        UUID sellerId,
        UUID shopId,
        BigDecimal shippingFee,
        BigDecimal shippingFeeOrigin,
        LocalDate estimatedDeliveryDate,
        ShipmentType shipmentType
) {
}
