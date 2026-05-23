package com.twohands.auth_service.infrastructure.objectstorage;

import com.twohands.auth_service.application.useraccount.avatarupload.AvatarUploadIntent;
import com.twohands.auth_service.application.useraccount.avatarupload.AvatarUploadStoragePort;
import com.twohands.auth_service.config.AuthObjectStorageProperties;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
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
public class MinioAvatarUploadStorageAdapter implements AvatarUploadStoragePort {

    private static final Logger log = LoggerFactory.getLogger(MinioAvatarUploadStorageAdapter.class);

    private final MinioClient minioClient;
    private final AuthObjectStorageProperties properties;

    public MinioAvatarUploadStorageAdapter(MinioClient minioClient, AuthObjectStorageProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @Override
    public AvatarUploadIntent createUploadIntent(UUID userId, String contentType, Instant expiresAt) {
        String extension = resolveExtension(contentType);
        String objectKey = "avatars/" + userId + "/" + UUID.randomUUID() + "." + extension;
        String avatarUrl = buildPublicUrl(objectKey);

        try {
            String uploadUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(properties.getAvatarBucket())
                            .object(objectKey)
                            .expiry(properties.getPresignedUrlTtlSeconds())
                            .extraHeaders(Map.of("Content-Type", contentType))
                            .build()
            );

            log.info(
                    "Avatar upload URL issued. userId={}, objectKey={}, expiresAt={}",
                    userId,
                    objectKey,
                    expiresAt
            );

            return new AvatarUploadIntent(uploadUrl, objectKey, avatarUrl, expiresAt);
        } catch (Exception ex) {
            throw new AppException(
                    ErrorCode.OBJECT_STORAGE_UNAVAILABLE,
                    ErrorCode.OBJECT_STORAGE_UNAVAILABLE.defaultMessage(),
                    ex
            );
        }
    }

    private String buildPublicUrl(String objectKey) {
        String base = properties.getPublicUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + objectKey;
    }

    private String resolveExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Content type is not allowed",
                    "content_type",
                    "INVALID_VALUE"
            );
        };
    }
}
