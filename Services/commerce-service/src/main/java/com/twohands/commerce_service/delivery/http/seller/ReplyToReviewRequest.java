package com.twohands.commerce_service.delivery.http.seller;

import jakarta.validation.constraints.NotBlank;

public record ReplyToReviewRequest(@NotBlank String content) {
}
