package com.twohands.auth_service.application.useraccount.avatarupload;

import com.twohands.auth_service.config.AuthObjectStorageProperties;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class CreateAvatarUploadUrlValidationService {

    private final AuthObjectStorageProperties properties;

    public CreateAvatarUploadUrlValidationService(AuthObjectStorageProperties properties) {
        this.properties = properties;
    }

    public String validateContentType(String rawContentType) {
        if (rawContentType == null || rawContentType.isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Content type is required", "content_type", "REQUIRED");
        }
        String normalized = rawContentType.trim().toLowerCase();
        if (!properties.getAllowedAvatarContentTypes().contains(normalized)) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Content type is not allowed",
                    "content_type",
                    "INVALID_VALUE"
            );
        }
        return normalized;
    }

    public void validateFileSize(long fileSizeBytes) {
        if (fileSizeBytes <= 0) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "File size must be greater than zero",
                    "file_size_bytes",
                    "INVALID_VALUE"
            );
        }
        if (fileSizeBytes > properties.getAvatarMaxFileSizeBytes()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "File size exceeds maximum allowed",
                    "file_size_bytes",
                    "MAX_SIZE_EXCEEDED"
            );
        }
    }
}
