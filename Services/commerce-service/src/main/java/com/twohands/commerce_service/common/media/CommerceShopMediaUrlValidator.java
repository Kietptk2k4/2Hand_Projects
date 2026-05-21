package com.twohands.commerce_service.common.media;

import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CommerceShopMediaUrlValidator {

    private final CommerceObjectStorageProperties properties;

    public CommerceShopMediaUrlValidator(CommerceObjectStorageProperties properties) {
        this.properties = properties;
    }

    public void validateOptionalUrl(String fieldName, String url) {
        if (!StringUtils.hasText(url)) {
            return;
        }
        if (!properties.isEnabled()) {
            return;
        }
        String normalized = url.trim();
        String bucketSegment = "/" + properties.getShopBucket().trim() + "/";
        String publicBase = trimTrailingSlash(properties.getPublicUrl());
        if (!normalized.contains(bucketSegment) && !normalized.startsWith(publicBase + bucketSegment)) {
            throw new AppException(
                    ErrorCode.INVALID_MEDIA_URL,
                    fieldName + " must reference MinIO shop bucket " + properties.getShopBucket()
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
