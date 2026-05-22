package com.twohands.commerce_service.delivery.http.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ViewProductDetailMediaResponse(
        @JsonProperty("media_id") UUID mediaId,
        @JsonProperty("media_url") String mediaUrl,
        @JsonProperty("media_type") String mediaType,
        @JsonProperty("sort_order") int sortOrder
) {
}
