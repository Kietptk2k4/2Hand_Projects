package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.shop.ShopStatus;

import java.time.Instant;
import java.util.UUID;

public record UpdateShopVacationResponse(
        @JsonProperty("shop_id") UUID shopId,
        @JsonProperty("seller_id") UUID sellerId,
        ShopStatus status,
        @JsonProperty("is_vacation") boolean isVacation,
        @JsonProperty("vacation_message") String vacationMessage,
        @JsonProperty("updated_at") Instant updatedAt
) {
}
