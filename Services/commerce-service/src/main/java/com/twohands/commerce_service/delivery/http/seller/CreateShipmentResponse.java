package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateShipmentResponse(
        @JsonProperty("shipment_id") UUID shipmentId,
        @JsonProperty("order_id") UUID orderId,
        @JsonProperty("seller_id") UUID sellerId,
        ShipmentCarrier carrier,
        @JsonProperty("shipment_type") ShipmentType shipmentType,
        ShipmentStatus status,
        @JsonProperty("ghn_order_code") String ghnOrderCode,
        @JsonProperty("tracking_number") String trackingNumber,
        @JsonProperty("shipping_fee") BigDecimal shippingFee,
        @JsonProperty("cod_amount") BigDecimal codAmount,
        @JsonProperty("weight_gram") int weightGram,
        @JsonProperty("estimated_delivery_date") LocalDate estimatedDeliveryDate,
        @JsonProperty("order_item_ids") List<UUID> orderItemIds,
        @JsonProperty("created_at") Instant createdAt
) {
}
