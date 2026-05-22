package com.twohands.commerce_service.application.product.pauseproduct;

import java.util.UUID;

public record PauseProductCommand(
        UUID sellerId,
        UUID productId
) {
}
