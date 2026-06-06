package com.twohands.commerce_service.application.product.uploadproductmedia;

import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class CreateProductMediaUploadUrlValidationService {

    public static final String MEDIA_KIND_PRODUCT_IMAGE = "PRODUCT_IMAGE";

    private final CommerceObjectStorageProperties properties;

    public CreateProductMediaUploadUrlValidationService(CommerceObjectStorageProperties properties) {
        this.properties = properties;
    }

    public String validateMediaKind(String rawMediaKind) {
        if (rawMediaKind == null || rawMediaKind.isBlank()) {
            throw fieldError("media_kind", "REQUIRED");
        }
        String normalized = rawMediaKind.trim().toUpperCase(Locale.ROOT);
        if (!MEDIA_KIND_PRODUCT_IMAGE.equals(normalized)) {
            throw fieldError("media_kind", "INVALID_VALUE");
        }
        return normalized;
    }

    public String validateContentType(String rawContentType) {
        if (rawContentType == null || rawContentType.isBlank()) {
            throw fieldError("content_type", "REQUIRED");
        }
        String normalized = rawContentType.trim().toLowerCase(Locale.ROOT);
        if (!properties.getAllowedProductMediaContentTypes().contains(normalized)) {
            throw fieldError("content_type", "INVALID_VALUE");
        }
        return normalized;
    }

    public void validateFileSize(long fileSizeBytes) {
        if (fileSizeBytes <= 0) {
            throw fieldError("file_size_bytes", "INVALID_VALUE");
        }
        if (fileSizeBytes > properties.getProductMediaMaxFileSizeBytes()) {
            throw fieldError("file_size_bytes", "MAX_SIZE_EXCEEDED");
        }
    }

    public List<String> allowedContentTypes() {
        return List.copyOf(properties.getAllowedProductMediaContentTypes());
    }

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
