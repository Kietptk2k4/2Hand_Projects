package com.twohands.commerce_service.delivery.http.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.review.ReviewMediaType;

import java.util.UUID;

public record ProductReviewMediaResponse(
        @JsonProperty("media_id") UUID mediaId,
        String url,
        @JsonProperty("media_type") ReviewMediaType mediaType
) {
}
