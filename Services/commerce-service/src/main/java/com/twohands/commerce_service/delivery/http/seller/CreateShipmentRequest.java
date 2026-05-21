package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

public record CreateShipmentRequest(
        @JsonProperty("order_id")
        @NotNull(message = "order_id is required")
        UUID orderId,

        @JsonProperty("order_item_ids")
        @NotEmpty(message = "order_item_ids must not be empty")
        List<UUID> orderItemIds,

        @NotBlank(message = "carrier is required")
        String carrier,

        @JsonProperty("shipment_type")
        @NotBlank(message = "shipment_type is required")
        String shipmentType,

        @JsonProperty("weight_gram")
        @Positive(message = "weight_gram must be greater than 0")
        Integer weightGram,

        @JsonProperty("tracking_number")
        String trackingNumber
) {
}
