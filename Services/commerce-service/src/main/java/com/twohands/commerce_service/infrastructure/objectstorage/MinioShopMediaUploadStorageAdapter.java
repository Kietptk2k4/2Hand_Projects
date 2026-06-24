package com.twohands.commerce_service.infrastructure.objectstorage;

import com.twohands.commerce_service.application.shop.uploadshopmedia.ShopMediaUploadIntent;
import com.twohands.commerce_service.application.shop.uploadshopmedia.ShopMediaUploadStoragePort;
import com.twohands.commerce_service.config.CommerceMinioConfig;
import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
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
@ConditionalOnBean(name = CommerceMinioConfig.PRESIGN_MINIO_CLIENT)
public class MinioShopMediaUploadStorageAdapter implements ShopMediaUploadStoragePort {

    private static final Logger log = LoggerFactory.getLogger(MinioShopMediaUploadStorageAdapter.class);

    private final MinioPresignClientFactory presignClientFactory;
    private final CommerceObjectStorageProperties properties;

    public MinioShopMediaUploadStorageAdapter(
            MinioPresignClientFactory presignClientFactory,
            CommerceObjectStorageProperties properties
    ) {
        this.presignClientFactory = presignClientFactory;
        this.properties = properties;
    }

    @Override
    public ShopMediaUploadIntent createUploadIntent(
            UUID sellerId,
            String contentType,
            String mediaKind,
            Instant expiresAt,
            String clientUploadOrigin
    ) {
        String extension = resolveExtension(contentType);
        String objectKey = buildObjectKey(sellerId, mediaKind, extension);
        String bucket = resolveBucket(mediaKind);
        String mediaUrl = properties.buildPublicObjectUrl(bucket, objectKey);
        String presignEndpoint = clientUploadOrigin != null
                ? clientUploadOrigin
                : properties.resolvePresignedEndpoint();

        try {
            MinioClient minioClient = presignClientFactory.createForEndpoint(presignEndpoint);
            String uploadUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(properties.getPresignedUrlTtlSeconds())
                            .extraHeaders(Map.of("Content-Type", contentType))
                            .build()
            );

            log.info(
                    "Shop media upload URL issued. sellerId={}, mediaKind={}, objectKey={}, presignEndpoint={}, expiresAt={}",
                    sellerId,
                    mediaKind,
                    objectKey,
                    presignEndpoint,
                    expiresAt
            );

            return new ShopMediaUploadIntent(uploadUrl, objectKey, mediaUrl, mediaKind, expiresAt);
        } catch (Exception ex) {
            throw new AppException(
                    ErrorCode.OBJECT_STORAGE_UNAVAILABLE,
                    ErrorCode.OBJECT_STORAGE_UNAVAILABLE.defaultMessage(),
                    ex
            );
        }
    }

    private String buildObjectKey(UUID sellerId, String mediaKind, String extension) {
        String fileName = UUID.randomUUID() + "." + extension;
        return switch (mediaKind) {
            case "SHOP_AVATAR" -> "shops/" + sellerId + "/avatar/" + fileName;
            case "SHOP_COVER" -> "shops/" + sellerId + "/cover/" + fileName;
            case "PRODUCT_THUMBNAIL" -> "products/" + sellerId + "/thumbnails/" + fileName;
            default -> throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "media_kind",
                    "INVALID_VALUE"
            );
        };
    }

    private String resolveBucket(String mediaKind) {
        return "PRODUCT_THUMBNAIL".equals(mediaKind)
                ? properties.getProductBucket()
                : properties.getShopBucket();
    }

    private String resolveExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "content_type",
                    "INVALID_VALUE"
            );
        };
    }
}
