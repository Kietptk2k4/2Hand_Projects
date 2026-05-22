package com.twohands.commerce_service.delivery.http.admin;

import jakarta.validation.constraints.NotBlank;

public record ModerateShopRequest(
        @NotBlank String action,
        @NotBlank String reason
) {
}
