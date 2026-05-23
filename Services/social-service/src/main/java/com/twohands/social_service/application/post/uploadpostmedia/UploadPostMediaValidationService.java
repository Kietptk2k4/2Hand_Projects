package com.twohands.social_service.application.post.uploadpostmedia;

import com.twohands.social_service.config.SocialObjectStorageProperties;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class UploadPostMediaValidationService {

    private final SocialObjectStorageProperties properties;

    public UploadPostMediaValidationService(SocialObjectStorageProperties properties) {
        this.properties = properties;
    }

    public String validateMediaKind(String rawMediaKind) {
        if (rawMediaKind == null || rawMediaKind.isBlank()) {
            throw fieldError("media_kind", "REQUIRED");
        }
        String normalized = rawMediaKind.trim().toUpperCase(Locale.ROOT);
        if (!"IMAGE".equals(normalized) && !"VIDEO".equals(normalized)) {
            throw fieldError("media_kind", "INVALID_VALUE");
        }
        return normalized;
    }

    public String validateContentType(String rawContentType, String mediaKind) {
        if (rawContentType == null || rawContentType.isBlank()) {
            throw fieldError("content_type", "REQUIRED");
        }
        String normalized = rawContentType.trim().toLowerCase(Locale.ROOT);
        List<String> allowed = allowedTypesFor(mediaKind);
        if (!allowed.contains(normalized)) {
            throw fieldError("content_type", "INVALID_VALUE");
        }
        return normalized;
    }

    public void validateFileSize(long fileSizeBytes, String mediaKind) {
        if (fileSizeBytes <= 0) {
            throw fieldError("file_size_bytes", "INVALID_VALUE");
        }
        long maxSize = maxSizeFor(mediaKind);
        if (fileSizeBytes > maxSize) {
            throw fieldError("file_size_bytes", "MAX_SIZE_EXCEEDED");
        }
    }

    public long maxSizeFor(String mediaKind) {
        return "VIDEO".equals(mediaKind)
                ? properties.getVideoMaxFileSizeBytes()
                : properties.getImageMaxFileSizeBytes();
    }

    public List<String> allowedTypesFor(String mediaKind) {
        return "VIDEO".equals(mediaKind)
                ? properties.getAllowedVideoContentTypes()
                : properties.getAllowedImageContentTypes();
    }

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
