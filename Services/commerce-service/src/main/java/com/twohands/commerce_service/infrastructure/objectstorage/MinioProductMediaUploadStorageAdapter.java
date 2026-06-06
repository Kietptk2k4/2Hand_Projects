package com.twohands.commerce_service.infrastructure.objectstorage;

import com.twohands.commerce_service.application.product.uploadproductmedia.ProductMediaUploadIntent;
import com.twohands.commerce_service.application.product.uploadproductmedia.ProductMediaUploadStoragePort;
import com.twohands.commerce_service.common.media.ProductMediaContentValidator;
import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.domain.product.ProductMediaKind;
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
@ConditionalOnBean(MinioClient.class)
public class MinioProductMediaUploadStorageAdapter implements ProductMediaUploadStoragePort {

    private static final Logger log = LoggerFactory.getLogger(MinioProductMediaUploadStorageAdapter.class);

    private final MinioClient minioClient;
    private final CommerceObjectStorageProperties properties;
    private final ProductMediaContentValidator productMediaContentValidator;

    public MinioProductMediaUploadStorageAdapter(
            MinioClient minioClient,
            CommerceObjectStorageProperties properties,
            ProductMediaContentValidator productMediaContentValidator
    ) {
        this.minioClient = minioClient;
        this.properties = properties;
        this.productMediaContentValidator = productMediaContentValidator;
    }

    @Override
    public ProductMediaUploadIntent createUploadIntent(
            UUID sellerId,
            UUID productId,
            String contentType,
            String mediaKind,
            Instant expiresAt
    ) {
        String extension = productMediaContentValidator.resolveExtension(contentType);
        String objectKey = buildObjectKey(sellerId, productId, extension, mediaKind);
        String bucket = properties.getProductBucket();
        String mediaUrl = buildPublicUrl(bucket, objectKey);

        try {
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
                    "Product media upload URL issued. sellerId={}, productId={}, mediaKind={}, objectKey={}, expiresAt={}",
                    sellerId,
                    productId,
                    mediaKind,
                    objectKey,
                    expiresAt
            );

            return new ProductMediaUploadIntent(uploadUrl, objectKey, mediaUrl, mediaKind, expiresAt);
        } catch (Exception ex) {
            throw new AppException(
                    ErrorCode.OBJECT_STORAGE_UNAVAILABLE,
                    ErrorCode.OBJECT_STORAGE_UNAVAILABLE.defaultMessage(),
                    ex
            );
        }
    }

    private String buildObjectKey(UUID sellerId, UUID productId, String extension, String mediaKind) {
        String fileName = UUID.randomUUID() + "." + extension;
        String folder = ProductMediaKind.PRODUCT_VIDEO.equals(mediaKind) ? "videos" : "images";
        return "products/" + sellerId + "/" + productId + "/" + folder + "/" + fileName;
    }

    private String buildPublicUrl(String bucket, String objectKey) {
        String base = trimTrailingSlash(properties.getPublicUrl());
        return base + "/" + bucket + "/" + objectKey;
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}