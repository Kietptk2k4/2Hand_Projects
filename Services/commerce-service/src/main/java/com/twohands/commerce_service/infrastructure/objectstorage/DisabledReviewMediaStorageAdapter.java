package com.twohands.commerce_service.infrastructure.objectstorage;

import com.twohands.commerce_service.domain.storage.ReviewMediaStorageGateway;
import com.twohands.commerce_service.domain.storage.ReviewMediaUploadPayload;
import com.twohands.commerce_service.domain.storage.StoredReviewMedia;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "commerce.object-storage", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DisabledReviewMediaStorageAdapter implements ReviewMediaStorageGateway {

    @Override
    public StoredReviewMedia upload(UUID buyerId, UUID reviewId, ReviewMediaUploadPayload payload) {
        throw new AppException(ErrorCode.OBJECT_STORAGE_UNAVAILABLE, "Object storage is unavailable");
    }

    @Override
    public void deleteBestEffort(String objectKey) {
        // no-op when MinIO is disabled
    }
}
