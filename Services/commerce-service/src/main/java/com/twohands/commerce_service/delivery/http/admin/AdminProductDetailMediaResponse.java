package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminProductDetailMediaResponse(
        @JsonProperty("media_url") String mediaUrl,
        @JsonProperty("media_type") String mediaType,
        @JsonProperty("sort_order") int sortOrder
) {
}
