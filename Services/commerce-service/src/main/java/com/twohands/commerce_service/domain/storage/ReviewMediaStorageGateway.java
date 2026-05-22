package com.twohands.commerce_service.domain.storage;

import java.util.UUID;

public interface ReviewMediaStorageGateway {

    StoredReviewMedia upload(UUID buyerId, UUID reviewId, ReviewMediaUploadPayload payload);

    void deleteBestEffort(String objectKey);
}
