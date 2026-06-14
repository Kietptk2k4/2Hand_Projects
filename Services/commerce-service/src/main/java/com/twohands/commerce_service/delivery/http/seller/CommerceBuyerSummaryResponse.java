package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.order.CommerceBuyerSummary;

import java.util.UUID;

public record CommerceBuyerSummaryResponse(
        @JsonProperty("buyer_id") UUID buyerId,
        @JsonProperty("buyer_display_name") String buyerDisplayName,
        @JsonProperty("buyer_avatar_url") String buyerAvatarUrl
) {
    public static CommerceBuyerSummaryResponse from(CommerceBuyerSummary buyer) {
        if (buyer == null) {
            buyer = CommerceBuyerSummary.empty();
        }
        return new CommerceBuyerSummaryResponse(
                buyer.buyerId(),
                buyer.displayName(),
                buyer.avatarUrl()
        );
    }
}
