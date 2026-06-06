package com.twohands.commerce_service.application.product.uploadproductmedia;

import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.UpdateProductMediaProductRef;
import com.twohands.commerce_service.domain.product.UpdateProductMediaRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class CreateProductMediaUploadUrlUseCase {

    private static final String SUCCESS_MESSAGE = "Tao link upload anh san pham thanh cong.";

    private final CreateProductMediaUploadUrlValidationService validationService;
    private final UpdateProductMediaRepository updateProductMediaRepository;
    private final CommerceObjectStorageProperties objectStorageProperties;
    private final Optional<ProductMediaUploadStoragePort> productMediaUploadStoragePort;

    public CreateProductMediaUploadUrlUseCase(
            CreateProductMediaUploadUrlValidationService validationService,
            UpdateProductMediaRepository updateProductMediaRepository,
            CommerceObjectStorageProperties objectStorageProperties,
            @Autowired(required = false) ProductMediaUploadStoragePort productMediaUploadStoragePort
    ) {
        this.validationService = validationService;
        this.updateProductMediaRepository = updateProductMediaRepository;
        this.objectStorageProperties = objectStorageProperties;
        this.productMediaUploadStoragePort = Optional.ofNullable(productMediaUploadStoragePort);
    }

    public CreateProductMediaUploadUrlResult execute(CreateProductMediaUploadUrlCommand command) {
        String mediaKind = validationService.validateMediaKind(command.mediaKind());
        String contentType = validationService.validateContentType(command.contentType());
        validationService.validateFileSize(command.fileSizeBytes());

        UpdateProductMediaProductRef product = updateProductMediaRepository
                .findProductByIdAndSellerId(command.productId(), command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.status() == ProductStatus.REMOVED) {
            throw new AppException(ErrorCode.PRODUCT_REMOVED, "Removed product media cannot be uploaded");
        }

        if (!objectStorageProperties.isEnabled()) {
            throw new AppException(
                    ErrorCode.OBJECT_STORAGE_UNAVAILABLE,
                    "Object storage is disabled; enable COMMERCE_MINIO_ENABLED to upload product media"
            );
        }

        ProductMediaUploadStoragePort storagePort = productMediaUploadStoragePort.orElseThrow(() -> new AppException(
                ErrorCode.OBJECT_STORAGE_UNAVAILABLE,
                ErrorCode.OBJECT_STORAGE_UNAVAILABLE.defaultMessage()
        ));

        Instant expiresAt = Instant.now().plusSeconds(objectStorageProperties.getPresignedUrlTtlSeconds());
        ProductMediaUploadIntent intent = storagePort.createUploadIntent(
                command.sellerId(),
                command.productId(),
                contentType,
                mediaKind,
                expiresAt
        );

        return new CreateProductMediaUploadUrlResult(
                intent.uploadUrl(),
                intent.objectKey(),
                intent.mediaUrl(),
                intent.mediaKind(),
                intent.expiresAt().toString(),
                objectStorageProperties.getProductMediaMaxFileSizeBytes(),
                validationService.allowedContentTypes()
        );
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }
}
