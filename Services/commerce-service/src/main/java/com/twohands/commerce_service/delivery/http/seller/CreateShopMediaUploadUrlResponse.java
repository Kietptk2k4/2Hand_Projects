package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateShopMediaUploadUrlResponse(
        @JsonProperty("upload_url")
        String uploadUrl,

        @JsonProperty("object_key")
        String objectKey,

        @JsonProperty("media_url")
        String mediaUrl,

        @JsonProperty("media_kind")
        String mediaKind,

        @JsonProperty("expires_at")
        String expiresAt,

        @JsonProperty("max_file_size_bytes")
        long maxFileSizeBytes,

        @JsonProperty("allowed_content_types")
        List<String> allowedContentTypes
) {
}
