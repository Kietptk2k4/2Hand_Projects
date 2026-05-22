package com.twohands.commerce_service.delivery.http.review;

import java.util.List;

public record UploadReviewMediaResponse(
        List<ReviewMediaResponse> media
) {
}
