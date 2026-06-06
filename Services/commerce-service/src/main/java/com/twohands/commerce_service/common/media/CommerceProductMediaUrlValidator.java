package com.twohands.commerce_service.common.media;

import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.domain.product.ProductMediaType;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class CommerceProductMediaUrlValidator {

    private final CommerceObjectStorageProperties properties;
    private final ProductMediaContentValidator productMediaContentValidator;

    public CommerceProductMediaUrlValidator(
            CommerceObjectStorageProperties properties,
            ProductMediaContentValidator productMediaContentValidator
    ) {
        this.properties = properties;
        this.productMediaContentValidator = productMediaContentValidator;
    }

    public int maxProductMediaCount() {
        return properties.getProductMediaMaxCount();
    }

    public int maxProductVideoCount() {
        return properties.getProductMediaMaxVideoCount();
    }

    public void validateRequiredProductMedia(String mediaUrl) {
        if (!properties.isEnabled()) {
            return;
        }
        if (!StringUtils.hasText(mediaUrl)) {
            throw new AppException(
                    ErrorCode.INVALID_MEDIA_URL,
                    "At least one product image is required when MinIO is enabled"
            );
        }
        validateProductBucketUrl(mediaUrl);
        if (productMediaContentValidator.inferMediaTypeFromUrl(mediaUrl) != ProductMediaType.IMAGE) {
            throw new AppException(
                    ErrorCode.INVALID_MEDIA_URL,
                    "Primary product media must be an image"
            );
        }
    }

    public void validateProductMediaUrls(List<String> mediaUrls) {
        if (!properties.isEnabled()) {
            return;
        }
        validateProductMediaComposition(mediaUrls);
    }

    public void validateProductMediaComposition(List<String> mediaUrls) {
        if (!properties.isEnabled()) {
            return;
        }

        int imageCount = 0;
        int videoCount = 0;

        for (String mediaUrl : mediaUrls) {
            validateProductBucketUrl(mediaUrl);
            ProductMediaType mediaType = productMediaContentValidator.inferMediaTypeFromUrl(mediaUrl);
            if (mediaType == ProductMediaType.IMAGE) {
                imageCount++;
            } else {
                videoCount++;
            }
        }

        if (!mediaUrls.isEmpty() && imageCount == 0) {
            throw fieldError("media_urls", "must include at least one image");
        }
        if (imageCount > maxProductMediaCount()) {
            throw fieldError(
                    "media_urls",
                    "must contain at most " + maxProductMediaCount() + " images"
            );
        }
        if (videoCount > maxProductVideoCount()) {
            throw fieldError(
                    "media_urls",
                    "must contain at most " + maxProductVideoCount() + " videos"
            );
        }
    }

    private void validateProductBucketUrl(String url) {
        String normalized = url.trim();
        String bucketSegment = "/" + properties.getProductBucket().trim() + "/";
        String publicBase = trimTrailingSlash(properties.getPublicUrl());
        if (!normalized.contains(bucketSegment) && !normalized.startsWith(publicBase + bucketSegment)) {
            throw new AppException(
                    ErrorCode.INVALID_MEDIA_URL,
                    "Product media must reference MinIO product bucket " + properties.getProductBucket()
            );
        }
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
