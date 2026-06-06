package com.twohands.commerce_service.application.shop.uploadshopmedia;

import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class CreateShopMediaUploadUrlUseCase {

    private static final String SUCCESS_MESSAGE = "Tao link upload anh shop thanh cong.";

    private final CreateShopMediaUploadUrlValidationService validationService;
    private final CommerceObjectStorageProperties objectStorageProperties;
    private final Optional<ShopMediaUploadStoragePort> shopMediaUploadStoragePort;

    public CreateShopMediaUploadUrlUseCase(
            CreateShopMediaUploadUrlValidationService validationService,
            CommerceObjectStorageProperties objectStorageProperties,
            @Autowired(required = false) ShopMediaUploadStoragePort shopMediaUploadStoragePort
    ) {
        this.validationService = validationService;
        this.objectStorageProperties = objectStorageProperties;
        this.shopMediaUploadStoragePort = Optional.ofNullable(shopMediaUploadStoragePort);
    }

    public CreateShopMediaUploadUrlResult execute(CreateShopMediaUploadUrlCommand command) {
        String mediaKind = validationService.validateMediaKind(command.mediaKind());
        String contentType = validationService.validateContentType(command.contentType());
        validationService.validateFileSize(command.fileSizeBytes());

        if (!objectStorageProperties.isEnabled()) {
            throw new AppException(
                    ErrorCode.OBJECT_STORAGE_UNAVAILABLE,
                    "Object storage is disabled; enable COMMERCE_MINIO_ENABLED to upload shop media"
            );
        }

        ShopMediaUploadStoragePort storagePort = shopMediaUploadStoragePort.orElseThrow(() -> new AppException(
                ErrorCode.OBJECT_STORAGE_UNAVAILABLE,
                ErrorCode.OBJECT_STORAGE_UNAVAILABLE.defaultMessage()
        ));

        Instant expiresAt = Instant.now().plusSeconds(objectStorageProperties.getPresignedUrlTtlSeconds());
        ShopMediaUploadIntent intent = storagePort.createUploadIntent(
                command.sellerId(),
                contentType,
                mediaKind,
                expiresAt
        );

        return new CreateShopMediaUploadUrlResult(
                intent.uploadUrl(),
                intent.objectKey(),
                intent.mediaUrl(),
                intent.mediaKind(),
                intent.expiresAt(),
                objectStorageProperties.getShopMediaMaxFileSizeBytes(),
                validationService.allowedContentTypes()
        );
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }
}
