package com.twohands.commerce_service.delivery.http.checkout;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.math.BigDecimal;
import java.util.UUID;

public record SellerShippingGroupResponse(
        @JsonProperty("seller_id")
        UUID sellerId,
        @JsonProperty("shop_id")
        UUID shopId,
        @JsonProperty("shipping_fee")
        BigDecimal shippingFee,
        @JsonProperty("shipment_type")
        ShipmentType shipmentType
) {
}
