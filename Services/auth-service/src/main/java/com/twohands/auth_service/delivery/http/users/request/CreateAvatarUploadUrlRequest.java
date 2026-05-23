package com.twohands.auth_service.delivery.http.users.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateAvatarUploadUrlRequest(
        @JsonProperty("content_type")
        @NotBlank(message = "Content type is required")
        String contentType,

        @JsonProperty("file_size_bytes")
        @NotNull(message = "File size is required")
        @Positive(message = "File size must be greater than zero")
        Long fileSizeBytes
) {
}
