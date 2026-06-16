package com.twohands.commerce_service.infrastructure.objectstorage;

import com.twohands.commerce_service.common.media.ReviewMediaFileValidator;
import com.twohands.commerce_service.config.CommerceMinioConfig;
import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.domain.storage.ReviewMediaStorageGateway;
import com.twohands.commerce_service.domain.storage.ReviewMediaUploadPayload;
import com.twohands.commerce_service.domain.storage.StoredReviewMedia;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.UUID;

@Component
@ConditionalOnBean(name = CommerceMinioConfig.INTERNAL_MINIO_CLIENT)
public class MinioReviewMediaStorageAdapter implements ReviewMediaStorageGateway {

    private static final Logger log = LoggerFactory.getLogger(MinioReviewMediaStorageAdapter.class);

    private final MinioClient minioClient;
    private final CommerceObjectStorageProperties properties;
    private final ReviewMediaFileValidator reviewMediaFileValidator;

    public MinioReviewMediaStorageAdapter(
            @Qualifier(CommerceMinioConfig.INTERNAL_MINIO_CLIENT) MinioClient minioClient,
            CommerceObjectStorageProperties properties,
            ReviewMediaFileValidator reviewMediaFileValidator
    ) {
        this.minioClient = minioClient;
        this.properties = properties;
        this.reviewMediaFileValidator = reviewMediaFileValidator;
    }

    @Override
    public StoredReviewMedia upload(UUID buyerId, UUID reviewId, ReviewMediaUploadPayload payload) {
        String extension = reviewMediaFileValidator.resolveExtension(payload.contentType());
        String objectKey = "reviews/"
                + buyerId + "/"
                + reviewId + "/"
                + UUID.randomUUID() + "." + extension;

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(payload.content())) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getReviewBucket())
                            .object(objectKey)
                            .stream(inputStream, payload.content().length, -1)
                            .contentType(payload.contentType())
                            .build()
            );
        } catch (Exception ex) {
            throw new AppException(
                    ErrorCode.OBJECT_STORAGE_UNAVAILABLE,
                    "Failed to upload review media to object storage",
                    ex
            );
        }

        return new StoredReviewMedia(objectKey, buildPublicUrl(objectKey));
    }

    @Override
    public void deleteBestEffort(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getReviewBucket())
                            .object(objectKey)
                            .build()
            );
        } catch (Exception ex) {
            log.warn("Failed to cleanup orphan review media objectKey={}", objectKey, ex);
        }
    }

    private String buildPublicUrl(String objectKey) {
        String base = trimTrailingSlash(properties.getPublicUrl());
        return base + "/" + properties.getReviewBucket() + "/" + objectKey;
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
