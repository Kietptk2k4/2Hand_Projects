package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateShopMediaUploadUrlRequest(
        @JsonProperty("content_type")
        @NotBlank(message = "Content type is required")
        String contentType,

        @JsonProperty("file_size_bytes")
        @NotNull(message = "File size is required")
        @Positive(message = "File size must be greater than zero")
        Long fileSizeBytes,

        @JsonProperty("media_kind")
        @NotBlank(message = "Media kind is required")
        String mediaKind,

        @JsonProperty("client_upload_origin")
        String clientUploadOrigin
) {
}
