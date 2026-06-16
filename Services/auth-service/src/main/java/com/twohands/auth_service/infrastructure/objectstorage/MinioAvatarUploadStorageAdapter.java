package com.twohands.auth_service.infrastructure.objectstorage;

import com.twohands.auth_service.application.useraccount.avatarupload.AvatarUploadIntent;
import com.twohands.auth_service.application.useraccount.avatarupload.AvatarUploadStoragePort;
import com.twohands.auth_service.config.AuthMinioConfig;
import com.twohands.auth_service.config.AuthObjectStorageProperties;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnBean(name = AuthMinioConfig.PRESIGN_MINIO_CLIENT)
public class MinioAvatarUploadStorageAdapter implements AvatarUploadStoragePort {

    private static final Logger log = LoggerFactory.getLogger(MinioAvatarUploadStorageAdapter.class);

    private final MinioClient minioClient;
    private final AuthObjectStorageProperties properties;

    public MinioAvatarUploadStorageAdapter(
            @Qualifier(AuthMinioConfig.PRESIGN_MINIO_CLIENT) MinioClient minioClient,
            AuthObjectStorageProperties properties
    ) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @Override
    public AvatarUploadIntent createUploadIntent(UUID userId, String contentType, Instant expiresAt) {
        return createUploadIntentForPrefix(userId, contentType, expiresAt, "avatars");
    }

    @Override
    public AvatarUploadIntent createCoverUploadIntent(UUID userId, String contentType, Instant expiresAt) {
        return createUploadIntentForPrefix(userId, contentType, expiresAt, "covers");
    }

    private AvatarUploadIntent createUploadIntentForPrefix(
            UUID userId,
            String contentType,
            Instant expiresAt,
            String prefix
    ) {
        String extension = resolveExtension(contentType);
        String objectKey = prefix + "/" + userId + "/" + UUID.randomUUID() + "." + extension;
        String publicUrl = buildPublicUrl(objectKey);

        try {
            log.debug(
                    "Issuing presigned upload URL. userId={}, prefix={}, bucket={}, contentType={}, presignedEndpoint={}",
                    userId,
                    prefix,
                    properties.getAvatarBucket(),
                    contentType,
                    properties.resolvePresignedEndpoint()
            );

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
                    "Profile image upload URL issued. userId={}, objectKey={}, uploadUrlHost={}, expiresAt={}",
                    userId,
                    objectKey,
                    extractHost(uploadUrl),
                    expiresAt
            );

            return new AvatarUploadIntent(uploadUrl, objectKey, publicUrl, expiresAt);
        } catch (Exception ex) {
            log.error(
                    "Failed to issue presigned upload URL. userId={}, prefix={}, bucket={}, contentType={}",
                    userId,
                    prefix,
                    properties.getAvatarBucket(),
                    contentType,
                    ex
            );
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

    private static String extractHost(String url) {
        try {
            return java.net.URI.create(url).getHost();
        } catch (Exception ignored) {
            return "unknown";
        }
    }
}
