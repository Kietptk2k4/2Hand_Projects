package com.twohands.social_service.delivery.http.post.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UploadPostMediaRequest(
        @NotBlank(message = "content_type is required")
        @JsonProperty("content_type")
        String contentType,

        @NotNull(message = "file_size_bytes is required")
        @Positive(message = "file_size_bytes must be greater than zero")
        @JsonProperty("file_size_bytes")
        Long fileSizeBytes,

        @NotBlank(message = "media_kind is required")
        @JsonProperty("media_kind")
        String mediaKind,

        @JsonProperty("client_upload_origin")
        String clientUploadOrigin
) {
}
