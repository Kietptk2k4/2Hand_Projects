package com.twohands.commerce_service.domain.review;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UploadReviewMediaRepository {

    Optional<UploadReviewMediaOwnedReview> findOwnedReview(UUID reviewId, UUID buyerId);

    int countMediaByReviewId(UUID reviewId);

    List<ReviewMediaItem> insertMedia(UUID reviewId, List<ReviewMediaInsertDraft> drafts);
}
