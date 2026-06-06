package com.twohands.commerce_service.delivery.http.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SellerShippingFeeGroupResponse(
        @JsonProperty("seller_id")
        UUID sellerId,
        @JsonProperty("shop_id")
        UUID shopId,
        @JsonProperty("shipping_fee")
        BigDecimal shippingFee,
        @JsonProperty("shipping_fee_origin")
        BigDecimal shippingFeeOrigin,
        @JsonProperty("estimated_delivery_date")
        LocalDate estimatedDeliveryDate,
        @JsonProperty("shipment_type")
        ShipmentType shipmentType
) {
}
