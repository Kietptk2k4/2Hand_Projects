package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

public record UpdateShopProfileRequest(
        @JsonProperty("shop_name")
        @Size(max = 255, message = "shop_name must be at most 255 characters")
        String shopName,

        String description,

        @JsonProperty("avatar_url")
        String avatarUrl,

        @JsonProperty("cover_url")
        String coverUrl
) {
}
