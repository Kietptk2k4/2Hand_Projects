package com.twohands.commerce_service.delivery.http.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PageMetaResponse(
        int page,
        int limit,
        @JsonProperty("total_items") long totalItems,
        @JsonProperty("total_pages") int totalPages,
        @JsonProperty("has_next") boolean hasNext
) {
}
