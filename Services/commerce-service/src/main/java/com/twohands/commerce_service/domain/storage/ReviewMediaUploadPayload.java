package com.twohands.commerce_service.domain.storage;

public record ReviewMediaUploadPayload(
        String originalFilename,
        String contentType,
        byte[] content
) {
}
