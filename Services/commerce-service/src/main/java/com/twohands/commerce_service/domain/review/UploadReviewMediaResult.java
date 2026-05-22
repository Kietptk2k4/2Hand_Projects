package com.twohands.commerce_service.domain.review;

import java.util.List;

public record UploadReviewMediaResult(
        List<ReviewMediaItem> media
) {
}
