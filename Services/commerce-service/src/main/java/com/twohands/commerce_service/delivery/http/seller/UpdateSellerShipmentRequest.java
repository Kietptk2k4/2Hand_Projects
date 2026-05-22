package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateSellerShipmentRequest(
        String status,
        @JsonProperty("tracking_number") String trackingNumber
) {
}
