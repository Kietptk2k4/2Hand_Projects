package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateProductMediaRequest(
        @JsonProperty("media_urls")
        @NotNull(message = "media_urls is required")
        List<@NotBlank(message = "media URL must not be blank") @Size(max = 2048, message = "media URL must be at most 2048 characters") String> mediaUrls,

        @JsonProperty("thumbnail_url")
        @Size(max = 2048, message = "thumbnail_url must be at most 2048 characters")
        String thumbnailUrl
) {
}
