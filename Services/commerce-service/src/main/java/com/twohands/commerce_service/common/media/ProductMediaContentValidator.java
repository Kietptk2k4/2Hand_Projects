package com.twohands.commerce_service.common.media;

import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.domain.product.ProductMediaKind;
import com.twohands.commerce_service.domain.product.ProductMediaType;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class ProductMediaContentValidator {

    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final Set<String> VIDEO_CONTENT_TYPES = Set.of(
            "video/mp4",
            "video/webm"
    );

    private static final Map<String, String> CONTENT_TYPE_TO_EXTENSION = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "video/mp4", "mp4",
            "video/webm", "webm"
    );

    private final CommerceObjectStorageProperties properties;

    public ProductMediaContentValidator(CommerceObjectStorageProperties properties) {
        this.properties = properties;
    }

    public void validateContentTypeForMediaKind(String contentType, String mediaKind) {
        String normalized = normalizeContentType(contentType);
        assertAllowedContentType(normalized);

        if (ProductMediaKind.PRODUCT_VIDEO.equals(mediaKind)) {
            if (!VIDEO_CONTENT_TYPES.contains(normalized)) {
                throw invalidMediaType("content_type", "video content type required for PRODUCT_VIDEO");
            }
            return;
        }

        if (!IMAGE_CONTENT_TYPES.contains(normalized)) {
            throw invalidMediaType("content_type", "image content type required for PRODUCT_IMAGE");
        }
    }

    public void validateUploadFile(String contentType, long sizeBytes) {
        String normalized = normalizeContentType(contentType);
        assertAllowedContentType(normalized);

        if (sizeBytes <= 0) {
            throw fieldError("file_size_bytes", "must be greater than 0");
        }

        long maxBytes = maxBytesForContentType(normalized);
        if (sizeBytes > maxBytes) {
            throw new AppException(
                    ErrorCode.INVALID_MEDIA_SIZE,
                    "Media file size exceeds limit",
                    "file_size_bytes",
                    "max size is " + maxBytes + " bytes"
            );
        }
    }

    public long maxBytesForContentType(String contentType) {
        String normalized = normalizeContentType(contentType);
        if (VIDEO_CONTENT_TYPES.contains(normalized)) {
            return properties.getProductMediaMaxVideoFileSizeBytes();
        }
        return properties.getProductMediaMaxFileSizeBytes();
    }

    public String resolveExtension(String contentType) {
        String normalized = normalizeContentType(contentType);
        String extension = CONTENT_TYPE_TO_EXTENSION.get(normalized);
        if (extension == null) {
            throw invalidMediaType("content_type", "unsupported content type");
        }
        return extension;
    }

    public ProductMediaType inferMediaTypeFromUrl(String mediaUrl) {
        String normalized = mediaUrl.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("/videos/") || endsWithVideoExtension(normalized)) {
            return ProductMediaType.VIDEO;
        }
        if (normalized.contains("/images/") || endsWithImageExtension(normalized)) {
            return ProductMediaType.IMAGE;
        }
        throw new AppException(
                ErrorCode.INVALID_MEDIA_URL,
                "Cannot infer product media type from URL"
        );
    }

    public boolean isImageUrl(String mediaUrl) {
        return inferMediaTypeFromUrl(mediaUrl) == ProductMediaType.IMAGE;
    }

    private boolean endsWithImageExtension(String url) {
        return url.endsWith(".jpg")
                || url.endsWith(".jpeg")
                || url.endsWith(".png")
                || url.endsWith(".webp");
    }

    private boolean endsWithVideoExtension(String url) {
        return url.endsWith(".mp4") || url.endsWith(".webm");
    }

    private void assertAllowedContentType(String normalized) {
        if (!properties.getAllowedProductMediaContentTypes().contains(normalized)) {
            throw invalidMediaType("content_type", "content type is not allowed");
        }
    }

    private String normalizeContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            throw fieldError("content_type", "must not be blank");
        }
        return contentType.trim().toLowerCase(Locale.ROOT);
    }

    private AppException invalidMediaType(String field, String reason) {
        return new AppException(ErrorCode.INVALID_MEDIA_TYPE, "Media type is not allowed", field, reason);
    }

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}