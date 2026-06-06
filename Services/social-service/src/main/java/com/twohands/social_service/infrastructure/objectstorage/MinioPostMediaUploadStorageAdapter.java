package com.twohands.social_service.infrastructure.objectstorage;

import com.twohands.social_service.application.post.uploadpostmedia.PostMediaUploadIntent;
import com.twohands.social_service.application.post.uploadpostmedia.PostMediaUploadStoragePort;
import com.twohands.social_service.config.SocialObjectStorageProperties;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnBean(MinioClient.class)
public class MinioPostMediaUploadStorageAdapter implements PostMediaUploadStoragePort {

    private static final Logger log = LoggerFactory.getLogger(MinioPostMediaUploadStorageAdapter.class);

    private final MinioClient minioClient;
    private final SocialObjectStorageProperties properties;

    public MinioPostMediaUploadStorageAdapter(MinioClient minioClient, SocialObjectStorageProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @Override
    public PostMediaUploadIntent createUploadIntent(
            UUID userId,
            String contentType,
            String mediaKind,
            Instant expiresAt
    ) {
        String extension = resolveExtension(contentType);
        String objectKey = "posts/" + userId + "/" + UUID.randomUUID() + "." + extension;
        String mediaUrl = properties.buildPublicObjectUrl(objectKey);

        try {
            String uploadUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(properties.getPostBucket())
                            .object(objectKey)
                            .expiry(properties.getPresignedUrlTtlSeconds())
                            .extraHeaders(Map.of("Content-Type", contentType))
                            .build()
            );

            log.info(
                    "Post media upload URL issued. userId={}, mediaKind={}, objectKey={}, expiresAt={}",
                    userId,
                    mediaKind,
                    objectKey,
                    expiresAt
            );

            return new PostMediaUploadIntent(uploadUrl, objectKey, mediaUrl, mediaKind, expiresAt);
        } catch (Exception ex) {
            throw new AppException(
                    ErrorCode.OBJECT_STORAGE_UNAVAILABLE,
                    ErrorCode.OBJECT_STORAGE_UNAVAILABLE.defaultMessage(),
                    ex
            );
        }
    }

    private String resolveExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "video/mp4" -> "mp4";
            default -> throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "content_type",
                    "INVALID_VALUE"
            );
        };
    }
}
