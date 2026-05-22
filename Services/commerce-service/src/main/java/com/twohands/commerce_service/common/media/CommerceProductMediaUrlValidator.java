package com.twohands.commerce_service.common.media;

import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CommerceProductMediaUrlValidator {

    private final CommerceObjectStorageProperties properties;

    public CommerceProductMediaUrlValidator(CommerceObjectStorageProperties properties) {
        this.properties = properties;
    }

    public void validateRequiredProductMedia(String mediaUrl) {
        if (!properties.isEnabled()) {
            return;
        }
        if (!StringUtils.hasText(mediaUrl)) {
            throw new AppException(
                    ErrorCode.INVALID_MEDIA_URL,
                    "At least one product media URL is required when MinIO is enabled"
            );
        }
        validateProductBucketUrl(mediaUrl);
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
}
