package com.twohands.commerce_service.delivery.http.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.moderation.ReviewModerationParties;

import java.util.UUID;

public record ReviewModerationPartiesResponse(
        @JsonProperty("review_id") UUID reviewId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("buyer_id") UUID buyerId
) {

    public static ReviewModerationPartiesResponse from(ReviewModerationParties parties) {
        return new ReviewModerationPartiesResponse(parties.reviewId(), parties.sellerId(), parties.buyerId());
    }
}
