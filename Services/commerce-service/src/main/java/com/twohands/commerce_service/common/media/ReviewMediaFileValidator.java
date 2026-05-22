package com.twohands.commerce_service.common.media;

import com.twohands.commerce_service.domain.review.ReviewMediaType;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class ReviewMediaFileValidator {

    public static final int MAX_MEDIA_PER_REVIEW = 10;
    public static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    public static final long MAX_VIDEO_BYTES = 50L * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "video/mp4",
            "video/webm"
    );

    private static final Map<String, ReviewMediaType> CONTENT_TYPE_TO_MEDIA_TYPE = Map.of(
            "image/jpeg", ReviewMediaType.IMAGE,
            "image/png", ReviewMediaType.IMAGE,
            "image/webp", ReviewMediaType.IMAGE,
            "video/mp4", ReviewMediaType.VIDEO,
            "video/webm", ReviewMediaType.VIDEO
    );

    private static final Map<String, String> CONTENT_TYPE_TO_EXTENSION = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "video/mp4", "mp4",
            "video/webm", "webm"
    );

    public ReviewMediaType resolveMediaType(String contentType) {
        String normalized = normalizeContentType(contentType);
        ReviewMediaType mediaType = CONTENT_TYPE_TO_MEDIA_TYPE.get(normalized);
        if (mediaType == null) {
            throw new AppException(
                    ErrorCode.INVALID_MEDIA_TYPE,
                    "Media type is not allowed",
                    "files",
                    "allowed types: image/jpeg, image/png, image/webp, video/mp4, video/webm"
            );
        }
        return mediaType;
    }

    public String resolveExtension(String contentType) {
        String normalized = normalizeContentType(contentType);
        String extension = CONTENT_TYPE_TO_EXTENSION.get(normalized);
        if (extension == null) {
            throw new AppException(ErrorCode.INVALID_MEDIA_TYPE, "Media type is not allowed");
        }
        return extension;
    }

    public void validateFile(String fieldName, String contentType, long sizeBytes) {
        if (!StringUtils.hasText(contentType)) {
            throw fieldError(fieldName, "content type is required");
        }
        String normalized = normalizeContentType(contentType);
        if (!ALLOWED_CONTENT_TYPES.contains(normalized)) {
            throw new AppException(
                    ErrorCode.INVALID_MEDIA_TYPE,
                    "Media type is not allowed",
                    fieldName,
                    "allowed types: image/jpeg, image/png, image/webp, video/mp4, video/webm"
            );
        }
        if (sizeBytes <= 0) {
            throw fieldError(fieldName, "file must not be empty");
        }
        ReviewMediaType mediaType = CONTENT_TYPE_TO_MEDIA_TYPE.get(normalized);
        long maxBytes = mediaType == ReviewMediaType.VIDEO ? MAX_VIDEO_BYTES : MAX_IMAGE_BYTES;
        if (sizeBytes > maxBytes) {
            throw new AppException(
                    ErrorCode.INVALID_MEDIA_SIZE,
                    "Media file size exceeds limit",
                    fieldName,
                    mediaType == ReviewMediaType.VIDEO
                            ? "max video size is " + MAX_VIDEO_BYTES + " bytes"
                            : "max image size is " + MAX_IMAGE_BYTES + " bytes"
            );
        }
    }

    private String normalizeContentType(String contentType) {
        return contentType.trim().toLowerCase(Locale.ROOT);
    }

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
