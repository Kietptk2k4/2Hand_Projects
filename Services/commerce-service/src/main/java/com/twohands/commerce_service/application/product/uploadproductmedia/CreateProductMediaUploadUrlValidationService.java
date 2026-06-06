package com.twohands.commerce_service.application.product.uploadproductmedia;

import com.twohands.commerce_service.common.media.ProductMediaContentValidator;
import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.domain.product.ProductMediaKind;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class CreateProductMediaUploadUrlValidationService {

    public static final String MEDIA_KIND_PRODUCT_IMAGE = ProductMediaKind.PRODUCT_IMAGE;
    public static final String MEDIA_KIND_PRODUCT_VIDEO = ProductMediaKind.PRODUCT_VIDEO;

    private final CommerceObjectStorageProperties properties;
    private final ProductMediaContentValidator productMediaContentValidator;

    public CreateProductMediaUploadUrlValidationService(
            CommerceObjectStorageProperties properties,
            ProductMediaContentValidator productMediaContentValidator
    ) {
        this.properties = properties;
        this.productMediaContentValidator = productMediaContentValidator;
    }

    public String validateMediaKind(String rawMediaKind) {
        if (rawMediaKind == null || rawMediaKind.isBlank()) {
            throw fieldError("media_kind", "REQUIRED");
        }
        String normalized = rawMediaKind.trim().toUpperCase(Locale.ROOT);
        if (!MEDIA_KIND_PRODUCT_IMAGE.equals(normalized) && !MEDIA_KIND_PRODUCT_VIDEO.equals(normalized)) {
            throw fieldError("media_kind", "INVALID_VALUE");
        }
        return normalized;
    }

    public String validateContentType(String rawContentType, String mediaKind) {
        if (rawContentType == null || rawContentType.isBlank()) {
            throw fieldError("content_type", "REQUIRED");
        }
        String normalized = rawContentType.trim().toLowerCase(Locale.ROOT);
        productMediaContentValidator.validateContentTypeForMediaKind(normalized, mediaKind);
        return normalized;
    }

    public void validateFileSize(long fileSizeBytes, String contentType) {
        productMediaContentValidator.validateUploadFile(contentType, fileSizeBytes);
    }

    public long maxFileSizeBytesForContentType(String contentType) {
        return productMediaContentValidator.maxBytesForContentType(contentType);
    }

    public List<String> allowedContentTypes() {
        return List.copyOf(properties.getAllowedProductMediaContentTypes());
    }

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}