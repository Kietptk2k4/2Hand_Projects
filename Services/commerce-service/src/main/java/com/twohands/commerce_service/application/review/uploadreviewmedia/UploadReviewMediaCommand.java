package com.twohands.commerce_service.application.review.uploadreviewmedia;

import java.util.List;
import java.util.UUID;

public record UploadReviewMediaCommand(
        UUID buyerId,
        UUID reviewId,
        List<ReviewMediaFileCommand> files
) {
}
