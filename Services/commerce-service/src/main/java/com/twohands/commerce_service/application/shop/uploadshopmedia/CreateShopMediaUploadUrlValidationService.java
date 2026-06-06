package com.twohands.commerce_service.application.shop.uploadshopmedia;

import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class CreateShopMediaUploadUrlValidationService {

    private final CommerceObjectStorageProperties properties;

    public CreateShopMediaUploadUrlValidationService(CommerceObjectStorageProperties properties) {
        this.properties = properties;
    }

    public String validateMediaKind(String rawMediaKind) {
        if (rawMediaKind == null || rawMediaKind.isBlank()) {
            throw fieldError("media_kind", "REQUIRED");
        }
        String normalized = rawMediaKind.trim().toUpperCase(Locale.ROOT);
        if (!"SHOP_AVATAR".equals(normalized)
                && !"SHOP_COVER".equals(normalized)
                && !"PRODUCT_THUMBNAIL".equals(normalized)) {
            throw fieldError("media_kind", "INVALID_VALUE");
        }
        return normalized;
    }

    public String validateContentType(String rawContentType) {
        if (rawContentType == null || rawContentType.isBlank()) {
            throw fieldError("content_type", "REQUIRED");
        }
        String normalized = rawContentType.trim().toLowerCase(Locale.ROOT);
        if (!properties.getAllowedShopMediaContentTypes().contains(normalized)) {
            throw fieldError("content_type", "INVALID_VALUE");
        }
        return normalized;
    }

    public void validateFileSize(long fileSizeBytes) {
        if (fileSizeBytes <= 0) {
            throw fieldError("file_size_bytes", "INVALID_VALUE");
        }
        if (fileSizeBytes > properties.getShopMediaMaxFileSizeBytes()) {
            throw fieldError("file_size_bytes", "MAX_SIZE_EXCEEDED");
        }
    }

    public List<String> allowedContentTypes() {
        return List.copyOf(properties.getAllowedShopMediaContentTypes());
    }

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
