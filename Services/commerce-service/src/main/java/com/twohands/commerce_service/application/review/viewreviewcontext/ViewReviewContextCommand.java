package com.twohands.commerce_service.application.review.viewreviewcontext;

import java.util.UUID;

public record ViewReviewContextCommand(
        UUID buyerId,
        UUID orderItemId
) {
}
