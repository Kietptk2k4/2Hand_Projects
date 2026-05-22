package com.twohands.commerce_service.application.review.uploadreviewmedia;

public record ReviewMediaFileCommand(
        String originalFilename,
        String contentType,
        byte[] content
) {
}
