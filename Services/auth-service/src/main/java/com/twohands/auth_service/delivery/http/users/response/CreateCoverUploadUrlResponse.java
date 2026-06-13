package com.twohands.auth_service.delivery.http.users.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateCoverUploadUrlResponse(
        @JsonProperty("upload_url")
        String uploadUrl,

        @JsonProperty("object_key")
        String objectKey,

        @JsonProperty("cover_url")
        String coverUrl,

        @JsonProperty("expires_at")
        String expiresAt,

        @JsonProperty("max_file_size_bytes")
        long maxFileSizeBytes,

        @JsonProperty("allowed_content_types")
        List<String> allowedContentTypes
) {
}
