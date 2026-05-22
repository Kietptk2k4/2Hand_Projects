package com.twohands.commerce_service.delivery.http.review;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ReviewMediaResponse(
        @JsonProperty("id")
        UUID id,
        String url,
        String type
) {
}
