package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ModerateReviewRequest(
        @NotBlank String action,
        @NotBlank String reason
) {
}
