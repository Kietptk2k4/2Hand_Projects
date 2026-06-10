package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.finance.PlatformTopSeller;

import java.math.BigDecimal;
import java.util.UUID;

public record PlatformTopSellerResponse(
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("shop_name") String shopName,
        @JsonProperty("recognized_gross") BigDecimal recognizedGross,
        @JsonProperty("platform_fee") BigDecimal platformFee,
        @JsonProperty("completed_item_count") long completedItemCount
) {
    public static PlatformTopSellerResponse from(PlatformTopSeller seller) {
        return new PlatformTopSellerResponse(
                seller.sellerId(),
                seller.shopName(),
                seller.recognizedGross(),
                seller.platformFee(),
                seller.completedItemCount()
        );
    }
}
